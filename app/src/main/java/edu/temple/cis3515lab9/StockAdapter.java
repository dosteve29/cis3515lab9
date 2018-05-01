package edu.temple.cis3515lab9;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StockAdapter extends BaseAdapter {
    private Context context;
    private JSONArray jsonArray;

    public StockAdapter(Context context, JSONArray jsonArray) {
        this.context = context;
        this.jsonArray = jsonArray;
    }

    @Override
    public int getCount() {
        return jsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = jsonArray.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        try {
            textView.setText(jsonArray.getJSONObject(position).getString("Symbol"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return textView;
    }

    public void updateJSONArray(JSONArray jsonArray){
        this.jsonArray = jsonArray;
    }
}
