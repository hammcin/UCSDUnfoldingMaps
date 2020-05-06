# Interactive Earthquake Map

This project is an interactive visualization of a large dataset tagged by
geospatial information.

![Interactive Earthquake Map][image2]

## Data on Map and Custom Markers

The earthquake map reads earthquake data from a live
[RSS feed](http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom).
Markers on the map indicate locations where earthquakes of magnitude 2.5 or
higher have occurred in the last week.  Earthquake markers are customized for
different kinds of earthquakes depending on whether the epicenter is over land
or in the ocean.  Additionally, the earthquake map displays markers to indicate
the locations of major cities and their proximity to earthquakes.  These city
markers are styled differently to distinguish them from earthquake markers.

## Responding to User Actions

Additional information is displayed when the user hovers over or clicks on any
marker with the mouse.  When the user hovers over a city marker, the earthquake
map displays a box with the city's name, country, and population.  When the user
hovers over an eaqrthquake marker, the map displays the title of the earthquake
(including its magnitude and region).  Clicking on a marker gives even more
information: A click on a city marker will lead to only that city and
earthquakes which affect it being displayed on the map.  Clicking once again on
that marker will bring the rest of the map's markers back.  Similarly, after
clicking on an earthquake marker, only cities potentially affected by that
earthquake will be displayed.

## Popup Menu

When a city marker on the earthquake map is clicked, a popup menu appears under
the map key.  The popup menu displays the name and country of the city that was
clicked.  The number of nearby earthquakes for which the city lies within the
threat circle and the average magnitude of those earthquakes are also displayed.
The menu additionally displays information about the most recent nearby
earthquake, if one exists.

The `addPopup` method which is called by the `draw` method in the
`EarthquakeCityMap` class is responsible for drawing the popup menu with nearby
earthquake information when a `CityMarker` is clicked.  To ensure the `addPopup`
method only gets called when a `CityMarker` is clicked, the `boolean` field
`cityClicked` in the `EarthquakeCityMap` class keeps track of when a
`CityMarker` is clicked.  The `cityClicked` field is set to `false` in the
`mouseClicked` method if `lastClicked != null`.  Finally, the `cityClicked`
field is set equal to `true` in the `selectMarkersInThreatRadius` method when a
click on a `CityMarker` is detected.

The `addPopup` method calculates information about nearby earthquakes to display
by looping over all `EarthquakeMarkers` and determining if the `CityMarker` that
has been clicked lies within the threat circle of the `EarthquakeMarker`.  To
find the most recent earthquake to occur near a city it is necessary to compare
the `age` property of `EarthquakeMarker`s.  The `stringAgeToInt` method converts
the `age` property of an `EarthquakeMarker` which is represented by a `String`
to an `int`.  While the `compAge` method compares the `age` properties of two
`EarthquakeMarker`s to determine which earthquake occurred most recently.

![Popup Menu for Nearby Earthquakes][image1]

## Acknowledgements

This project was developed for the course Object Oriented Programming in Java
offered by the University of California, San Diego as part of the
Object Oriented Programming in Java Specialization on Coursera.  The starter
code for this project was provided by the UC San Diego Intermediate Software
Development MOOC team.  Additionally, this project makes use of the Unfolding
Maps library for interactive maps and geovisualizations
[(found here)](http://unfoldingmaps.org/) which was developed by Till Nagel and
the team at the University of Applied Sciences Potsdam along with other
contributors.  As part of this package, the SQLite library
[(found here)](https://bitbucket.org/xerial/sqlite-jdbc/) (version 3.7.2) is
included.  This Java library was created by xerial.org. SQLite was developed by
D. Richard Hipp.

[image1]: ./EarthquakeMapPopup.png
[image2]: EarthquakeMapMouseHover.png
