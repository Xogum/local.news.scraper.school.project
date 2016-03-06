package com.thesis.ashline.localnewsscraper.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.model.Country;

import java.util.List;

/**
 * Created by ashfire on 2/1/15.
 */
public class CountrySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Activity activity;
    private final List<Country> countries;
    private LayoutInflater inflater;


    public CountrySpinnerAdapter(List<Country> countries, Activity activity) {
        super();
        this.activity = activity;
        this.countries = countries;
    }

    @Override
    public int getCount() {
        return countries.size();
    }

    @Override
    public Object getItem(int position) {
        return countries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.country_spinner_item, null);
        TextView txtCode = (TextView) convertView.findViewById(R.id.country_code);
        TextView txtName = (TextView) convertView.findViewById(R.id.country_name);
        Country country = countries.get(position);
        txtCode.setText(country.code);
        txtName.setText(country.name);
        return convertView;
    }
}
