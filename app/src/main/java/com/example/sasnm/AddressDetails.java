package com.example.sasnm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.Nextbillion;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;

public class AddressDetails extends AppCompatActivity {
    MapView mapView;
    EditText editTextStreet, editTextSuite, editTextCity, editTextState, editTextPostalCode, editTextPhoneNumber;
    Button button;
    String latitude, longitude, urlUpdateUserAddress;
    String companyId = "1";
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String controller = "mobile/customer";

    String updateAddress = "/update/address"; // testing, uses company #1
    String updateAddressUrl = apiDomain + controller + updateAddress; // this is sent to the api
    SharedPreferences sharedPreferences;

    final long UPDATE_INTERVAL_IN_MILLISECONDS = 50000;
    LocationEngine locationEngine;

    NextbillionMap nextbillionMap;

    final NavigationLauncherLocationCallback callbackL =
            new NavigationLauncherLocationCallback(this);

    Point origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Nextbillion.getInstance(getApplicationContext(),"");
        setContentView(R.layout.activity_address_details);

        editTextStreet = findViewById(R.id.street);
        editTextSuite = findViewById(R.id.suite);
        editTextCity = findViewById(R.id.city);
        editTextState = findViewById(R.id.state);
        editTextPostalCode = findViewById(R.id.postalCode);
        editTextPhoneNumber = findViewById(R.id.phoneNumber);

        origin = Point.fromLngLat(-81.954109, 28.040510);
        sharedPreferences = getSharedPreferences("SASNM", MODE_PRIVATE);
        button = findViewById(R.id.btnAddressConfirm);

        mapView = findViewById(R.id.mapView);


        mapView.onCreate(savedInstanceState);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                if (!editTextStreet.getText().toString().equals("") &&
                    !editTextCity.getText().toString().equals("") &&
                    !editTextState.getText().toString().equals("") &&
                    !editTextPostalCode.getText().toString().equals("") &&
                    !editTextPhoneNumber.getText().toString().equals("")) {
                    urlUpdateUserAddress = updateAddressUrl + "/" + sharedPreferences.getString("userId", "");;
                    sendRequest(view, urlUpdateUserAddress);
                } else
                    Toast.makeText(AddressDetails.this,
                            "Enter address and latitude, longitude first",
                            Toast.LENGTH_SHORT).show();
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
                AddressDetails.this.nextbillionMap = nextbillionMap;
                String styleUri = "https://api.nextbillion.io/maps/streets/style.json?key="
                        + Nextbillion.getAccessKey();
                nextbillionMap.setStyle(new Style.Builder().fromUri(styleUri));
                nextbillionMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        initializeLocationEngine();

                        animateCamera(new LatLng(origin.latitude(), origin.longitude()));
                        nextbillionMap.addOnMapLongClickListener(new NextbillionMap.OnMapLongClickListener() {
                            @Override
                            public boolean onMapLongClick(@NonNull LatLng latLng) {
                                if(nextbillionMap.getMarkers().size() > 0){
                                    nextbillionMap.removeMarker(nextbillionMap.getMarkers().get(0));
                                }
                                nextbillionMap.addMarker(latLng);
                                latitude = String.valueOf(latLng.getLatitude());
                                longitude = String.valueOf(latLng.getLongitude());
                                return false;
                            }
                        });
                    }
                });
            }
        });
    }

    @NonNull
    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS).
                setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS).
                setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build();
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request = buildEngineRequest();
        locationEngine.requestLocationUpdates(request, callbackL, null);
        locationEngine.getLastLocation(callbackL);
    }

    private static class NavigationLauncherLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<AddressDetails> activityWeakReference;

        NavigationLauncherLocationCallback(AddressDetails activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            AddressDetails activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                activity.onLocationFound(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            exception.printStackTrace();
        }
    }

    void onLocationFound(Location location) {
        animateCamera(new LatLng(location.getLatitude(), location.getLongitude()));
    }


    private void animateCamera(LatLng point) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 15);
        nextbillionMap.easeCamera(cameraUpdate);
    }


    public void sendRequest(View v, String url) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        v.setEnabled(true);
                        if (response.equals("")) {
                            Toast.makeText(getApplicationContext(), "Address updated",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else
                            Toast.makeText(getApplicationContext(), "Address update failed",
                                    Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                v.setEnabled(true);
                error.printStackTrace();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();

                paramV.put("addressStreet", editTextStreet.getText().toString());
                paramV.put("addressSuite", editTextSuite.getText().toString());
                paramV.put("addressCity", editTextCity.getText().toString());
                paramV.put("addressState", editTextState.getText().toString());
                paramV.put("addressCountry", "USA");
                paramV.put("phoneNumber", editTextPhoneNumber.getText().toString());
                paramV.put("latitude", latitude);
                paramV.put("longitude", longitude);

                return paramV;
            }
        };
        queue.add(stringRequest);
    }
}