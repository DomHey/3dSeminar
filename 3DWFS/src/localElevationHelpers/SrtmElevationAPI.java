package localElevationHelpers;



import java.io.File;
import java.io.IOException;

public class SrtmElevationAPI implements ElevationAPI {

	private SrtmHelper osmSrtm;

	/**
	 * Init the SRTM based ElevationApi
	 * 
	 * @param localDir The local folder that contains the .hgt or .zip srtm files
	 */
	public SrtmElevationAPI(File localDir) {
		osmSrtm = new SrtmHelper(localDir);
	}

	@Override
	public double getElevation(double lat, double lon) throws IOException {
		return osmSrtm.srtmHeight(lat, lon);
	}



}
