package org.pptik.radiostreaming.adapter;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.apache.http.Header;
import org.pptik.radiostreaming.R;
import org.pptik.radiostreaming.model.Acara;
import org.pptik.radiostreaming.util.DownloadRadioTask;
import org.pptik.radiostreaming.util.RadioClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Hafid on 4/21/2017.
 * Adapter untuk ListView acara channel radio
 */

public class AcaraAdapter extends BaseAdapter {
    private static String TAG = "[AcaraAdapter]";
    private ArrayList<Acara> acaraArrayList;
    private Context context;
    private boolean isAnyDownload[];
    private boolean isAnyPlay[];

    public AcaraAdapter(ArrayList<Acara> acaraArrayList, Context context) {
        this.acaraArrayList = acaraArrayList;
        this.context = context;
        this.isAnyDownload = new boolean[acaraArrayList.size()];
        this.isAnyPlay = new boolean[acaraArrayList.size()];
        for (int i = 0; i < acaraArrayList.size(); i++) {
            isAnyPlay[i] = false;
            isAnyDownload[i] = false;
        }
    }

    @Override
    public int getCount() {
        return acaraArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean checkIfAnyPlayProgress() {
        boolean check = false;
        for (int i = 0; i < acaraArrayList.size(); i++) {
            if (isAnyPlay[i]) {
                check = true;
            }
        }
        if (check)
            Toast.makeText(context, "Berhentikan seluruh playlist yang sedang dimainkan atau dalam proses streaming!", Toast.LENGTH_LONG).show();
        return check;
    }

    public boolean checkIfAnyDownloadProgress() {
        boolean check = false;
        for (int i = 0; i < acaraArrayList.size(); i++) {
            if (isAnyDownload[i]) {
                check = true;
            }
        }
        if (check)
            Toast.makeText(context, "Berhentikan seluruh aktivitas download terlebih dahulu!", Toast.LENGTH_LONG).show();
        return check;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Acara acara = acaraArrayList.get(position);
        Log.i(TAG, "Size : " + acaraArrayList.size() + " " + position + " " + acara.getNama());
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_acara, parent, false);
        final MyViewHolder holder = new MyViewHolder(convertView);
        holder.name.setText(acara.getNama());
        holder.tanggal.setText(acara.getTanggal());
        holder.waktu.setText(acara.getWaktu());

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (acara.getPlayer()==null){
                        acara.setPlayer(new MediaPlayer());
                        acara.getPlayer().reset();
                        acara.getPlayer().setDataSource(acara.getRadio_url());
                        acara.getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
                        acara.getPlayer().prepare();
                        holder.playIconOn();
                    }else{
                        if (acara.getPlayer().isPlaying()){
                            acara.getPlayer().pause();
                            holder.playIconOn();
                        }else{
                            acara.getPlayer().start();
                            holder.pauseIconOn();
                        }
                    }
                    isAnyPlay[position] = true;
                } catch (IOException e) {
                    Log.e(TAG, "Preparing " + acara.getRadio_url());
                }
            }
        });

        holder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acara.getPlayer().stop();
                acara.getPlayer().release();
                acara.setPlayer(null);
                holder.syncIconOn();
                isAnyPlay[position] = false;
            }
        });

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (acara.getDownloader() != null && acara.getDownloader().getStatus() != AsyncTask.Status.FINISHED) {
                    Log.i(TAG, "Download canceled now..");
                    acara.getDownloader().cancel(true);
                } else {
                    acara.setDownloader(new DownloadRadioTask(context, acara, holder.download_prog, holder.download_size, holder.download));
                    acara.getDownloader().execute(acara.getDownload_url());
                    Log.i(TAG, "Download starting now..");
                }
                isAnyDownload[position] = !acara.getDownloader().isCancelled();
            }
        });
        return convertView;
    }

    /**
     * ViewHolder untuk acara
     * 1. nama acara
     * 2. tanggal acara
     * 3. tombol prepare/play/pause
     * 4. tombol stop
     * 5. tombol download
     * 6. progress bar download
     * 7. ukuran sisa file yang akan didownload
     */

    public class MyViewHolder extends View {
        public TextView name;
        public TextView tanggal;
        public TextView waktu;
        public ImageButton play;
        public ImageButton download;
        public ImageButton stop;
        public ProgressBar download_prog;
        public boolean isDownloading;
        public boolean isStream;
        public boolean isPlay;
        public boolean isSync;
        public boolean isPause;
        public ImageView prof_pic;
        public TextView download_size;
        public boolean firstDownload_click = true;

        public MyViewHolder(View view) {
            super(view.getContext());
            name = (TextView) view.findViewById(R.id.tx_name);
            tanggal = (TextView) view.findViewById(R.id.tx_tanggal);
            waktu = (TextView) view.findViewById(R.id.tx_waktu);
            play = (ImageButton) view.findViewById(R.id.id_play);
            download = (ImageButton) view.findViewById(R.id.id_download);
            download_size = (TextView) view.findViewById(R.id.id_download_size);
            stop = (ImageButton) view.findViewById(R.id.id_stop);
            download_prog = (ProgressBar) view.findViewById(R.id.id_download_progress);
            download_prog.setVisibility(View.GONE);
            download_size.setVisibility(View.GONE);
            isDownloading = false;
            isStream = false;
            isPlay = false;
            isSync = false;
            isPause = false;
            syncIconOn();
        }

        public void syncIconOn() {
            play.setImageResource(R.drawable.ic_action_syncdata);
            play.setEnabled(true);
            stop.setEnabled(false);
        }

        public void playIconOn() {
            play.setImageResource(R.drawable.ic_action_play);
            play.setEnabled(true);
            stop.setEnabled(true);
        }

        public void pauseIconOn() {
            play.setImageResource(R.drawable.ic_action_pause);
            play.setEnabled(true);
            stop.setEnabled(true);
        }
    }
}
