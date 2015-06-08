import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import com.google.gson.Gson;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterSearch {
	private Twitter twitter;
	private String url,name,pwd;
	private Connection conn = Server.conn;
	private LocalElevationService leService = new LocalElevationService(); 
	
	public TwitterSearch() {

		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		// aquire own keys and secrets by registering an app on your twitter account!
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey("KEY")
			.setOAuthConsumerSecret("CSecret")
			.setOAuthAccessToken("AT")
			.setOAuthAccessTokenSecret("TS");
		
		TwitterFactory tf = new TwitterFactory(cb.build());
		// get twitter instance via auth keys
		twitter = tf.getInstance();
	}
	
	
	
	public ArrayList<Tweet> getChachedPosts(String date, String cType){
		ArrayList<Tweet> aReturn = new ArrayList<Tweet>();

        try{
             Connection conn=Server.conn;
             PreparedStatement ps=null;
             ResultSet rs=null;
             String sql=null;
             // check if twitter post with a specific date and type(2D/3D) is cached
             if(cType.equals("3D")){
            	 sql="select * from "+Startup.twitterCache+" where date='" + date + "' and 3D ='True'";
            	 
             }else{
            	 sql = "select * from "+Startup.twitterCache+" where date = '" + date+"'";
             }
             ps=conn.prepareStatement(sql);

             rs=ps.executeQuery();
             
             // if cached posts are found get them deserialize them and return them
             while(rs.next())
             {
            	 ByteArrayInputStream bais;

                 ObjectInputStream ins;

                 try {

                 bais = new ByteArrayInputStream(rs.getBytes("twitterObject"));

                 ins = new ObjectInputStream(bais);

                 Tweet t =(Tweet)ins.readObject();

                 ins.close();
                 
                 aReturn.add(t);

                 }
                 catch (Exception e) {
                	 System.out.println(e);
                 }
             }

             return aReturn;

        }
        catch(Exception e)
        {	System.out.println(e);
            return aReturn;
        }
        
	}
	
	public  void cacheTwitterPost(String date, Tweet t, String coordinateType) throws Exception
    {
		// store twitter posts, handle updating the 3d and 2d fields
        try{
        Connection conn= Server.conn;
        PreparedStatement ps=null;
        String sql=null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(t);
        oos.flush();
        oos.close();
        bos.close();
        // serialize tweet calls into blob
        byte[] data = bos.toByteArray();

        if(coordinateType.equals("2D")){
        	// if cached tweets are already present set 2d index to true
        	String selectSql = "select * from "+Startup.twitterCache+" where id=?";
        	PreparedStatement pss = conn.prepareStatement(selectSql);
        	pss.setLong(1, t.getId());
        	ResultSet rs = pss.executeQuery();
        	if(rs.next()){
                sql="update "+Startup.twitterCache+" SET 2D='True'";
                ps=conn.prepareStatement(sql);
                ps.executeUpdate();
        	}else{
        		// if no data is present add to cache and set 2d to true
                sql="insert into "+Startup.twitterCache+" (id,2D,layer,twitterObject,date) values(?,?,?,?,?)";
                ps=conn.prepareStatement(sql);
                ps.setLong(1, t.getId());
                ps.setString(2, "True");
                ps.setString(3, "Twitter");
                ps.setObject(4, data);
                ps.setString(5, date);
                ps.executeUpdate();
        	}
        }else{
        	// if cached tweets are already present set 3d index to true
        	String selectSql = "select * from "+Startup.twitterCache+" where id=?";
        	PreparedStatement pss = conn.prepareStatement(selectSql);
        	pss.setLong(1, t.getId());
        	ResultSet rs = pss.executeQuery();
        	if(rs.next()){
        		// if data is present update 3d index to true
                sql="update "+Startup.twitterCache+" SET 3D='True'";
                ps=conn.prepareStatement(sql);
                ps.executeUpdate();
        	}else{
        		// if no data is present update 3d index to true and add data
                sql="insert into "+Startup.twitterCache+" (id,3D,layer,twitterObject,date) values(?,?,?,?,?)";
                ps=conn.prepareStatement(sql);
                ps.setLong(1, t.getId());
                ps.setString(2, "True");
                ps.setString(3, "Twitter");
                ps.setObject(4, data);
                ps.setString(5, date);
                ps.executeUpdate();
        	}
        	
        }

        }
        catch(Exception e)
        {
        }

    }
	
	public String doDateGeoSearch( final boolean threeD,final String date ,final double lat, final double lg, final double rd){
		// search twitter posts for a region with given date (yyyy-MM-dd)
		
		
		Callable<ArrayList<Tweet>> searchResult = new Callable<ArrayList<Tweet>>() {

			@Override
			public ArrayList<Tweet> call() throws Exception {
				 Date d1 = null ;
				 Date d2 = null;
				try {
					Date searchDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date);
					d1 = new Date(searchDate.getTime() + (1000*60*60*24));
					d2 = new Date(searchDate.getTime() - (1000*60*60*24));
					// get the day after and the day before the searchdate, because twitter api
					// returns data between 2 days
				} catch (ParseException e1) {
				
					e1.printStackTrace();
				}

				ArrayList<Tweet> posts = new ArrayList<Tweet>(); 
				try {
					// construct the query for twitter api
					Format formatter = new SimpleDateFormat("yyyy-MM-dd");
					String s1 = formatter.format(d1);
					String s2 = formatter.format(d2);
					twitter4j.Query dateQuery = new Query();
					dateQuery.geoCode(new GeoLocation(lat, lg), rd, "km");
					dateQuery.setSince(s2);
					dateQuery.setUntil(s1);
					QueryResult result = twitter.search(dateQuery);
					ArrayList tweets = (ArrayList) result.getTweets();
					String hashtags = new String("");
					// get each tweet
					for (int i = 0; i < tweets.size(); i++) {
						Status t = (Status) tweets.get(i);
						long id = t.getId();
						GeoLocation geo = t.getGeoLocation();
						HashtagEntity[] entities = t.getHashtagEntities();
						for (int j = 0; j < entities.length; j++) {
							HashtagEntity ent = entities[j];
							hashtags += "#" +ent.getText() + " ";
						}
						// get content and hashtags of each tweet and create a 2d tweet object
						
						Tweet tweet = new Tweet(id,geo.getLatitude(), geo.getLongitude(),(double)0, t.getText(), hashtags);
						posts.add(tweet);

						
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return posts;
			}
		};
		
		
		ArrayList<Tweet> result = null;
		String geoJson = "";
		String tweet = "";
		try {
			// query at first for cached posts if no posts found call the twitter api
			if(!threeD){
				System.out.println("2d cashed posts");
				ArrayList<Tweet> tweets = getChachedPosts(date, "2D");
				if(tweets.isEmpty()){
					result = searchResult.call();
				}else{
					System.out.println("cachedposts as return");
					result = tweets;
				}
			// create geojson features
			for (int i = 0; i < result.size(); i++) {
				Tweet t = result.get(i);
				String content = t.getContent();
				content = content.replace(":", "");
				content = content.replace("\"", "");
				tweet= "{"
						+ "\"type\": \"Feature\","
						+ "\"geometry\": {"
						+ "\"type\": \"Point\","
						+ "\"coordinates\": ["
						+ t.getLg()+","+t.getLat()+ "]},"
								+ "\"properties\": {"
								+ "\"id\": \""+t.getId()+"\","
								+ "\"text\": \"" + content+ "\","
								+ "\"hashtags\": \"" + t.getHashtag()+"\"}},";
				tweet = tweet.replace("\n", "");
				cacheTwitterPost(date,t, "2D");
				geoJson+=tweet;
			}; 

			}else{
				// query for 3d cached posts
				ArrayList<Tweet> tweets = getChachedPosts(date, "3D");
				if(tweets.isEmpty()){	
					// if no posts found call twitter api
					result = searchResult.call();
				}else{
					result = tweets;
				}
				// create geojson features from tweets
				for (int i = 0; i < result.size(); i++) {
					Tweet t = result.get(i);
					String content = t.getContent();
					content = content.replace(":", "");
					content = content.replace("\"", "");
					String height = String.valueOf(leService.getElevation(t.getLat(), t.getLg()));
					//String height =  hService.getHeigtFromCoordinates(String.valueOf(t.getLg()),String.valueOf( t.getLat()));
					t.setHeight(Double.valueOf(height));
					tweet= "{"
							+ "\"type\": \"Feature\","
							+ "\"geometry\": {"
							+ "\"type\": \"Point\","
							+ "\"coordinates\": ["
							+ t.getLg()+","+t.getLat()+","+t.getHeight()+ "]},"
									+ "\"properties\": {"
									+ "\"id\": \""+t.getId()+"\","
									+ "\"text\": \"" + content+ "\","
									+ "\"hashtags\": \"" + t.getHashtag()+"\"}},";
					cacheTwitterPost(date,t, "3D");
					tweet = tweet.replace("\n", "");
					geoJson+=tweet;
				}; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		return geoJson;
		
	}
	
 // do a normal geo search with no caching
		
	public String doGeoSearch(final boolean threeD,final double lat, final double lg, final double rd){
		Callable<ArrayList<Tweet>> searchResult = new Callable<ArrayList<Tweet>>() {
			// get twitter data async
			@Override
			public ArrayList<Tweet> call() throws Exception {
				ArrayList<Tweet> posts = new ArrayList<Tweet>(); 
				try {
					Query quer = new Query().geoCode(new GeoLocation(lat, lg), rd, "km");
					quer.setResultType(Query.MIXED); // new and old data for variation
					// normal search query for tweets in a certain radius
					QueryResult result = twitter.search(new Query().geoCode(new GeoLocation(lat, lg), rd, "km"));
					ArrayList tweets = (ArrayList) result.getTweets();
					String hashtags = new String("");
					
					for (int i = 0; i < tweets.size(); i++) {
						Status t = (Status) tweets.get(i);
						long id = t.getId();
						GeoLocation geo = t.getGeoLocation();
						HashtagEntity[] entities = t.getHashtagEntities();
						for (int j = 0; j < entities.length; j++) {
							HashtagEntity ent = entities[j];
							hashtags += "#" +ent.getText() + " ";
						}
						
						Tweet tweet = new Tweet(id,geo.getLatitude(), geo.getLongitude(),(double)0, t.getText(), hashtags);
						posts.add(tweet);
						
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return posts;
			}
		};
		
		// return 2d post
		ArrayList<Tweet> result = null;
		String geoJson = "";
		String tweet = "";
		try {
			result = searchResult.call();
			if(!threeD){
			for (int i = 0; i < result.size(); i++) {
				Tweet t = result.get(i);
				String content = t.getContent();
				content = content.replace(":", "");
				content = content.replace("\"", "");
				tweet= "{"
						+ "\"type\": \"Feature\","
						+ "\"geometry\": {"
						+ "\"type\": \"Point\","
						+ "\"coordinates\": ["
						+ t.getLg()+","+t.getLat() +"]},"
								+ "\"properties\": {"
								+ "\"id\": \""+t.getId()+"\","
								+ "\"text\": \"" + content+ "\","
								+ "\"hashtags\": \"" + t.getHashtag()+"\"}},";
				tweet = tweet.replace("\n", "");
				geoJson+=tweet;
			}; 
			}else{
				// return 3d post
				for (int i = 0; i < result.size(); i++) {
					Tweet t = result.get(i);
					String content = t.getContent();
					content = content.replace(":", "");
					content = content.replace("\"", "");
					String height = String.valueOf(leService.getElevation(t.getLat(), t.getLg()));
					tweet= "{"
							+ "\"type\": \"Feature\","
							+ "\"geometry\": {"
							+ "\"type\": \"Point\","
							+ "\"coordinates\": ["
							+ t.getLg()+","+t.getLat()+","+height+ "]},"
									+ "\"properties\": {"
									+ "\"id\": \""+t.getId()+"\","
									+ "\"text\": \"" + content+ "\","
									+ "\"hashtags\": \"" + t.getHashtag()+"\"}},";
					tweet = tweet.replace("\n", "");
					geoJson+=tweet;
				}; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return geoJson;
		
	}
	
		
	
}
