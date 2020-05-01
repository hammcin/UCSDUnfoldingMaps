

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

/** Implements a visual marker for cities on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Hamadi McIntosh
 * 
 */
public class CityMarker extends CommonMarker {
	
	public static int TRI_SIZE = 5;  // The size of the triangle marker
	
	public CityMarker(Location location) {
		super(location);
	}
	
	
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
		// Cities have properties: "name" (city name), "country" (country name)
		// and "population" (population, in millions)
	}
	
	
	/**
	 * Implementation of method to draw marker on the map.
	 */
	public void drawMarker(PGraphics pg, float x, float y) {
		// Save previous drawing style
		pg.pushStyle();
		
		// Code to draw a triangle to represent the CityMarker
		pg.fill(255, 0, 0);
		pg.triangle(x, (y-TRI_SIZE), (x-TRI_SIZE), (y+TRI_SIZE), (x+TRI_SIZE), (y+TRI_SIZE));
		
		// Restore previous drawing style
		pg.popStyle();
	}
	
	/** Show the title of the city if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y)
	{
		pg.pushStyle();
		
		String cityInfo = getCity() + ", " + getCountry() +
				" population: " + getPopulation() + " million";
		pg.fill(255, 250, 240);
		pg.rect(x, (y-15), (10+(7*cityInfo.length())+10), 25);
		pg.fill(0, 0, 0);
		pg.text(cityInfo, (x+10), y);
		
		pg.popStyle();
	}
	
	private String getCity()
	{
		return getStringProperty("name");
	}
	
	private String getCountry()
	{
		return getStringProperty("country");
	}
	
	private float getPopulation()
	{
		return Float.parseFloat(getStringProperty("population"));
	}
}
