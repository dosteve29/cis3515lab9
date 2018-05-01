package edu.temple.cis3515lab9;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {
    OnStockSelectedListener mCallback;
    StockAdapter stockAdapter;
    ListView listView;
    TextView textView;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        listView = view.findViewById(R.id.stock_list);

        //read from file to create View
        File file = new File(getActivity().getFilesDir(), "myfile.json");
        if (file.exists()){
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                bufferedReader.close();
                stockAdapter = new StockAdapter(getContext(), new JSONArray(text.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{
            stockAdapter = new StockAdapter(getContext(), new JSONArray());
        }

        listView.setAdapter(stockAdapter);

        textView = view.findViewById(R.id.introHint);
        //hide the textView if there is a stock added
        if (stockAdapter.getCount() > 0){
            textView.setVisibility(View.GONE);
        } else{
            textView.setText(R.string.intro);
        }

        //Send which stock is selected to parent activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.OnStockSelectedListener(position);
            }
        });
        return view;
    }

    public interface OnStockSelectedListener{
        void OnStockSelectedListener(int position);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        //Check if the container activity has implemented the callback interface
        try{
            mCallback = (OnStockSelectedListener) activity;
        } catch(ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallback = null;
    }
}
