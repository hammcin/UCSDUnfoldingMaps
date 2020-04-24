

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Hamadi McIntosh
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setup and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = true;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;
	
	private HashMap<String, Integer> quakeCount;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	    
	    buildQuakesCounts();

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	// If there is a marker under the cursor, and lastSelected is null 
	// set the lastSelected to be the first marker found under the cursor
	// Make sure you do not select two markers.
	// 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		for (Marker marker : markers) {
			if (marker.isInside(map, mouseX, mouseY) && lastSelected == null) {
				lastSelected = (CommonMarker) marker;
				marker.setSelected(true);
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		if (lastClicked == null) {
			
			selectMarkerIfClicked(quakeMarkers);
			selectMarkerIfClicked(cityMarkers);
			
			if (lastClicked != null && lastClicked.getClass() == OceanQuakeMarker.class) {
				((OceanQuakeMarker) lastClicked).loadCityMarkers(cityMarkers, map);
			}
			
			selectMarkersInThreatRadius();
			
		}
		else {
			lastClicked = null;
			unhideMarkers();
			
			unClick(quakeMarkers);
			unClick(cityMarkers);
		}
	}
	
	private void unClick(List<Marker> markers) {
		for (Marker m : markers) {
			((CommonMarker)m).setClicked(false);
		}
	}
	
	private void selectMarkersInThreatRadius() {
		if (!(lastClicked == null)) {
			if (lastClicked.getClass() == CityMarker.class) {
				for (Marker m : quakeMarkers) {
					if (m.getDistanceTo(lastClicked.getLocation()) <=
							((EarthquakeMarker)m).threatCircle()) {
						m.setHidden(false);
					}
				}
			}
			else {
				for (Marker m : cityMarkers) {
					if (m.getDistanceTo(lastClicked.getLocation()) <=
							((EarthquakeMarker)lastClicked).threatCircle()) {
						m.setHidden(false);
					}
				}
			}
		}
		else {
			unhideMarkers();
		}
	}
	
	private void selectMarkerIfClicked(List<Marker> markers) {
		for (Marker m : markers) {
			if (m.isInside(map, mouseX, mouseY) && (lastClicked == null)) {
				lastClicked = (CommonMarker) m;
				m.setHidden(false);
				((CommonMarker)m).setClicked(true);
			}
			else {
				m.setHidden(true);
				((CommonMarker)m).setClicked(false);
			}
		}
	}
	
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(200);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		text("City Marker", 75, 100);
		text("Land Quake", 75, 125);
		text("Ocean Quake", 75, 150);
		text("Size ~ Magnitude", 50, 175);
		text("Shallow", 75, 200);
		text("Intermediate", 75, 225);
		text("Deep", 75, 250);
		text("Past hour", 75, 275);
		
		// City marker
		fill(color(255, 0, 0));
		triangle(50, (100-CityMarker.TRI_SIZE),
				(50-CityMarker.TRI_SIZE), (100+CityMarker.TRI_SIZE),
				(50+CityMarker.TRI_SIZE), (100+CityMarker.TRI_SIZE));
		
		fill(color(255,255,255));
		// Land quake marker
		ellipse(50,125,(2*CityMarker.TRI_SIZE),(2*CityMarker.TRI_SIZE));
		// Ocean quake marker
		rect((float)(50-(2*CityMarker.TRI_SIZE)/2.0),(float)(150-(2*CityMarker.TRI_SIZE)/2.0),
				(2*CityMarker.TRI_SIZE),(2*CityMarker.TRI_SIZE));
		
		// Earthquake depth
		fill(color(255,255,0));
		ellipse(50,200,(2*CityMarker.TRI_SIZE),(2*CityMarker.TRI_SIZE));
		fill(color(0,0,255));
		ellipse(50,225,(2*CityMarker.TRI_SIZE),(2*CityMarker.TRI_SIZE));
		fill(color(255,0,0));
		ellipse(50,250,(2*CityMarker.TRI_SIZE),(2*CityMarker.TRI_SIZE));
		
		// Age of earthquake
		fill(color(255,255,255));
		ellipse(50,275,(2.5f*CityMarker.TRI_SIZE),(2.5f*CityMarker.TRI_SIZE));
		stroke(0,0,0);
		strokeWeight(2);
		line((50-(1.5f*CityMarker.TRI_SIZE)),(275-(1.5f*CityMarker.TRI_SIZE)),
				(50+(1.5f*CityMarker.TRI_SIZE)),(275+(1.5f*CityMarker.TRI_SIZE)));
		line((50+(1.5f*CityMarker.TRI_SIZE)),(275-(1.5f*CityMarker.TRI_SIZE)),
				(50-(1.5f*CityMarker.TRI_SIZE)),(275+(1.5f*CityMarker.TRI_SIZE)));
			
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.	
	private boolean isLand(PointFeature earthquake) {
		
		// Loop over all the country markers.  
		// For each, check if the earthquake PointFeature is in the 
		// country in m.  Notice that isInCountry takes a PointFeature
		// and a Marker as input.  
		// If isInCountry ever returns true, isLand should return true.
		for (Marker m : countryMarkers) {
			if (isInCountry(earthquake, m)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	private void buildQuakesCounts() {
		quakeCount = new HashMap<String, Integer>();
		for (Marker quake : quakeMarkers) {
			if (((EarthquakeMarker)quake).isOnLand()) {
				String country = ((LandQuakeMarker)quake).getCountry();
				if (quakeCount.containsKey(country)) {
					quakeCount.put(country, quakeCount.get(country)+1);
				}
				else {
					quakeCount.put(country, 1);
				}
			}
			else {
				if (quakeCount.containsKey("OCEAN QUAKES")) {
					quakeCount.put("OCEAN QUAKES", quakeCount.get("OCEAN QUAKES")+1);
				}
				else {
					quakeCount.put("OCEAN QUAKES", 1);
				}
			}
		}
	}
	
	// prints countries with number of earthquakes
	private void printQuakes() {
		for (String countryKey : quakeCount.keySet()) {
			if (!countryKey.equals("OCEAN QUAKES")) {
				System.out.println(countryKey + ": " + quakeCount.get(countryKey));
			}
		}
		System.out.println("OCEAN QUAKES: " + quakeCount.get("OCEAN QUAKES"));
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
