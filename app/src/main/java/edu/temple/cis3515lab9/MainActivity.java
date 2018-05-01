package edu.temple.cis3515lab9;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements PortfolioFragment.OnStockSelectedListener{
    PortfolioFragment portfolioFragment;
    FragmentManager fragmentManager;

    String fileName = "myfile.json";
    File file;

    MyService myService;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        file = new File(getFilesDir(), fileName);

        //add the fragment to the container. fragContainer is present in all layouts
        fragmentManager = getSupportFragmentManager();

        //this is the fragment that holds the list of stocks
        portfolioFragment = new PortfolioFragment();
        fragmentManager.beginTransaction().add(R.id.fragContainer, portfolioFragment).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.TestBinder binder = (MyService.TestBinder) service;
            myService = binder.getService();
            myService.doSomething(ServiceHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    Handler ServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            StockDetailsFragment stockDetailsFragment;
            if ((stockDetailsFragment = (StockDetailsFragment) fragmentManager.findFragmentByTag("Details")) != null){
                JSONArray jsonArray = (JSONArray) msg.obj;
                try {
                    String newPrice = getString(R.string.price).concat(jsonArray.getJSONObject(position).getString("LastPrice"));
                    stockDetailsFragment.stockPrice.setText(newPrice);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    });

    //create the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.search_stock); //this is the search icon menu item
        final SearchView searchView = (SearchView) searchItem.getActionView(); //the action of search icon

        //set callback method for searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) { //when the user submits the text
                //create new thread for network operation
                Thread thread = new Thread(){
                    @Override
                    public void run(){
                        URL url;
                        try{
                            url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + query); // build url
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                            String response = "", tmpResponse;
                            tmpResponse = bufferedReader.readLine(); //get line from input stream
                            while(tmpResponse != null){ //keep reading until null
                                response = response + tmpResponse;
                                tmpResponse = bufferedReader.readLine();
                            }
                            JSONObject stockObject = new JSONObject(response); //create JSON object from lines read

                            //send the object to a Handler
                            Message msg = Message.obtain();
                            msg.obj = stockObject;
                            stockResponseHandler.sendMessage(msg);

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                //hide the keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                //collapse the menu
                searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    Handler stockResponseHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            //this is the json object passed to the handler
            JSONObject responseObject = (JSONObject) msg.obj;
            if (!responseObject.has("Name")){
                Toast.makeText(MainActivity.this, "No Stock found", Toast.LENGTH_SHORT).show();
                return false;
            }
            JSONArray jsonArray = null;

            //check if file is empty or not
            if (file.exists()){
                //get the written stuff parsed into JSONArray
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    StringBuilder text = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    bufferedReader.close();
                    jsonArray = new JSONArray(text.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else{
                //if file is empty, just create new JSONArray
                jsonArray = new JSONArray();
            }

            //Put the new stock in
            jsonArray.put(responseObject);

            //write the Array to the file
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(jsonArray.toString().getBytes());
                fileOutputStream.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            portfolioFragment.stockAdapter.updateJSONArray(jsonArray);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    portfolioFragment.stockAdapter.notifyDataSetChanged();
                    portfolioFragment.textView.setVisibility(View.GONE);
                }
            });
            return false;
        }
    });

    @Override
    public void OnStockSelectedListener(int position){
        this.position = position;
        JSONArray jsonArray = null;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            bufferedReader.close();
            jsonArray = new JSONArray(text.toString());

            StockDetailsFragment stockDetailsFragment = new StockDetailsFragment();
            Bundle args = new Bundle();
            args.putString("companyName", jsonArray.getJSONObject(position).getString("Name"));
            args.putString("stockPrice", jsonArray.getJSONObject(position).getString("LastPrice"));
            args.putString("symbol", jsonArray.getJSONObject(position).getString("Symbol"));
            args.putString("updated", jsonArray.getJSONObject(position).getString("Timestamp"));
            stockDetailsFragment.setArguments(args);

            if (findViewById(R.id.detailsContainer) != null){
                fragmentManager.beginTransaction().replace(R.id.detailsContainer, stockDetailsFragment, "Details").commit();
            } else{
                fragmentManager.beginTransaction().replace(R.id.fragContainer, stockDetailsFragment, "Details").addToBackStack(null).commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
