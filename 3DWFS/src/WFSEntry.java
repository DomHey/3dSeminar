import java.io.Serializable;


public class WFSEntry implements Serializable{
	// serializable to store wfs data
	private String content = new String();
	public WFSEntry(String content) {
		this.content = content;
	}

	
	public String getContent(){
		return this.content;
	}
}
