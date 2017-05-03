package org.pptik.radiostreaming.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.pptik.radiostreaming.R;
import org.pptik.radiostreaming.model.BaseMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hafid on 4/28/2017.
 */

public class BaseMenuAdapter extends ArrayAdapter<BaseMenu> {
    public static String TAG = "[BaseMenuAdapter]";

    private ArrayList<BaseMenu> menu;
    private int layoutResourceId;
    private Context context;

    public BaseMenuAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<BaseMenu> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.menu = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BaseMenu bsmenu = menu.get(position);
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_base_menu, parent, false);
        final BaseMenuViewHolder holder = new BaseMenuViewHolder(convertView);
        holder.icon.setImageResource(bsmenu.getIcon());
        holder.menuText.setText(bsmenu.getMenuText());
        return convertView;
    }

    public class BaseMenuViewHolder extends View {
        public ImageView icon;
        public TextView menuText;
        public BaseMenuViewHolder(View view) {
            super(view.getContext());
            icon = (ImageView)view.findViewById(R.id.imgIcon);
            menuText = (TextView)view.findViewById(R.id.txtMenu);
        }
    }
}
