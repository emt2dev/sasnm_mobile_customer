package com.example.sasnm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class orders extends AppCompatActivity {
    /*
     *
     * HERE OUR THE API END POINTS
     *
     * */
    // here we provide strings that will be concat'd together, makes it easier for future version updates
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String companyId = "1";
    String controllerMethod_getAllOrders = "orders/mobile/all/"; // testing, uses company #1
    String getAllOrdersUrl = apiDomain + controllerMethod_getAllOrders + companyId; // this is sent to the api
    String apiUrl;
    /*
     *
     *
     * */
    TextView textViewOrders;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        textViewOrders = findViewById(R.id.textOrders);
        sharedPreferences = getSharedPreferences("SASNM", MODE_PRIVATE);

        apiUrl = getAllOrdersUrl + "/" + sharedPreferences.getString("userId", "");

        textViewOrders.setMovementMethod(new ScrollingMovementMethod());
        fetchData(apiUrl);
    }

    public void parseJSON(String response){
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String id = obj.getString("id");
                Boolean delivery = obj.getBoolean("delivery");
                String status = obj.getString("status");
                String eta = obj.getString("eta");
                String method = obj.getString("method");
                String deliveryAddress = obj.getString("deliveryAddress");

                if(delivery)
                {
                    textViewOrders.append("Order Type: " + method + "\nStatus: " + status + "\nDelivery ETA: " + eta + "\nDelivery Address: " + deliveryAddress);
                } else {
                    textViewOrders.append("Order Type: " + method + "\nStatus: " + status + "\nPickup Time: " + eta);
                }

                textViewOrders.append("\n\n");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void fetchData(String apiUrl) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
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
        queue.add(stringRequest);
    }

}