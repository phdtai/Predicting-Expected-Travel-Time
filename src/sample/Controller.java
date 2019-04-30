package sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.service.geocoding.GeocodingService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.*;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
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
import jdk.nashorn.internal.parser.JSONParser;

import javax.print.DocFlavor;
import java.net.URL;

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

    Marker[] marker;
    LatLong[] positions;
    private ArrayList<Polyline> sensorPolylineArray = new ArrayList<>();

    //Ship information
    String newline = System.getProperty("line.separator");
    public JsonElement tripID,shipName, startTime, port, finalPort, shipType, latHolder, lonHolder;
    public JsonObject shipPath;

    public Double[] latPos = new Double[10000];
    public Double[] lonPos = new Double[10000];

    //Connects to the API
    public void APIReader(String apiID) throws IOException {

        String sURL = "https://api.mzabel.eu/felixstowe-rotterdam/" + apiID;
        URL url = new URL(sURL);
        HttpURLConnection request =  (HttpURLConnection) url.openConnection();
        request.connect();

        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject obj = root.getAsJsonObject();

        shipName = obj.getAsJsonObject("data").get("Name");
        tripID = obj.getAsJsonObject("data").get("TripID");
        startTime = obj.getAsJsonObject("data").get("StartTime");
        port = obj.getAsJsonObject("data").get("StartPort");
        finalPort = obj.getAsJsonObject("data").get("EndPort");
        shipType = obj.getAsJsonObject("data").get("shiptype");

        shipPath = obj.getAsJsonObject("data").getAsJsonObject("path");

        for (int i = 1; i < shipPath.size(); i++) {
            String number = Integer.toString(i);
            latHolder = shipPath.getAsJsonObject(number).get("lat");
            lonHolder = shipPath.getAsJsonObject(number).get("lon");
            String latiHolder = latHolder.toString();
            String longHolder = lonHolder.toString();
            latPos[i - 1] = Double.parseDouble(latiHolder);
            lonPos[i - 1] = Double.parseDouble(longHolder);
            //System.out.println(latPos[i]);
           // System.out.println(lonPos[i]);
        }



    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapView.addMapInializedListener(this);
        //address.bind(addressTextField.textProperty());

        List<String> counter = new ArrayList<>();
        //Creates and adds the items for the list view
        for (int i = 0; i <= 1837; i++) {
            counter.add("Fahrt " + i);
        }
        ObservableList<String> items = FXCollections.observableArrayList (counter);
        list.setItems(items);
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        //When a trip gets selected the code gets executed
        list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            //Deletes all markers from the map
            map.clearMarkers();
            String result = newValue.toString().split("\\s+")[1];
            //Deletes the current path line displayed on the map
           for (Polyline polyline : sensorPolylineArray) {
               map.removeMapShape(polyline);
            }

            try {
                APIReader(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PointSetter();
            PointConnector();

            //Sets text on the detail window
            info.setText("Ship: " + shipName.toString() + newline);
            info.appendText("Date: " + startTime.toString() + newline);
            info.appendText("From: " + port.toString() + newline);
            info.appendText("To: " + finalPort.toString() + newline);
            info.appendText("Type: " + shipType.toString());

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

        positions = new LatLong[shipPath.size()];
        for(int i = 0; i <  positions.length; i++) {
            if (latPos[i] != null && lonPos[i] != null ) {
                positions[i] = new LatLong(latPos[i], lonPos[i]);
            } else {
                positions[i] = new LatLong(latPos[i - 2], lonPos[i - 2]);
            }
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

        marker = new Marker[shipPath.size()];
        MarkerOptions markerOptions = new MarkerOptions();

        for(int i = 0; i < shipPath.size(); i++) {
            //public Double[] latPos = new Double[1000];
            //public Double[] lonPos = new Double[1000];
           // lat[i] = ThreadLocalRandom.current().nextDouble(52, 54);
            //lon[i] = ThreadLocalRandom.current().nextDouble(8, 10);
            //lat[i] = latPos[i];
            //lon[i] = lonPos[i];
            if (latPos[i] != null && lonPos[i] != null ) {
                markerOptions.position( new LatLong(latPos[i], lonPos[i]))
                        .visible(Boolean.TRUE)
                        .title("My Marker");
                marker[i] = new Marker(markerOptions);
                map.addMarker((marker[i]));
            }
        }
    }

}
