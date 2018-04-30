package edu.temple.cis3515lab9;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

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

public class MainActivity extends AppCompatActivity {
    PortfolioFragment portfolioFragment;
    StockDetailsFragment stockDetailsFragment;

    String fileName = "myfile.json";
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        file = new File(getFilesDir(), fileName);

        //add the fragment to the container. fragContainer is present in all layouts
        FragmentManager fragmentManager = getSupportFragmentManager();

        //this is the fragment that holds the list of stocks
        portfolioFragment = new PortfolioFragment();
        fragmentManager.beginTransaction().add(R.id.fragContainer, portfolioFragment).commit();

        //detailsContainer may not be always there.
        if (findViewById(R.id.detailsContainer) != null){
            stockDetailsFragment = new StockDetailsFragment();
            fragmentManager.beginTransaction().add(R.id.detailsContainer, stockDetailsFragment).commit();
        }
    }


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
//                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show(); //TESTING SHOW THE INPUT STRING
//                "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" //the api address

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
            return false;
        }
    });
}
