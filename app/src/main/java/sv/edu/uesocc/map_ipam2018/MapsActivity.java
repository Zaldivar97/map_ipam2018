package sv.edu.uesocc.map_ipam2018;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    Button find;
    TextView myposition;
    EditText latitud;
    EditText longitud;
    private GoogleMap mMap;
    Location location;
    LocationManager locationManager;
    LocationListener locationListener;
    AlertDialog alert=null;
    WebService ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        find=(Button)findViewById(R.id.find);
        myposition=(TextView)findViewById(R.id.myposition);
        latitud=(EditText)findViewById(R.id.latitud);
        longitud=(EditText)findViewById(R.id.longitud);
        find.setOnClickListener(this);
       SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alert();
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                return;
            }
            else{
                location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }else{
            location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        MostrarLocalizacion(location);

        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            MostrarLocalizacion(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.removeUpdates(locationListener);
            }
        } else {
            locationManager.removeUpdates(locationListener);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }



    }


    public void alert(){
    final AlertDialog.Builder builder= new AlertDialog.Builder(this);
    builder.setMessage("El sistema GPS esta desactivado, ¿Desea Activarlo?")
            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });
    alert=builder.create();
    alert.show();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getApplicationContext(),"Map fragment is ready", Toast.LENGTH_LONG);
    }

    @Override
    public void onClick(View view) {
        try {
            double latValue = Double.parseDouble(latitud.getText().toString());
            double lngValue = Double.parseDouble(longitud.getText().toString());
            LatLng place = new LatLng(latValue, lngValue);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error "+e.getMessage().substring(0,35), Toast.LENGTH_LONG).show();

        }
    }

    public void MostrarLocalizacion(Location loc){



        if (loc!=null){
            ws = new WebService();
            ws.execute(String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()));
        }



    }

    public class WebService extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            String cadena = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
            cadena+=params[0]+",";
            cadena+=params[1]+"&sensor=false";
            String salida="";
            String direccion="";
            try {
                URL url = new URL(cadena);
                HttpURLConnection coneccion = (HttpURLConnection) url.openConnection();
                int respuesta=coneccion.getResponseCode();
                StringBuilder result=new StringBuilder();

                if(respuesta==HttpURLConnection.HTTP_OK){
                    InputStream is = new BufferedInputStream(coneccion.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String linea;
                    while ((linea=br.readLine())!=null){
                        result.append(linea);
                    }
                    JSONObject respuestaJson = new JSONObject(result.toString());
                    JSONArray resultJson = respuestaJson.getJSONArray("results");
                    direccion ="Sin datos para esa direccion";
                    if(resultJson.length()>0){
                        direccion=resultJson.getJSONObject(0).getString("formatted_address");
                    }

                }

            }catch (IOException ex){

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return direccion;
        }

        @Override
        protected void onCancelled(String aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onPostExecute(String aVoid) {
            myposition.setText(aVoid);
        }

        @Override
        protected void onPreExecute() {
            myposition.setText("");
                super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}

