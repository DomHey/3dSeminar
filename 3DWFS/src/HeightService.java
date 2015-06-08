import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;


public class HeightService {
	public HeightService(){
		
	}
	// query google api as fallback for heights when 3d coords are requested
	
	public String getHeigtFromCoordinates(String lng, String lat){
		String url = "http://maps.googleapis.com/maps/api/elevation/json?locations=" + lat +"," + lng ;
		String result = "";
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 

	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			result = response.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			JSONObject jsonObj = new JSONObject(result);
			JSONArray jarray = jsonObj.getJSONArray("results");
			if (jarray.length() > 0){
				result = jarray.getJSONObject(0).getString("elevation");
				// parse elevation from returned json
			}else{
				result = "NAN";
				// if no elevation exists return nan as default
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return result;
	}
}
