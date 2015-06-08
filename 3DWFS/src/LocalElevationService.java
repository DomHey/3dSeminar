import java.io.File;
import java.io.IOException;

import localElevationHelpers.SrtmElevationAPI;
import localElevationHelpers.SrtmUtil;


public class LocalElevationService {
	private SrtmElevationAPI api;
	// search order of srtm files
	private String[] folders = {"Eurasia","North_America","South_America","Africa","Australia","Islands"};
	// local directory to store the srtm files 
	private File localDir = new File("C:/mapfiles/strm");
	private HeightService heightS = new HeightService();
	public LocalElevationService(){
		api = new SrtmElevationAPI(localDir);
	}
	
	public double getElevation(double lat, double lon){
		try {
			return api.getElevation(lat, lon);
		} catch (IOException e) {
			for (int i = 0; i < folders.length; i++) {
				try {
					SrtmUtil.downloadSrtm(lat, lon, folders[i], localDir);
					// if the file was not found via getElevation() the heightfile must be
					// downloaded after the download, try to get the elevation again
					break;
				} catch (IOException e1) {
					System.out.println(e1);
				}

			}

			try {
				return api.getElevation(lat, lon);
			} catch (IOException e1) {
				// use as fallback google elevation api
				return Double.valueOf(heightS.getHeigtFromCoordinates(String.valueOf(lon), String.valueOf(lat)));
			}

		}
	}
	}
	
	
