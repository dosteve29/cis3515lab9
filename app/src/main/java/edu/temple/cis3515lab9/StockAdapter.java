package edu.temple.cis3515lab9;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StockAdapter extends BaseAdapter {
    private Context context;
    private String jsonString;

    public StockAdapter(Context context, String jsonString) {
        this.context = context;
        this.jsonString = jsonString;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
