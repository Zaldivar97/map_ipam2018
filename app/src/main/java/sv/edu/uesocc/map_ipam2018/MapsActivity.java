package sv.edu.uesocc.map_ipam2018;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.maps.MapFragment;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    final static String USER_AGENT = "Mozilla/5.0";
    final static String[] INIT_PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
    };
    final static int INITIAL_REQUEST = 1337;
    Button find;
    TextView myposition;
    EditText latitud;
    EditText longitud;
    private GoogleMap _mMap;
    Location location;
    LocationManager locationManager;
    LocationListener locationListener;
    AlertDialog alert = null;
    private WebService ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        find = (Button) findViewById(R.id.find);
        myposition = (TextView) findViewById(R.id.myposition);
        latitud = (EditText) findViewById(R.id.latitud);
        longitud = (EditText) findViewById(R.id.longitud);
        find.setOnClickListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        locationListener = new LocationListener() {
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






        if (apiGreaterThan23()) {
            if (!canAccesLocation() || !canAccessCoarseLocation() || !canAccessInternet()) {
                requestPermissions(INIT_PERMS, INITIAL_REQUEST);
            }
        }

           if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
               alert();                                                             
           }                                                                        



    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (canAccessInternet() && canAccessCoarseLocation() && canAccesLocation()) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location==null){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,10,locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                
            }
            MostrarLocalizacion(location);


        } else {
            Toast.makeText(getApplicationContext(), "Permisos ausentes, debe autorizarlos", Toast.LENGTH_LONG).show();
            System.exit(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(locationManager==null){
            Log.d("LOCATION_MANAGER-onpaus","es nulo :(");
        }
            locationManager.removeUpdates(locationListener);



    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
            if(locationManager==null){
                Log.d("LOCATION_MANAGER-onresu","es nulo :(");
            }
            if(locationListener==null){
                Log.d("LISTENER","es nulo :(");
            }
            if(canAccesLocation()) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            }


    }

    public void alert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        Toast.makeText(getApplicationContext(),"The app cannot run without gps service...bye",Toast.LENGTH_LONG);
                        System.exit(0);
                    }
                });
        alert = builder.create();
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
    public void onMapReady(GoogleMap mMap){
        _mMap = mMap;
        _mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng place = new LatLng(12.556677, -89.561878);

        _mMap.addMarker(new MarkerOptions().position(place).title("funciona wey"));
        _mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place,17f));
    }



    @Override
    public void onClick(View view) {
        try {
            double latValue = Double.parseDouble(latitud.getText().toString());
            double lngValue = Double.parseDouble(longitud.getText().toString());
            LatLng place = new LatLng(latValue, lngValue);

            _mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//            _mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
            _mMap.addMarker(new MarkerOptions().position(place).title("Santa Ana"));
            _mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place,17f));



        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error " + e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    public void MostrarLocalizacion(Location loc) {



        if (loc != null) {
            ws=new WebService();
            ws.execute(String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()));
        }


    }



    public boolean hasPermission(String perm) {
        return apiGreaterThan23() ? PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,perm) : true;
    }

    public boolean canAccesLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    public boolean canAccessCoarseLocation() {
        return (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    public boolean canAccessInternet() {
        return (hasPermission(Manifest.permission.INTERNET));
    }

    public boolean apiGreaterThan23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    public class WebService extends AsyncTask<String,Integer,String> {
//        double lat;
//        double lgt;
//        String address;
//
//        private void setAddress(String newAddr){
//          address=newAddr;
//        }
//
//        public String getAddress(){
//            return address;
//        }
//
//            public void setLatlng(double latt,double lgtt){
//            lat=latt;
//            lgt=lgtt;
//        }



          @Override
          protected String doInBackground(String... strings) {
              String cadena = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
        cadena += strings[0] + ",";
              cadena += strings[1] + "&sensor=false";
              String salida = "";
              String direccion = "";
              try {
                  URL url = new URL(cadena);
                  HttpURLConnection coneccion = (HttpURLConnection) url.openConnection();
                  coneccion.setRequestProperty("User-Agent", USER_AGENT);
                  coneccion.setRequestMethod("GET");
                  int respuesta = coneccion.getResponseCode();
                  StringBuilder result = new StringBuilder();

                  if (respuesta == HttpURLConnection.HTTP_OK) {
                      InputStream is = new BufferedInputStream(coneccion.getInputStream());
                      BufferedReader br = new BufferedReader(new InputStreamReader(is));
                      String linea;
                      while ((linea = br.readLine()) != null) {
                          result.append(linea);
                      }
                      JSONObject respuestaJson = new JSONObject(result.toString());
                      JSONArray resultJson = respuestaJson.getJSONArray("results");
                      direccion = "";
                      if (resultJson.length() > 0) {
                          direccion = resultJson.getJSONObject(0).getString("formatted_address");

                      }
                  }


              } catch (IOException ex) {
                          ex.printStackTrace();
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
                      Log.i("GPS",aVoid);
                      //super.onPostExecute(aVoid);
                  }

                  @Override
                  protected void onPreExecute() {
                      super.onPreExecute();
                  }

                  @Override
                  protected void onProgressUpdate(Integer... values) {
                      super.onProgressUpdate(values);
                  }










    }
}



