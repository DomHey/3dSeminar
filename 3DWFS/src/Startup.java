import java.io.IOException;



public class Startup {
	static String twitterCache = "";
	static String wfsCache = "";
	static String availableLayers = "";
	public static void main(String[] args) {
		try {
			String[] params = args[0].split(",");
		    int port = Integer.valueOf(params[0]);
		    String URL = params[1];
		    String name = params[2];
		    String pwd = params[3];
		    twitterCache=params[4];
			wfsCache=params[5];
			availableLayers=params[6];
			// new Server(PORT,URL zur Datenbank,Benutzername,Password)
			new Server(port,URL,name,pwd).start();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		while(true){
			// loop until server is manually closed
		}
	
	}
	
}
