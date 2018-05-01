package edu.temple.cis3515lab9;

import android.app.FragmentManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
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

public class MyService extends Service {
    IBinder binder = new TestBinder();
    Thread thread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public MyService(){

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class TestBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }

    public void doSomething(final Handler handler){
        final Handler repeatingHandler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    final File file = new File(getFilesDir(), "myfile.json");
                    JSONArray jsonArray = null;
                    if (file.exists()){
                        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                            StringBuilder text = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            bufferedReader.close();
                            jsonArray = new JSONArray(text.toString());
                            final JSONArray finalJsonArray = jsonArray;

                            thread = new Thread(){
                            @Override
                            public void run(){
                                for (int i = 0; i < finalJsonArray.length(); i++){
                                    try {
                                        JSONObject jsonObject = finalJsonArray.getJSONObject(i);
                                        String symbol = jsonObject.getString("Symbol");
                                        URL url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol);
                                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                                        String response = "", tmpResponse;
                                        tmpResponse = bufferedReader.readLine(); //get line from input stream
                                        while(tmpResponse != null){ //keep reading until null
                                            response = response + tmpResponse;
                                            tmpResponse = bufferedReader.readLine();
                                        }
                                        JSONObject stockObject = new JSONObject(response); //create JSON object from lines read
                                        finalJsonArray.put(i, stockObject);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                FileOutputStream fileOutputStream = null;
                                try {
                                    fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(finalJsonArray.toString().getBytes());
                                    fileOutputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                        Message msg = Message.obtain();
                        msg.obj = finalJsonArray;
                        handler.sendMessage(msg);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }

                repeatingHandler.postDelayed(this, 30 * 1000);
            }
        }, 0);
    }


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() { //handle service every 30 sec
//            @Override
//            public void run() {
//                final File file = new File(getFilesDir(), "myfile.json");
//                JSONArray jsonArray = null;
//                if (file.exists()){
//                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
//                        StringBuilder text = new StringBuilder();
//                        String line;
//                        while ((line = bufferedReader.readLine()) != null) {
//                            text.append(line);
//                            text.append('\n');
//                        }
//                        bufferedReader.close();
//                        jsonArray = new JSONArray(text.toString());
//                        final JSONArray finalJsonArray = jsonArray;
//
//                        //update the file here using Threads
//                        thread = new Thread(){
//                            @Override
//                            public void run(){
//                                for (int i = 0; i < finalJsonArray.length(); i++){
//                                    try {
//                                        JSONObject jsonObject = finalJsonArray.getJSONObject(i);
//                                        String symbol = jsonObject.getString("Symbol");
//                                        URL url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol);
//                                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
//                                        String response = "", tmpResponse;
//                                        tmpResponse = bufferedReader.readLine(); //get line from input stream
//                                        while(tmpResponse != null){ //keep reading until null
//                                            response = response + tmpResponse;
//                                            tmpResponse = bufferedReader.readLine();
//                                        }
//                                        JSONObject stockObject = new JSONObject(response); //create JSON object from lines read
//                                        finalJsonArray.put(i, stockObject);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                                FileOutputStream fileOutputStream = null;
//                                try {
//                                    fileOutputStream = new FileOutputStream(file);
//                                    fileOutputStream.write(finalJsonArray.toString().getBytes());
//                                    fileOutputStream.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//
//                            }
//                        };
//                        thread.start();
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }, 60 * 1000); //run every 30 seconds
//        return START_STICKY;
//    }
}
