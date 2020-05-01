

import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Hamadi McIntosh
 *
 */
public class OceanQuakeMarker extends EarthquakeMarker {
	
	public static boolean isLoaded;
	
	public static UnfoldingMap map;
	
	// Markers for each city
	public static List<Marker> cityMarkers;
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = false;
		
		isLoaded = false;
	}
	

	/** Draw the earthquake as a square */
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		// Drawing a centered square for Ocean earthquakes
		// DO NOT set the fill color.  That will be set in the EarthquakeMarker
		// class to indicate the depth of the earthquake.
		// Simply draw a centered square.
						
		// Notice the radius variable in the EarthquakeMarker class
		// and how it is set in the EarthquakeMarker constructor
						
		pg.rect((x-(getRadius()/(2.0f))), (y-(getRadius()/(2.0f))),
				getRadius(), getRadius());
		
		pg.pushStyle();
		if (isLoaded) {
			if (getClicked()) {
				Location quakeLoc = getLocation();
				ScreenPosition quakePos = map.getScreenPosition(quakeLoc);
				Location cityLoc;
				ScreenPosition cityPos;
				for (Marker m : cityMarkers) {
					if (m.getDistanceTo(getLocation()) <=
							threatCircle()) {
						cityLoc = m.getLocation();
						cityPos = map.getScreenPosition(cityLoc);
						pg.stroke(0, 255, 0);
						pg.line((quakePos.x-200),(quakePos.y-50),(cityPos.x-200),(cityPos.y-50));
					}
				}
			}
			else {
				pg.noStroke();
			}
		}
		pg.popStyle();
	}
	
	public static void loadCityMarkers(List<Marker> cityMarkerList,
			UnfoldingMap cityMap) {
		isLoaded = true;
		cityMarkers = cityMarkerList;
		map = cityMap;
	}
	

	

}
