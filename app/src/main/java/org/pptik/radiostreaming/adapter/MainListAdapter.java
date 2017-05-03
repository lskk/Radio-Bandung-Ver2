package org.pptik.radiostreaming.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.pptik.radiostreaming.database.DBManager;
import org.pptik.radiostreaming.model.StasiunRadio;
import org.pptik.radiostreaming.util.Radio;

public class MainListAdapter extends BaseAdapter {
    private Context mContext = null;

    private ArrayList<StasiunRadio> mRadio = null;

    private LayoutInflater mInflater = null;

    private DBManager mDBManager = null;

    public MainListAdapter(Context context, ArrayList<StasiunRadio> mRadio) {
        this.mContext = context;
        this.mRadio = mRadio;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        if (mRadio != null) {
            return mRadio.size();
        } else
            return 0;
    }

    @Override
    public Object getItem(int position) {

        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder", "NewApi"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final String ViewRadioName;
        final String viewRadioPath;
        final boolean info;
        ViewRadioName = mRadio.get(position).getNama();
        viewRadioPath = mRadio.get(position).getBroadcast_path();
        info = getInfo(ViewRadioName);
        convertView = mInflater.inflate(org.pptik.radiostreaming.R.layout.main_list_adapter, null);
        holder = new ViewHolder();
        holder.mLoveButton = (Button) convertView.findViewById(org.pptik.radiostreaming.R.id.mainLoveButton);
        holder.mDisloveButton = (Button) convertView.findViewById(org.pptik.radiostreaming.R.id.mainDisloveButton);
        holder.mTextView = (TextView) convertView.findViewById(org.pptik.radiostreaming.R.id.mainTextView);
        if (info == true) {
            holder.mDisloveButton.setVisibility(View.GONE);
            holder.mLoveButton.setVisibility(View.VISIBLE);
        }
        holder.mTextView.setText(ViewRadioName);
        holder.mLoveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                holder.mLoveButton.setVisibility(View.GONE);
                holder.mDisloveButton.setVisibility(View.VISIBLE);
                removeRadio(ViewRadioName);
            }
        });
        holder.mDisloveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                holder.mDisloveButton.setVisibility(View.GONE);
                holder.mLoveButton.setVisibility(View.VISIBLE);
                StasiunRadio stasiunRadio = new StasiunRadio();
                stasiunRadio.setNama(ViewRadioName);
                stasiunRadio.setBroadcast_path(viewRadioPath);
                stasiunRadio.setInfo(true);
                addRadio(stasiunRadio);

            }
        });

        return convertView;
    }

    private boolean getInfo(String name) {
        mDBManager = new DBManager(mContext);
        StasiunRadio radio = mDBManager.query(name);
        mDBManager.closeDB();
        return radio.isInfo();
    }

    private void removeRadio(String name) {
        mDBManager = new DBManager(mContext);
        mDBManager.remove(name);
        mDBManager.closeDB();
    }

    private void addRadio(StasiunRadio radio) {
        mDBManager = new DBManager(mContext);
        mDBManager.add(radio);
        mDBManager.closeDB();
    }

    private class ViewHolder {
        private TextView mTextView;

        private Button mLoveButton;

        private Button mDisloveButton;

    }

}
