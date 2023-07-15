package com.example.sasnm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /*
     *
     * HERE OUR THE API END POINTS
     *
     * */
    // here we provide strings that will be concat'd together, makes it easier for future version updates
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String companyId = "1";
    String controllerMethod_getProducts = "products/all/"; // testing, uses company #1
    String controllerMethod_getCustomerDetails = "customers/mobile/details"; // testing, uses company #1
    String allProductsUrl = apiDomain + controllerMethod_getProducts + companyId; // this is sent to the api
    String customerDetailsUrl = apiDomain + controllerMethod_getCustomerDetails;
    /*
     *
     *
     * */
    ArrayList<productDTO> productsList;
    SharedPreferences sharedPreferences;
    Button buttonCart, buttonAddress, buttonAllOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // preferences
        sharedPreferences = getSharedPreferences("SASNM", MODE_PRIVATE);

        // inits our variables
        productsList = new ArrayList<>();

        buttonCart = findViewById(R.id.btnCart);
        buttonAddress = findViewById(R.id.btnEnterAddress);
        buttonAllOrders = findViewById(R.id.btnAllOrders);

        checkPermissions(); // Here we determine if the device owner gave us internet permissions
        
        // here, if login is successful, we prevent the app from opening the login activity and go straight into the main activity
        if (sharedPreferences.getString("login", "false").equals("false"))
        {
            Intent intent = new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        }

        // here we get data
        fetchProducts();
        fetchCustomerDetails();

        buttonAllOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), orders.class);
                startActivity(intent);
            }
        });

        buttonAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddressDetails.class);
                startActivity(intent);
            }
        });

        buttonCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), cart.class);
                startActivity(intent);
            }
        });
    }

    public void fetchCustomerDetails() {
        // prepares the POST request
        RequestQueue queue = Volley.newRequestQueue(this);

        // this is the how the POST request will be handled
        String fullDetailsUrl = customerDetailsUrl + "/" + sharedPreferences.getString("userId", "");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, fullDetailsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseDetails(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(stringRequest); // this is the actual POST request
    }

    public void fetchProducts() {
        // prepares the POST request
        RequestQueue queue = Volley.newRequestQueue(this);

        // this is the how the POST request will be handled
        StringRequest stringRequest = new StringRequest(Request.Method.GET, allProductsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(stringRequest); // this is the actual POST request
    }

    public void parseDetails(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String userId = jsonObject.getString("userId");
            String addressStreet = jsonObject.getString("addressStreet");
            String addressSuite = jsonObject.getString("addressSuite");
            String addressCity = jsonObject.getString("addressCity");
            String addressState = jsonObject.getString("addressState");
            String addressPostal_code = jsonObject.getString("addressPostal_code");
            String addressCountry = jsonObject.getString("addressCountry");
            String PhoneNumber = jsonObject.getString("PhoneNumber");
            String longitude = jsonObject.getString("longitude");
            String latitude = jsonObject.getString("latitude");

                SharedPreferences.Editor myEdit = sharedPreferences.edit(); // stores the email
                myEdit.putString("addressStreet", addressStreet);
                myEdit.putString("addressSuite", addressSuite);
                myEdit.putString("addressCity", addressCity);
                myEdit.putString("addressState", addressState);
                myEdit.putString("addressPostal_code", addressPostal_code);
                myEdit.putString("addressCountry", addressCountry);
                myEdit.putString("PhoneNumber", PhoneNumber);
                myEdit.putString("longitude", longitude);
                myEdit.putString("latitude", latitude);
                myEdit.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parseJSON(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data); // receive json array of productDTOs and iterate through them, creating a dto and adding it to the array
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject incomingDTO = jsonArray.getJSONObject(i);
                String id = incomingDTO.getString("id");
                String name = incomingDTO.getString("name");
                String des = incomingDTO.getString("description");
                String image = incomingDTO.getString("image");
                String price = incomingDTO.getString("default_price");
                productsList.add(new productDTO(id, name, des, image, price));
            }
            // here we use the recycler view adapter class
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(productsList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Here we determine if the device owner gave us internet permissions
    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


}