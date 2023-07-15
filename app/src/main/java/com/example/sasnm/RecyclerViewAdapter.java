package com.example.sasnm;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    /*
     *
     * HERE OUR THE API END POINTS
     *
     * */
    // here we provide strings that will be concat'd together, makes it easier for future version updates
    String apiDomain = "http://10.0.2.2:5035/api/v2/";
    String controller = "mobile/customer/";
    String addToCart = "add";
    String removeFromCart = "remove";
    String emptyCart = "empty";
    String addUrl = apiDomain + controller + addToCart; // this is sent to the api
    String removeUrl = apiDomain + controller + removeFromCart; // this is sent to the api
    String emptyUrl = apiDomain + controller + emptyCart; // this is sent to the api
    String UrlBuilder;
    /*
    *
    *
    * */

    ArrayList<productDTO> productDTOList; // array of type productDTO
    Context context;

    SharedPreferences sharedPreferences;

    // here is the constructor for this class
    public RecyclerViewAdapter(ArrayList<productDTO> productDTOList) {
        this.productDTOList = productDTOList;
    }

    // this is where
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedPreferences = context.getSharedPreferences("SASNM", MODE_PRIVATE);
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.product_list_item, parent, false); // parses the product DTO and displays it
        return new ViewHolder(listItem); // displays the productDTO
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        productDTO list = productDTOList.get(position); // index of the product
        holder.textViewTitle.setText(list.getName() + " - $" + list.getPrice()); // displays the Title and price
        holder.textViewDes.setText(list.getDes()); // displays product description
        Picasso.get().load(list.getImg()).into(holder.imageView); // displays product image
        holder.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // once clicked, the post request is sent to the api to add one to cart
                v.setEnabled(false); // disables button to prevent additional requests.
                String UrlBuilder = addUrl+ "/" + list.getId() + "/" + sharedPreferences.getString("userId", "");
                sendRequest(v, UrlBuilder);
            }
        });

        holder.buttonRemove.setOnClickListener(new View.OnClickListener() {
            // once clicked, the post request is sent to the api to remove one from cart
            @Override
            public void onClick(View v) {
                v.setEnabled(false); // disables button to prevent additional requests.
                String UrlBuilder = removeUrl + "/" + list.getId() + "/" + sharedPreferences.getString("userId", "");
                sendRequest(v, UrlBuilder);
            }
        });


    }


    @Override
    public int getItemCount() {
        return productDTOList.toArray().length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // properties of the ViewHolder class
        public ImageView imageView; // displays image
        public ImageButton buttonAdd, buttonRemove, buttonEmpty; // displays buttons
        public TextView textViewTitle, textViewDes; // displays title and description
        public CardView cardView; // displays in card layout

        int numItem = 0;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.imageView); // sets property using class passed as argument in the constructor
            this.textViewTitle = itemView.findViewById(R.id.textViewTitle);
            this.textViewDes = itemView.findViewById(R.id.textViewDes);
            this.buttonAdd = itemView.findViewById(R.id.add_item);
            this.buttonRemove = itemView.findViewById(R.id.remove_item);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    // this is the method that is used to send api requests
    public void sendRequest(View v, String apiUrl) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                v.setEnabled(true);
                if (!response.equals("")) {
                    Toast.makeText(context, "Operation success", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show();
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
}