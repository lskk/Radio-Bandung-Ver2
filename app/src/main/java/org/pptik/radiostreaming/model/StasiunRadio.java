package org.pptik.radiostreaming.model;

/**
 * Created by Hafid on 4/21/2017.
 */

public class StasiunRadio {
    private int id;
    private String nama;
    private String alamat;
    private String broadcast_path;
    private String about;
    private boolean info;

    public StasiunRadio(){}

    public StasiunRadio(int id, String nama){
        this.id = id;
        this.nama = nama;
    }

    public StasiunRadio(int id, String nama, String alamat){
        this.id = id;
        this.nama = nama;
        this.alamat = alamat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getBroadcast_path() {
        return broadcast_path;
    }

    public void setBroadcast_path(String broadcast_path) {
        this.broadcast_path = broadcast_path;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public boolean isInfo() {
        return info;
    }

    public void setInfo(boolean info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "Entity : "+this.getNama()+", "+this.getBroadcast_path();
    }
}
