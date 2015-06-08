import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

public class WeatherSearch {
	private LocalElevationService hService;
	public WeatherSearch(){
		hService = new LocalElevationService();
	}
	
	// get 2D weather from openweathermap by lat / lon
	public String getWeatherByBB(String lat, String lng){
		String url = "http://api.openweathermap.org/data/2.5/find?lat="+lat+"&lon="+lng;
		String result = "";
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			String content ="";
			try {
						JSONObject o = new JSONObject(response.toString());
						JSONArray arry = o.getJSONArray("list");
						for (int i = 0; i < arry.length(); i++) {
							
						
						JSONObject weather = arry.getJSONObject(i);
				String name = "Weather: " + weather.getString("name");
						JSONObject coords = weather.getJSONObject("coord");
				String wlat = coords.getString("lat");
				String wlon = coords.getString("lon");
						JSONObject main = weather.getJSONObject("main");
				long temp = main.getLong("temp");
				int hum = main.getInt("humidity");
				long maxtemp = main.getLong("temp_max");
				long mintemp = main.getLong("temp_min");
						JSONArray weath = weather.getJSONArray("weather");
						JSONObject weathO = weath.getJSONObject(0);
				String weatherD = weathO.getString("description");
				
				// parse values in geojson
				 content += "{"
						+ "\"type\": \"Feature\","
						+ "\"geometry\": {"
						+ "\"type\": \"Point\","
						+ "\"coordinates\": ["
						+ Double.valueOf(wlon)+","+Double.valueOf(wlat)+ "]},"
								+ "\"properties\": {"
								+ "\"name\": \""+name+"\","
								+ "\"temperature\": \"" + temp+ "\","
								+ "\"humidity\": \"" + hum+ "\","
								+ "\"maxTemp\": \"" + maxtemp+ "\","
								+ "\"minTemp\": \"" + mintemp+ "\","
								+ "\"weatherDescriptiom\": \"" + weatherD+"\"}},";
						}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
			
			result = content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	// get 3D coordinates for weather by querying the height service
	public String get3DWeatherByBB(String lat, String lng){
		String url = "http://api.openweathermap.org/data/2.5/find?lat="+lat+"&lon="+lng;
		String result = "";
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			String content ="";
			// get all content from weather api
			try {
						JSONObject o = new JSONObject(response.toString());
						JSONArray arry = o.getJSONArray("list");
						for (int i = 0; i < arry.length(); i++) {
							
						
						JSONObject weather = arry.getJSONObject(i);
				String name = "Weather: " + weather.getString("name");
						JSONObject coords = weather.getJSONObject("coord");
				String wlat = coords.getString("lat");
				String wlon = coords.getString("lon");
						JSONObject main = weather.getJSONObject("main");
				long temp = main.getLong("temp");
				int hum = main.getInt("humidity");
				long maxtemp = main.getLong("temp_max");
				long mintemp = main.getLong("temp_min");
						JSONArray weath = weather.getJSONArray("weather");
						JSONObject weathO = weath.getJSONObject(0);
				String weatherD = weathO.getString("description");
				
				
				 content += "{"
						+ "\"type\": \"Feature\","
						+ "\"geometry\": {"
						+ "\"type\": \"Point\","
						+ "\"coordinates\": ["
						+ Double.valueOf(wlon)+","+Double.valueOf(wlat)+"," +hService.getElevation(Double.valueOf(wlat), Double.valueOf(wlon))+"]},"
								+ "\"properties\": {"
								+ "\"name\": \""+name+"\","
								+ "\"temperature\": \"" + temp+ "\","
								+ "\"humidity\": \"" + hum+ "\","
								+ "\"maxTemp\": \"" + maxtemp+ "\","
								+ "\"minTemp\": \"" + mintemp+ "\","
								+ "\"weatherDescriptiom\": \"" + weatherD+"\"}},";
						}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
			
			result = content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	
}
