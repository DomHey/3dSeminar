import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import twitter4j.JSONArray;
import twitter4j.JSONObject;

import com.google.gson.JsonObject;




public class CustomLayerHandler {
	private WFSAdapter wfsAdapter = new WFSAdapter();
	public CustomLayerHandler(){
		
	}
	// handle the radius search
	public String handleRadSearch(String s, boolean threeD){
		String response = "";
		String returnResponse ="";
		try{
		String url= null;
		String res= null;
		String type = null;
		String cache= null;
		String espg = null;
		String selectQuery = "SELECT * FROM test.customlayer WHERE LAYERNAME = ?"; //get saved information for a specific wfs
		Connection con = Server.conn;
		PreparedStatement stat = con.prepareStatement(selectQuery);
		stat.setString(1, s);
		ResultSet set =stat.executeQuery();
		
		if(set.next()){
			url = set.getString(2); // get all the attributes
			res = set.getString(3);
			type = set.getString(4);
			cache = set.getString(5);
			espg = set.getString(6);
		}
		set.close();
		
		if(type.equals("WFS")){ // pass data to the wfs handler if the type of the selected layer = wfs
			response = wfsAdapter.handleWFSRequest(url,res,cache,espg, threeD);
		}// todo register other handlers to handle non wfs requests
		
		JSONObject jObject = new JSONObject(response); 
		JSONArray array = jObject.getJSONArray("features");
		
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				returnResponse+= o.toString();
				returnResponse+=",";
			}
			// return wfs data
		
		}catch (Exception e){
			
		}
		
		
		
		
		return returnResponse;
	}
	
	// doto handle all the other search types
	
	public String handleRadDateSearch(String s){
		return "";
	}
	
	public String handleBBSearch(String s){
		return "";
	}
	
	public String handleBBDateSearch(String s){
		return "";
	}
	
	
	

}
