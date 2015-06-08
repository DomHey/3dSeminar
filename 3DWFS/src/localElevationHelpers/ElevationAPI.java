package localElevationHelpers;


import java.util.List;

/**
 * Defines the base method of Elevation API
 * 
 */
public interface ElevationAPI {

	/**
	 * Get the elevation of a given latitude and longitude
	 * 
	 * @param lat Latitude
	 * @param lon Longitude
	 * @return The elevation of given point
	 */
	public double getElevation(double lat, double lon) throws Exception;

	/**
	 * Get the elevation of a given GeoPoint
	 * 
	 * @param lat Latitude
	 * @param lon Longitude
	 * @return The elevation of given point
	 */

}
