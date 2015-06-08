import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;


public class EventSearch {
	private LocalElevationService hService;
	public EventSearch(){
		hService = new LocalElevationService();
	}
	
	public String doGeoEventSearch(String lat, String lng, double km){
		double rd = km* 0.6213;
		// convert km to whatever the api needs
		String url = "http://www.skiddle.com/api/v1/events?api_key=c7d9aa19fc39ca498e3f78c2175f4e52&latitude="+lat+"&longitude="+lng+"&radius="+String.valueOf(rd);
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
			// read response
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			String content = "";
			System.out.println(response.toString());
			String rcontent = response.toString();
			rcontent = rcontent.replace("\\n","");
			// replace broken newlines
			
			try {
				JSONObject o = new JSONObject(rcontent);
				JSONArray arry = o.getJSONArray("results");
				for (int i = 0; i < arry.length(); i++) {
					
				
				JSONObject ob = arry.getJSONObject(i);
				String eventname = ob.getString("eventname");
				JSONObject venue = ob.getJSONObject("venue");
				String address = venue.getString("address");
				String town = venue.getString("town");
				String postcode = venue.getString("postcode");
				String phone = venue.getString("phone");
				String lat2 = venue.getString("latitude");
				String lng2 = venue.getString("longitude");
				String imageurl = ob.getString("imageurl");
				String link = ob.getString("link");
				String date = ob.getString("date");
				String description = ob.getString("description");
				String price = ob.getString("entryprice");
				
				// get all the available data
				
				eventname=eventname.replace("\"", "");
				address=address.replace("\"", "");
				town=town.replace("\"", "");
				postcode=postcode.replace("\"", "");
				phone=phone.replace("\"", "");
				link=link.replace("\"", "");
				date=date.replace("\"", "");
				description=description.replace("\"", "");
				price=price.replace("\"", "");
				imageurl=imageurl.replace("\"", "");
				
				//replace geojson breaking characters
		
		 content += "{"
				+ "\"type\": \"Feature\","
				+ "\"geometry\": {"
				+ "\"type\": \"Point\","
				+ "\"coordinates\": ["
				+ Double.valueOf(lng2)+","+Double.valueOf(lat2)+ "]},"
						+ "\"properties\": {"
						+ "\"name\": \""+eventname+"\","
						+ "\"address\": \"" + address+ "\","
						+ "\"town\": \"" + town+ "\","
						+ "\"postcode\": \"" + postcode+ "\","
						//+ "\"phone\": \"" + phone+ "\","
						//+ "\"link\": \"" + link+ "\","
						+ "\"date\": \"" + date+ "\","
						//+ "\"description\": \"" + description+ "\","
						//+ "\"price\": \"" + price+ "\","
						+ "\"imgurl\": \"" + imageurl+"\"}},";
				}// convert information into geojson
	} catch (JSONException e) {
		e.printStackTrace();
	}
			content = content.replace("\"\"", "\"");
			result = content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public String do3DGeoEventSearch(String lat, String lng, double km){
		// same as above but with 3d data requested
		double rd = km* 0.6213;
		String url = "http://www.skiddle.com/api/v1/events?api_key=c7d9aa19fc39ca498e3f78c2175f4e52&latitude="+lat+"&longitude="+lng+"&radius="+String.valueOf(rd);
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
	 
			//print result
			String content = "";
			System.out.println(response.toString());
			String rcontent = response.toString();
			rcontent = rcontent.replace("\\n","");
			
			try {
				JSONObject o = new JSONObject(rcontent);
				JSONArray arry = o.getJSONArray("results");
				for (int i = 0; i < arry.length(); i++) {
					
				
				JSONObject ob = arry.getJSONObject(i);
				String eventname = ob.getString("eventname");
				JSONObject venue = ob.getJSONObject("venue");
				String address = venue.getString("address");
				String town = venue.getString("town");
				String postcode = venue.getString("postcode");
				String phone = venue.getString("phone");
				String lat2 = venue.getString("latitude");
				String lng2 = venue.getString("longitude");
				String imageurl = ob.getString("imageurl");
				String link = ob.getString("link");
				String date = ob.getString("date");
				String description = ob.getString("description");
				String price = ob.getString("entryprice");
		
		// get 3d coor. via heightservice
		 content += "{"
				+ "\"type\": \"Feature\","
				+ "\"geometry\": {"
				+ "\"type\": \"Point\","
				+ "\"coordinates\": [" 
				+ Double.valueOf(lng2)+","+Double.valueOf(lat2)+","+hService.getElevation(Double.valueOf(lat2),Double.valueOf(lng2))+ "]},"
				+ "\"properties\": {"
				+ "\"name\": \""+eventname+"\","
				+ "\"address\": \"" + address+ "\","
				+ "\"town\": \"" + town+ "\","
				+ "\"postcode\": \"" + postcode+ "\","
				//+ "\"phone\": \"" + phone+ "\","
				//+ "\"link\": \"" + link+ "\","
				+ "\"date\": \"" + date+ "\","
				//+ "\"description\": \"" + description+ "\","
				//+ "\"price\": \"" + price+ "\","
				+ "\"imgurl\": \"" + imageurl+"\"}},";
				}
	} catch (JSONException e) {
		e.printStackTrace();
	}
			
			
			content = content.replace("\"\"", "\"");
			result = content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
