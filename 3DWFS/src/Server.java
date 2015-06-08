import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;


public class Server extends NanoHTTPD{
	private String url = new String();
	private String name = new String();
	private String pwd = new String();
	protected static Connection conn;
	private TwitterSearch twSearch;
	private WeatherSearch weatherSearch;
	private EventSearch eventSearch;
	private CustomLayerHandler cLayerHandler;

	public Server(int port, String url, String name, String pwd) {
		super(port);
		this.url = url;
		this.name = name;
		this.pwd = pwd;
		// get static database connection
		getDBConnection();
		// register all implemented api's
		twSearch = new TwitterSearch();
		weatherSearch = new WeatherSearch();
		eventSearch = new EventSearch();
		cLayerHandler = new CustomLayerHandler();
	}

	@Override
	public Response serve(String uri, Method method,
            Map<String, String> header, Map<String, String> parms,
            Map<String, String> files){
		ArrayList<String> returnMessage = new ArrayList<>();
		String ret = new String();
		//serve all get requests
		if(method.equals(Method.GET)){
			List<String> keys = new ArrayList<String>(parms.keySet());
			for(String key: keys){
				
				if(key.equals("Request")){
					// if request parameter = getLayers, return all available layers
					if(parms.get(key).equals("getLayers")){
						
						// todo get layers from bounding box and setup database for layers
						ret = "{\"Layers\": [{\"name\":\"Twitter\"},{\"name\":\"Weather\"},{\"name\":\"Events\"}"+queryCustomLayers()
								+ "]}";
					}
					
				}
				
			}
			// Serve Post requests
		}else if(method.equals(Method.POST)){
			String[] layerarray = null;
			boolean threeD = false;
			String searchtype = "";
			String latp1= "",lgp1= "";
			String latp2= "",lgp2= "";
			String rad= "";
			String city= "";
			String date = "";
			List<String> keys = new ArrayList<String>(parms.keySet());
			for (String key: keys) {
					if(key.equals("Layers")){
						layerarray = parms.get(key).split(",");
						// get all queried layers
					}
					if(key.equals("Date")){
						date = parms.get(key);
						//get date if present
					}
					
					if(key.equals("CoordinateType")){
						if(parms.get(key).equals("3D")){
							threeD = true;
							// check if 3d coordinates are requested
						}
					}
			}
			
			for (String key: keys){
				
						
				if(key.equals("SearchType")){
					// save the searchtype for this request and get available coordinates
						searchtype = parms.get(key);
							if(searchtype.contains("BoundingBox")){
								latp1 = searchtype.split(",")[1];
								lgp1 = searchtype.split(",")[2];
								latp2 = searchtype.split(",")[3];
								lgp2 = searchtype.split(",")[4];
								searchtype = "BoundingBox";
							}else if(searchtype.contains("City")){
								city = searchtype.split(",")[1];
								rad = searchtype.split(",")[2];
								searchtype = "City";
							}else if(searchtype.contains("Radius")){
								latp1 = searchtype.split(",")[1];
								lgp1 = searchtype.split(",")[2];
								searchtype = "Radius";
							}
					}
				// handle for each Searchrequest date and 3D parameters , call appropriate method accordingly 
				if(searchtype.equals("BoundingBox")){
					if(!threeD){
						if(!date.equals("")){
							returnMessage = doBBDateSearch(false,date, layerarray , latp1,latp2, lgp1,lgp2);
						}else{	
							returnMessage = doBBSearch(false,layerarray , latp1,latp2, lgp1,lgp2);
						}
					}else{
						if(!date.equals("")){
							returnMessage = doBBDateSearch(true,date, layerarray , latp1,latp2, lgp1,lgp2);
						}else{	
							returnMessage = doBBSearch(true,layerarray , latp1,latp2, lgp1,lgp2);
						}
					}
					
				}else if(searchtype.equals("Radius")){
					if(!threeD){
						if(!date.equals("")){
							returnMessage = doRadDateSearch(false,date,layerarray,latp1,lgp1);
						}else{
							returnMessage= doRadSearch(false,layerarray,latp1,lgp1);
						}
					}else{
						if(!date.equals("")){
							returnMessage = doRadDateSearch(true,date,layerarray,latp1,lgp1);
						}else{
							returnMessage= doRadSearch(true,layerarray,latp1,lgp1);
						}
					}
				}
			}
			for (int i = 0; i < returnMessage.size(); i++) {
				ret+= returnMessage.get(i);
			}
			// wrap all returnd information from layers in a feature collection
			String cont= ret.substring(0, ret.length()-1);
			cont = cont.replace("'","");
			ret= "{\"type\": \"FeatureCollection\",\"features\": ["+cont+"]}";
			
		}
		// return result to the client
		return new NanoHTTPD.Response(ret);

		
	}
	
	private void getDBConnection(){
		try{
			// get database access with URL NAME PWD
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url,name,pwd);
			System.out.println("Connected to Database");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//implement all searches and call functions of each handler with 3d or 2d data
	// first boolean indecates if 3d is needed or not
	private ArrayList<String> doRadSearch(boolean threeD ,String[] layers, String lat, String lg){
		ArrayList<String> results = new ArrayList<>();
		for (int i = 0; i < layers.length; i++) {
			String layer = layers[i];
			if(layer.equals("Twitter")){
				if(threeD){
					results.add(twSearch.doGeoSearch(true,Double.valueOf(lat), Double.valueOf(lg), Double.valueOf(40)));
				}else{
					results.add(twSearch.doGeoSearch(false,Double.valueOf(lat), Double.valueOf(lg), Double.valueOf(40)));
				}
			}if(layer.equals("Weather")){
				if(threeD){
					results.add(weatherSearch.get3DWeatherByBB(lat, lg));	
				}else{
					results.add(weatherSearch.getWeatherByBB(lat, lg));					
				}
			}if(layer.equals("Events")){
				if(threeD){
					results.add(eventSearch.do3DGeoEventSearch(lat, lg, Double.valueOf(40)));	
				}else{
					results.add(eventSearch.doGeoEventSearch(lat, lg, Double.valueOf(40)));					
				}
			}else{
				if(threeD){
					results.add(cLayerHandler.handleRadSearch(layer,true));
				}else{
					results.add(cLayerHandler.handleRadSearch(layer,false));
				}
			}
		}
		return results;
	}
	
	private ArrayList<String> doRadDateSearch(boolean threeD ,String date ,String[] layers, String lat, String lg){
		ArrayList<String> results = new ArrayList<>();
		for (int i = 0; i < layers.length; i++) {
			String layer = layers[i];
			if(layer.equals("Twitter")){
				if(threeD){
					results.add(twSearch.doDateGeoSearch(true,date , Double.valueOf(lat), Double.valueOf(lg), Double.valueOf(40)));

				}else{
					
					results.add(twSearch.doDateGeoSearch(false,date , Double.valueOf(lat), Double.valueOf(lg), Double.valueOf(40)));
				}
			}if(layer.equals("Weather")){
				if(threeD){
					results.add(weatherSearch.get3DWeatherByBB(lat, lg));	
				}else{
					results.add(weatherSearch.getWeatherByBB(lat, lg));					
				}
			}if(layer.equals("Events")){
				if(threeD){
					results.add(eventSearch.do3DGeoEventSearch(lat, lg, Double.valueOf(40)));	
				}else{
					results.add(eventSearch.doGeoEventSearch(lat, lg, Double.valueOf(40)));					
				}
			}else{
				results.add(cLayerHandler.handleRadDateSearch(layer));
			}
		}
		return results;
	}
	
	private ArrayList<String> doBBSearch(boolean threeD ,String[] layers, String latp1, String latp2, String lgp1, String lgp2){
		ArrayList<String> results = new ArrayList<>();
		double latMid = (Double.valueOf(latp1) + Double.valueOf(latp2)) / 2 ;
		double lgMid = (Double.valueOf(lgp1) + Double.valueOf(lgp2)) / 2 ;
		double rad1 = Math.abs((Double.valueOf(latp1) - latMid))*110.54;
		double rad2 = Math.abs((Double.valueOf(lgp1) - lgMid))* (111.320*Math.cos(latMid));
		double rad = Math.max(rad1, rad2);
		
		for (int i = 0; i < layers.length; i++) {
			String layer = layers[i];
			if(layer.equals("Twitter")){	
				if(threeD){
					results.add(twSearch.doGeoSearch(true,latMid, lgMid, rad));

				}else{
					
					results.add(twSearch.doGeoSearch(false,latMid, lgMid, rad));
				}
			}if(layer.equals("Weather")){
				results.add(weatherSearch.getWeatherByBB(String.valueOf(latMid), String.valueOf(lgMid)));
			}if(layer.equals("Events")){
				results.add(eventSearch.doGeoEventSearch(String.valueOf(latMid), String.valueOf(lgMid), rad));
			}else{
				results.add(cLayerHandler.handleBBSearch(layer));
			}
		}
		
		
		return results;
		
		
	}
	
	

	private ArrayList<String> doBBDateSearch(boolean threeD ,String date ,String[] layers, String latp1, String latp2, String lgp1, String lgp2){
		ArrayList<String> results = new ArrayList<>();
		double latMid = (Double.valueOf(latp1) + Double.valueOf(latp2)) / 2 ;
		double lgMid = (Double.valueOf(lgp1) + Double.valueOf(lgp2)) / 2 ;
		double rad1 = Math.abs((Double.valueOf(latp1) - latMid))*110.54;
		double rad2 = Math.abs((Double.valueOf(lgp1) - lgMid))* (111.320*Math.cos(latMid));
		double rad = Math.max(rad1, rad2);
		
		// go through all layers and add results the the resultarray which is finally wrapped in
		// a geojson featurecollection
		for (int i = 0; i < layers.length; i++) {
			String layer = layers[i];
			if(layer.equals("Twitter")){	
				if(threeD){
					results.add(twSearch.doDateGeoSearch(true,date,latMid, lgMid, rad));

				}else{
					
					results.add(twSearch.doDateGeoSearch(false,date,latMid, lgMid, rad));
				}
			}if(layer.equals("Weather")){
				results.add(weatherSearch.getWeatherByBB(String.valueOf(latMid), String.valueOf(lgMid)));
			}if(layer.equals("Events")){
				results.add(eventSearch.doGeoEventSearch(String.valueOf(latMid), String.valueOf(lgMid), rad));
			}else{
				results.add(cLayerHandler.handleBBDateSearch(layer));
			}
		}
		
		
		return results;
		
		
	}
	// return all the custom layers saved in the database
	private String queryCustomLayers(){
		String retString = "";
		try{
		String selectQuery = "SELECT LAYERNAME from "+Startup.availableLayers;
		PreparedStatement prep = conn.prepareStatement(selectQuery);
		ResultSet set = prep.executeQuery();
		while(set.next()){
			retString+= ",{\"name\":\""+set.getString(1)+"\"}";
		}
		
		}catch(Exception e){
			System.out.println(e);
		}
		return retString;
		
	}
	
}
