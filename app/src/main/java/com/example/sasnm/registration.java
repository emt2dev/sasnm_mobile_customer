package com.example.sasnm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class registration extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    String email, password;
    TextView textViewError, textViewLogin;

    Button buttonSubmit;
    ProgressBar progressBar;
    // here we provide strings that will be concat'd together, makes it easier for future version updates
//    String apiDomain = "http://192.168.1.177:5035/api/v2/";
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String controllerMethod = "mobile/customer/register";
    String url = apiDomain + controllerMethod; // this is sent to the api

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // init user entered variables
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        // init view items
        textViewError = findViewById(R.id.error);
        textViewLogin = findViewById(R.id.loginNow);
        buttonSubmit = findViewById(R.id.submit);
        progressBar = findViewById(R.id.loading);

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        // here we get all the data from the form and send it to rest api
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE); // shows the progress bar
                textViewError.setVisibility(View.GONE); // removes errors

                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                // front-end validation
                if(email.equals("")) {
                    Toast.makeText(registration.this, "email is required", Toast.LENGTH_SHORT).show();
                }

                if(password.equals("")) {
                    Toast.makeText(registration.this, "password is required", Toast.LENGTH_SHORT).show();
                }

                if(email.equals("") && password.equals("")) {
                    Toast.makeText(registration.this, "email and password are required", Toast.LENGTH_SHORT).show();
                }

                // here we begin creating the post request.
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                // this is the how the post request will be handled
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE); // removes progress bar once the request is sent
                                if (response.equals("success")) {
                                    // go to main activity if success
                                    Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                } else
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> paramV = new HashMap<>();

                        paramV.put("Email", email.toString());// passed in the POST request
                        paramV.put("Password", password.toString());// passed in the POST request
                        return paramV;
                    }
                };
                queue.add(stringRequest); // this is the actual POST request

            }
        });
    }
}