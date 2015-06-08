import java.io.Serializable;


public class Tweet implements Serializable{
	// tweet to be stored in the twitter database
	private double lat;
	private double lg;
	private double height;
	private long id;
	private String content = new String();
	private String hashtag = new String();
	
	public Tweet(long id,double lat, double lg, double height, String content, String hashtag ){
		this.content = content;
		this.hashtag = hashtag;
		this.lat = lat;
		this.lg = lg;
		this.id = id;
		this.height = height;
	}
	public long getId(){
		return this.id;
	}
	
	public void setHeight(double height){
		this.height = height;
	}

	public double getLat() {
		return lat;
	}
	
	public double getHeight(){
		return height;
	}

	public double getLg() {
		return lg;
	}

	public String getContent() {
		return content;
	}

	public String getHashtag() {
		return hashtag;
	}
}
