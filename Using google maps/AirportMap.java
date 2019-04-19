package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	
	//First click lastSelected
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
		
	
	public void setup() {
		// setting up PAppler
		size(800,600, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 50, 50, 750, 550);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
	
			m.setRadius(5);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		int dest = 0;
		int source = 0;
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			source = Integer.parseInt((String)route.getProperty("source"));
			dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				if(source==507) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
				}
			}
					
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);
		
		map.addMarkers(airportList);
		
		//Hide airports that are not on the destination of the routes from Heathrow
				
	}
		
	
	
	public void draw() {
		background(0);
		map.draw();
		
	}
	
	private void selectMarkerIfHover(List<Marker> markers) {
		
		//Abort if there is already a marker selected
		if(lastSelected != null) {
			return;
		}
			
		for(Marker m : markers) {
			CommonMarker marker = (CommonMarker)m;
			if(marker.isInside(map, mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	public void mouseMoved() {
		
		//clear the last selection
		
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		
		selectMarkerIfHover(airportList);
	}
	
	public void mouseClicked() {
		//if the last thing clicked was a marker (lastClicked != null), then check if it was firstMarker or not. If it was
		//firstMarker then we go next. If it was not firstMarker then we unhide all.
		if(lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if(lastClicked == null) {
			checkAirportForClicked();
		}
	}
	
	//Private method to check which airport marker was clicked
	private void checkAirportForClicked() {
		if(lastClicked != null) {
			return;
		}
		for(Marker marker : airportList) {
			if(!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
					lastClicked = (CommonMarker)marker;
					for(Marker mhide : airportList) {
						if(mhide != lastClicked) {
							mhide.setHidden(true);
						}
					}
			return;			
			}
		}
	}
		
	public void unhideMarkers() {
		for(Marker marker : airportList) {
			marker.setHidden(false);
		}
	}
	
}
