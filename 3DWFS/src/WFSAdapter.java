import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WFSAdapter {
	private CoordinateTransformService cService;
	private LocalElevationService leService = new LocalElevationService();
	public WFSAdapter(){
		cService = new CoordinateTransformService();
	}

	
	public String handleWFSRequest(String url, String res, String cache, String espg, boolean threeD){
		String sResponse ="";
		// check if wfs request is cached
		sResponse = getCacheFromDatabase(url+":"+res, threeD);
		
		if(!sResponse.equals("")){
			// if so return cache
			return sResponse;
		}else{
		
		try{
			// request actual features from the wfs
		String aurl = url+"?REQUEST=GetFeature&SERVICE=WFS&Version=1.3.0&COUNT=50&OUTPUTFORMAT=application/json&TYPENAME="+res;
		 
		URL obj = new URL(aurl);
		long before = System.nanoTime();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
 
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		long after = System.nanoTime();
		long runningTimeMs = (after - before) / 1000000;
		System.out.println("Querying wfs Data: " +runningTimeMs );
		
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		 sResponse = response.toString();
		 //Pattern coordinateMatcher = Pattern.compile("\"coordinates\":\\[.*?\\]",Pattern.DOTALL);
		 
		 Pattern coordinateFinder = Pattern.compile("\"coordinates\":\\[.*?\\]}",Pattern.DOTALL);
		 Pattern singleCoord = Pattern.compile("\\[[0-9.,]+\\]");
		 // patter for searching for coordinate pairs, works for all geojson geometries
		 long before1 = System.nanoTime();
		 Matcher matcher = coordinateFinder.matcher(sResponse);
		 while (matcher.find()){
			 String orginalCoordinateContent = matcher.group();
			 String modifiedOrginalContent = orginalCoordinateContent;
			 Matcher matcher2 = singleCoord.matcher(orginalCoordinateContent);
			 	while(matcher2.find()){
			 		String coordToBeReplaced = matcher2.group();
			 		String working = coordToBeReplaced.replace("[", "");
			 		 working = working.replace("]", "");
			 		 String[] workingArray = working.split(",");
			 		 String convertedCoords = "";
					 	for (int i = 0; i < workingArray.length; i+=2) {
							convertedCoords += cService.transformCoordinates(espg, workingArray[i], workingArray[i+1]);
							double l1  = Double.valueOf(convertedCoords.split(",")[0]);
							double l2  = Double.valueOf(convertedCoords.split(",")[1]);
							convertedCoords +=",";
							if(threeD){
								convertedCoords+=leService.getElevation(l2, l1);
								// if 3d data is requested call the elevation service
								convertedCoords+= ",";
							}
						}
					 String replacement = "["+convertedCoords.substring(0,convertedCoords.length()-1) +"]";
					modifiedOrginalContent = modifiedOrginalContent.replace(coordToBeReplaced, replacement);
					// replace orginal coordinates with transformed ones
			 	}
			 
			 	sResponse = sResponse.replace(orginalCoordinateContent, modifiedOrginalContent);
		 }
		 
			long after1 = System.nanoTime();
			long runningTimeMs1 = (after1 - before1) / 1000000;
			System.out.println("Transforming wfs Data: " +runningTimeMs1 );
			
		 
		 
		/* Matcher matcher = coordinateMatcher.matcher(sResponse);
		 
		 while(matcher.find()){
			 String replacementString = matcher.group();
			 String coordinates = replacementString.replace("\"coordinates\":", "");
			 coordinates = coordinates.replace("]", "");
			 coordinates = coordinates.replace("[", "");
			 String[] coordarray = coordinates.split(",");
			 String convertedCoords = "";
			 	for (int i = 0; i < coordarray.length; i+=2) {
					convertedCoords += cService.transformCoordinates(espg, coordarray[i], coordarray[i+1]);
					double l1  = Double.valueOf(convertedCoords.split(",")[0]);
					double l2  = Double.valueOf(convertedCoords.split(",")[1]);
					convertedCoords +=",";
					if(threeD){
						convertedCoords+=leService.getElevation(l2, l1);
						convertedCoords+= ",";
					}
				}
				 String finalReplacement = "\"coordinates\":["+convertedCoords.substring(0, convertedCoords.length()-1)+"]";
				 sResponse = sResponse.replace(replacementString, finalReplacement);
			 }
		 */
		 	if(cache.equals("True")){
		 		// if wfs sould be cached cache the result
			 cacheWfsRequest(url+":"+res, threeD, new WFSEntry(sResponse));
		 	}
		 
		 
				 
		}catch (Exception e){
			System.out.println(e);
		}
		
		return sResponse;
		}
	}
	
	private void cacheWfsRequest(String res, boolean threeD, WFSEntry w){
		try{
			// handle caching 2d and 3d data and update 2d and 3d indicator in cache accordingly
	        Connection conn= Server.conn;
	        PreparedStatement ps=null;
	        String sql=null;

	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(bos);

	        oos.writeObject(w);
	        oos.flush();
	        oos.close();
	        bos.close();
	        // create blob from wfs response (wfsentry serialziable)
	        byte[] data = bos.toByteArray();
	        // handly caching and updating 2d and 3d indices 
	        if(!threeD){
	        	String selectSql = "select * from "+Startup.wfsCache+" where ressource=?";
	        	PreparedStatement pss = conn.prepareStatement(selectSql);
	        	pss.setString(1, res);
	        	ResultSet rs = pss.executeQuery();
	        	if(rs.next()){
	        		sql="update "+Startup.wfsCache+" SET 2D='True'";
	                ps=conn.prepareStatement(sql);
	                ps.executeUpdate();
	        	}else{
	        		sql="insert into "+Startup.wfsCache+" (ressource,2D,content) values(?,?,?)";
	                ps=conn.prepareStatement(sql);
	                ps.setString(1, res);
	                ps.setString(2, "True");
	                ps.setObject(3, data);
	                ps.executeUpdate();
	        	}
	        }else{
	        	String selectSql = "select * from "+Startup.wfsCache+" where ressource=?";
	        	PreparedStatement pss = conn.prepareStatement(selectSql);
	        	pss.setString(1, res);
	        	ResultSet rs = pss.executeQuery();
	        	if(rs.next()){
	        		sql="update "+Startup.wfsCache+" SET 3D='True'";
	                ps=conn.prepareStatement(sql);
	                ps.executeUpdate();
	        	}else{
	        		sql="insert into "+Startup.wfsCache+" (ressource,3D,content) values(?,?,?)";
	                ps=conn.prepareStatement(sql);
	                ps.setString(1, res);
	                ps.setString(2, "True");
	                ps.setObject(3, data);
	                ps.executeUpdate();
	        	}
	        }
	        
	        
		}catch (Exception e){
			System.out.println(e);
		}
	}
	
	
	private String getCacheFromDatabase(String res, boolean threeD){
		Connection conn=Server.conn;
		String sql = null;
		String result = "";
		// see if cached 2d and 3d data exists
		if(threeD){
			sql= "SELECT * FROM "+Startup.wfsCache+" where ressource = '" +res +"' and 3D = 'True'";
		}else{
			sql = "SELECT * FROM "+Startup.wfsCache+" where ressource = '" +res +"'";
		}
		System.out.println(sql);
		try{
        PreparedStatement ps = conn.prepareStatement(sql);

        ResultSet rs = ps.executeQuery();
        
        if(rs.next()){
       	 ByteArrayInputStream bais;

         ObjectInputStream ins;

         try {

         bais = new ByteArrayInputStream(rs.getBytes("content"));

         ins = new ObjectInputStream(bais);
         // deserialize wfs entry
         WFSEntry wfse =(WFSEntry)ins.readObject();

         ins.close();
         result = wfse.getContent();
         } catch (Exception e){
        	 System.out.println(e);
         }
        }
        
		}catch (Exception e){
			System.out.println(e);
		}
		return result;
	}
}
