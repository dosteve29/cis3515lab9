package edu.temple.cis3515lab9;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockDetailsFragment extends Fragment {
    TextView companyName;
    TextView stockPrice;
    ImageView stockImage;
    TextView numberView;
    Bitmap bmp;
    View view;


    public StockDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stock_details, container, false);
        companyName = view.findViewById(R.id.companyName);
        stockPrice = view.findViewById(R.id.stockPrice);
        stockImage = view.findViewById(R.id.stockImage);

        Bundle args = getArguments();
        if (args != null){
            companyName.setText(args.getString("companyName"));
            String price = getString(R.string.price);
            stockPrice.setText(price.concat(" $").concat(args.getString("stockPrice")));
            getBitmap(args.getString("symbol"));
        }
        return view;
    }

    public void getBitmap(final String symbol){
        Thread thread = new Thread(){
            @Override
            public void run(){
                URL url;
                try{
                    url = new URL("http://www.google.com/finance/chart?q=" + symbol + "&p=1d"); // build url
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    connection.disconnect();

                    //send the object to a Handler
                    Message msg = Message.obtain();
                    msg.obj = bitmap;
                    imageResponseHandler.sendMessage(msg);

                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    Handler imageResponseHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            bmp = (Bitmap) msg.obj;
            stockImage.setImageBitmap(bmp);
            return false;
        }
    });
}
