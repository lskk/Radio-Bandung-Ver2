package org.pptik.radiostreaming.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.pptik.radiostreaming.R;
import org.pptik.radiostreaming.model.StasiunRadio;

import java.util.ArrayList;

/**
 * Created by Hafid on 4/21/2017.
 * Adapter untuk spinner Stasiun Radio
 *
 */

public class CustomSpinnerAdapter extends ArrayAdapter<StasiunRadio> {
    public static String tag = "[CustomSpinnerAdapter]";
    private Context context;
    private ArrayList<StasiunRadio> stasiunRadios;
    private StasiunRadio stasiunRadio;
    private LayoutInflater inflater;

    public CustomSpinnerAdapter(Context context, int textViewResourceId, ArrayList<StasiunRadio> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        stasiunRadios = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_stasiun_radio, null, false); //view layout diganti dengan layout list_stasiun_radio
        stasiunRadio = null;
        stasiunRadio = (StasiunRadio) stasiunRadios.get(i);
        TextView nama = (TextView) view.findViewById(R.id.textView);
        TextView alamat = (TextView) view.findViewById(R.id.subTextView);
        nama.setText(stasiunRadios.get(i).getNama());
        alamat.setText(stasiunRadios.get(i).getAlamat());
        return view;
    }

    @Override
    public int getCount() {
        return stasiunRadios.size();
    }

    /*
    public CustomSpinnerAdapter(Context applicationContext, ArrayList<StasiunRadio> stasiunRadios) {
        Log.i(tag," Constructor");
        this.context = applicationContext;
        this.stasiunRadios = stasiunRadios;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return stasiunRadios.size();
    }

    @Override
    public Object getItem(int i){
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.i(tag, " getView");
        view = inflter.inflate(R.layout.list_statsiun_radio, null);
        //ImageView icon = (ImageView) view.findViewById(R.id.imageView);
        TextView names = (TextView) view.findViewById(R.id.textView);
        //icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_radio_station));
        names.setText(stasiunRadios.get(i).getNama());
        return view;
    }
    */
}