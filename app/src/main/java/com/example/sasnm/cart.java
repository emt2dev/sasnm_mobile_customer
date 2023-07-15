package com.example.sasnm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class cart extends AppCompatActivity {

    TextView textViewCartData;
    SharedPreferences sharedPreferences;
    Button buttonConfirm, buttonRemove, buttonEmpty;

    int pricePerKM = 5;
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String companyId = "1";
    String controller = "mobile/customer";
    String existingCart = "existing/";
    String emptyCart = "empty/";
    String deliveryOrder = "submit/delivery/";
    String takeoutOrder = "submit/takeout/";
    String urlDeliveryOrder =
            apiDomain + controller + "/" + deliveryOrder + companyId;
    String urlTakeoutOrder =
            apiDomain + controller + "/" + takeoutOrder + companyId;
    String urlGetCart =
            apiDomain + controller + "/" + existingCart + companyId;
    String urlEmptyCart =
            apiDomain + controller + "/" + emptyCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        textViewCartData = findViewById(R.id.textCartData);
        sharedPreferences = getSharedPreferences("SASNM", MODE_PRIVATE);
        buttonConfirm = findViewById(R.id.btnConfirmOrder);
        buttonRemove = findViewById(R.id.btnClearCart);
        buttonEmpty = findViewById(R.id.btnEmptyCart);
        fetchData();
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRequest(view, urlDeliveryOrder + "/" + sharedPreferences.getString("userId", ""));
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest(view, urlTakeoutOrder + "/" + sharedPreferences.getString("userId", ""));
            }
        });

        buttonEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest(view, urlEmptyCart + "/" + sharedPreferences.getString("cartId", ""));
            }
        });

    }

    public void sendRequest(View v, String apiUrl) {
        Log.e("url", apiUrl);
        v.setEnabled(false);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                v.setEnabled(true);
                if (!response.equals("")) {
                    Toast.makeText(getApplicationContext(), "Operation success", Toast.LENGTH_SHORT).show();
                    finish();
                } else
                    Toast.makeText(getApplicationContext(), "Operation failed", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                v.setEnabled(true);
                error.printStackTrace();
            }
        });
        queue.add(stringRequest);
    }

    public void parseJSON(String data) {
        try {
            JSONObject obj = new JSONObject(data);
            String cost = obj.getString("cost");

            // here we save the cart id
            SharedPreferences.Editor myEdit = sharedPreferences.edit(); // stores the email
            myEdit.putString("cartId", obj.getString("id"));
            myEdit.apply();

            textViewCartData.append("$" + cost);
            JSONArray Items = obj.getJSONArray("Items");

            for (int i = 0; i < Items.length(); i++) {
                JSONObject stu = Items.getJSONObject(i);
                String id = stu.getString("id");
                String name = stu.getString("name");
                String description = stu.getString("description");
                String quantity = stu.getString("quantity");
                String default_price = stu.getString("default_price");
                textViewCartData.append("\nName " + name + "\nCost: $" + cost + " each\nQuantity: " + quantity);
            }

        } catch (JSONException e) {
            Log.e("error", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void fetchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String getExistingCartUrl = urlGetCart + "/" + sharedPreferences.getString("userId", "");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getExistingCartUrl, new Response.Listener<String>() {
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