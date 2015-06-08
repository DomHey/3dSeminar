import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

// Service to transform epsg
public class CoordinateTransformService {
	public CoordinateTransformService(){
		
	}
	
	// using the open geo tools library , or at least only the libraries for coordinate transforming
	public String transformCoordinates(String currentSceme, String lng, String lat){
		CoordinateReferenceSystem sourceCrs;
		try {
			// target system is epsg 4326
			sourceCrs = CRS.decode(currentSceme);
			CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:4326");
			
			// get coordinate pair 
			double x = Double.valueOf(lng);
			double y = Double.valueOf(lat);
			
			boolean lenient = true;
			MathTransform mathTransform 
			= CRS.findMathTransform(sourceCrs, targetCrs, lenient);
			// transform coordinates
			DirectPosition2D srcDirectPosition2D 
			= new DirectPosition2D(sourceCrs, x, y);
			DirectPosition2D destDirectPosition2D 
			= new DirectPosition2D();
			mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
		
			double transX = destDirectPosition2D.x;
			double transY = destDirectPosition2D.y;
			//return , seperated new coordinates
			return transX + "," + transY;
			
			
			
			
		} catch (Exception e) {
			// if error return empty coordinates
			return "";
		} 
	}
	
}
