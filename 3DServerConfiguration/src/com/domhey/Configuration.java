package com.domhey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Configuration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Pattern featureTypes = Pattern.compile("<Name>.*?</Name>",
			Pattern.DOTALL);
	private static HashMap<String, String> mapper = new HashMap<>();
	
    public Configuration() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 response.setContentType("text/plain");
		 request.setAttribute("serverUrl", request.getParameter("url"));
		 request.setAttribute("parsedInformation", parseUrl(request.getParameter("url")));
		 ServletContext context = getServletContext();
		 // display all parsed data from the wfs - site
         RequestDispatcher dispatcher = context.getRequestDispatcher("/parsedWfs.jsp");
         dispatcher.forward(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 storeLayerInDatabase(request);
		 ServletContext context = getServletContext();
		 // success html site
         RequestDispatcher dispatcher = context.getRequestDispatcher("/success.jsp");
         dispatcher.forward(request,response);
         // dispatch to sucessfully saved site
	}
	
	
	// query  avalable data from wfs like ressources 
	private String parseUrl(String s){
		String textResponse = "";
		try{
		String addurl = "?REQUEST=GetCapabilities&SERVICE=WFS&Version=1.3.0";
		 
		URL obj = new URL(s+addurl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
 
		int responseCode = con.getResponseCode();
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// get coordinate system and feature types via regex
		String sResponse = response.toString();
		Pattern getCoordTypePattern = Pattern.compile("<DefaultSRS>.*?</DefaultSRS>",Pattern.DOTALL);
		Matcher featureTypeMatcher = featureTypes.matcher(sResponse);
		while (featureTypeMatcher.find()) {
			String s3 = "";
			String st = featureTypeMatcher.group();
			st = st.replace("<Name>", "");
			Pattern coordTypePattern = Pattern.compile(st+".*?</DefaultSRS>",Pattern.DOTALL);
			Matcher matcher2 = coordTypePattern.matcher(sResponse);
				if(matcher2.find()){
					Matcher matcher3 = getCoordTypePattern.matcher(matcher2.group());
						if(matcher3.find()){
							s3 = matcher3.group();
							s3 = s3.replace("<DefaultSRS>", "");
							s3 = s3.replace("</DefaultSRS>", "");
						}
				}
			st = st.replace("</Name>", "");
			mapper.put(st, s3);
			textResponse += st +";";
		}
		
		
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return textResponse;
 
	}
	
	
	
	// Store all the relevant data in the database
	private void storeLayerInDatabase(HttpServletRequest request){
		String res = request.getParameter("ressource"); // ressource of the wfs
		String url = request.getParameter("serverUrl"); // url of the wfs
		String Layername = request.getParameter("layername"); // given Name to show up when requesting the getLayers() method from the server
		boolean cache = Boolean.valueOf(request.getParameter("cache")); // indicates if the ressource should be cached
		String type = "WFS"; // type hardcoded to wfs because only wfs can beadded via the tomcat at the moment
		String coordtype = ""; // sould be filled automatically
		
		if(coordtype.equals("")){
		try{
			coordtype = mapper.get(res); // get the coordtype
		}catch (Exception e){}
		}else{
			// handle if coord type cannot be parsed from the wfs
		}
		
		try{
			// get jdbc driver and store the layer in the actual database , mb its better to implement a rount in the server to call to do so
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:1337/test","root","azzlan1992");
			String insertQuery = "INSERT INTO test.customlayer"
					+"(LAYERNAME,URL,RESSOURCE,TYPE,CACHE,COORDTYPE) VALUES"
					+"(?,?,?,?,?,?)";
			PreparedStatement prep = conn.prepareStatement(insertQuery);
			prep.setString(1,Layername );
			prep.setString(2,url );
			prep.setString(3,res );
			prep.setString(4,type );
			if(cache){
				prep.setString(5,"True" );	
			}else{
				prep.setString(5,"False" );
			}
			prep.setString(6, coordtype);
			prep.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	

}
