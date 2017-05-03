package org.pptik.radiostreaming.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.pptik.radiostreaming.model.StasiunRadio;
import org.pptik.radiostreaming.util.Radio;

public class DBManager
{
    private DBHelper helper;
    
    private SQLiteDatabase db = null;
    
    public DBManager(Context context)
    {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }
    
    public void add(StasiunRadio radio)
    {
        db.beginTransaction();
        try
        {
            db.execSQL("insert into radiolist values(null,?,?,?)", new Object[] {radio.getNama(), radio.getBroadcast_path(),
                radio.isInfo()});
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        
    }
    
    public void remove(String name)
    {
        
        db.delete("radiolist", "name= ?", new String[] {name});
        
    }
    
    public void updateInfo(String name, boolean info)
    {
        
        db.beginTransaction();
        try
        {
            ContentValues cv = new ContentValues();
            cv.put("info", info);
            db.update("radiolist", cv, "name=?", new String[] {name});
        }
        finally
        {
            db.endTransaction();
        }
    }
    
    public StasiunRadio query(String name)
    {
        StasiunRadio radio = new StasiunRadio();
        Cursor c = db.rawQuery("select * from radiolist where name=?", new String[] {name});
        while (c.moveToNext())
        {
            radio.setNama(c.getString(c.getColumnIndex("name")));
            radio.setBroadcast_path(c.getString(c.getColumnIndex("path")));
            radio.setInfo(c.getInt(c.getColumnIndex("info")) == 0 ? false : true);
        }
        c.close();
        return radio;
        
    }
    
    public ArrayList<StasiunRadio> queryAll()
    {
        ArrayList<StasiunRadio> radios = new ArrayList<StasiunRadio>();
        Cursor c = db.rawQuery("select * from radiolist", null);
        while (c.moveToNext())
        {
            StasiunRadio radio = new StasiunRadio();
            radio.setNama(c.getString(c.getColumnIndex("name")));
            radio.setBroadcast_path(c.getString(c.getColumnIndex("path")));
            radio.setInfo(c.getInt(c.getColumnIndex("info")) == 0 ? false : true);
            radios.add(radio);
        }
        c.close();
        return radios;
        
    }
    
    public void closeDB()
    {
        db.close();
    }
}
