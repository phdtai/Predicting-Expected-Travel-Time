package sample;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.service.geocoding.GeocodingService;
import java.net.URL;
import java.sql.*;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;

public class Controller implements Initializable, MapComponentInitializedListener {

    @FXML
    private GoogleMapView mapView;

    @FXML
    private TextField addressTextField;

    @FXML
    private ListView list;

    @FXML
    private TextArea info;

    private GoogleMap map;

    private GeocodingService geocodingService;

    private StringProperty address = new SimpleStringProperty();

    Marker[] marker = new Marker[5];
    LatLong[] positions = new LatLong[5];
    double[] lat = new double[5];
    double[] lon = new double[5];
    private ArrayList<Polyline> sensorPolylineArray = new ArrayList<>();
    InfoWindow window;

    //Ship and trip details
    public String shipname, startPort, endPort, time;
    public double[] pLat = new double[100];
    public double[] pLong = new double[100];

    //Connects to the database, executes queries and saves results in variables
    public void DatabaseConnector(String myID){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("url", "user", "password");
            Statement stmt = con.createStatement();
            int intID = Integer.parseInt(myID);
            ResultSet rs = stmt.executeQuery("select * from mytable where id ='"+ intID +"'");
                while(rs.next()) {
                    shipname = rs.getString(1);
                    startPort = rs.getString(2);
                    endPort = rs.getString(3);
                    time = rs.getString(4);

                    //More queries
                }
            con.close();
        }catch(Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapView.addMapInializedListener(this);
        //address.bind(addressTextField.textProperty());

        //Creates and adds the items for the list view
        ObservableList<String> items = FXCollections.observableArrayList ("Fahrt 1", "Fahrt 2", "Fahrt 3", "Fahrt 4", "Fahrt 5", "Fahrt 6", "Fahrt 7", "Fahrt 8", "Fahrt 9", "Fahrt 10");
        list.setItems(items);
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        //When a trip gets selected the code gets executed
        list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            //Deletes all markers from the map
            map.clearMarkers();

            //Deletes the current path line displayed on the map
            for (Polyline polyline : sensorPolylineArray) {
                map.removeMapShape(polyline);
            }

            //DatabaseConnector(newValue.toString());
            PointSetter();
            PointConnector();

            //Sets text on the detail window
            info.setText(newValue.toString());
        });
    }


    //Creates the map and sets options
    @Override
    public void mapInitialized() {
        geocodingService = new GeocodingService();
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(new LatLong(53.527206, 9.918959))
                .mapType(MapTypeIdEnum.ROADMAP)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(10);

        map = mapView.createMap(mapOptions);

        //Opens an info window on a specific marker
        /*LatLong center = new LatLong(47.606189, -122.335842);
        InfoWindowOptions infoOptions = new InfoWindowOptions();
        infoOptions.content("<h2>Here's an info window</h2><h3>with some info</h3>")
                .position(center);

        InfoWindow window = new InfoWindow(infoOptions);
        window.open(map, marker[0]);*/
    }

    //Draws a polyline on a path including all markers
    void PointConnector() {

        for(int i = 0; i < positions.length; i++) {
            positions[i] = new LatLong(lat[i], lon[i]);
        }

        LatLong[] array = positions;
        MVCArray pmvc = new MVCArray(array);
        PolylineOptions polyOptions = new PolylineOptions()
                .path(pmvc)
                .strokeColor("red")
                .strokeWeight(2);
        Polyline poly = new Polyline((polyOptions));
        sensorPolylineArray.add(poly);
        map.addMapShape(poly);
    }

    //Sets markers on every position
    void PointSetter() {

        MarkerOptions markerOptions = new MarkerOptions();

        for(int i = 0; i < marker.length; i++) {
            lat[i] = ThreadLocalRandom.current().nextDouble(52, 54);
            lon[i] = ThreadLocalRandom.current().nextDouble(8, 10);
            markerOptions.position( new LatLong(lat[i], lon[i]))
                    .visible(Boolean.FALSE)
                    .title("My Marker");
            marker[i] = new Marker(markerOptions);
            map.addMarker((marker[i]));
        }
    }

}
