package com.thesis.ashline.localnewsscraper.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.database.DB;


//todo http://hello-android.blogspot.com/2011/06/using-autocompletetextview-with-sqlite.html
public class CityListAdapter extends CursorAdapter {

    private DB dbAdapter = null;

    public CityListAdapter(Context context, Cursor c)
    {
        super(context, c);
        dbAdapter = new DB(context);
        dbAdapter.open();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        String item = createItem(cursor);
        ((TextView) view).setText(item);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(R.layout.autocomplete_item, parent, false);

        String item = createItem(cursor);
        view.setText(item);
        return view;
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint)
    {
        Cursor currentCursor = null;

        if (getFilterQueryProvider() != null)
        {
            return getFilterQueryProvider().runQuery(constraint);
        }

        String args = "";

        if (constraint != null)
        {
            args = constraint.toString();
        }

        currentCursor = dbAdapter.getCityCursor(args);

        return currentCursor;
    }

    private String createItem(Cursor cursor)
    {
        String item = cursor.getString(1);
        return item;
    }

    public void close()
    {
        dbAdapter.close();
    }
}