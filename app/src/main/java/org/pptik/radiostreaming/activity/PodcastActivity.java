package org.pptik.radiostreaming.activity;

import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pptik.radiostreaming.R;
import org.pptik.radiostreaming.adapter.CustomSpinnerAdapter;
import org.pptik.radiostreaming.customui.DateDialog;
import org.pptik.radiostreaming.model.Acara;
import org.pptik.radiostreaming.adapter.AcaraAdapter;
import org.pptik.radiostreaming.model.StasiunRadio;
import org.pptik.radiostreaming.util.ConfigUrl;
import org.pptik.radiostreaming.util.RadioClient;
import org.pptik.radiostreaming.view.ExitDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.vov.vitamio.LibsChecker;

/**
 * PodcastActivity -----------------------------------------------------------------------
 * Podcast streaming ataupun download file untuk acara radio yang pernah tayang sebelumnya
 * Cara kerja:
 * 1. Activity diaktifkan
 * 2. List stasiun radio dipersiapkan (Membutuhkan koneksi internet)
 * 3. Pilih tanggal acara yang telah tayang
 * 4. List acara untuk stasiun radio yang dipilih pada tanggal yang dipilih dipersiapkan
 *   (Membutuhkan koneksi internet)
 * 5. List acara dimunculkan dalam ListView beserta custom action untuk setiap listnya
 *   (prepare, play, pause, stop, download)
 */

public class PodcastActivity extends Activity implements View.OnClickListener{
    public static String TAG = "[PodcastActivity]";

    @BindView(R.id.id_list_stasiun_radio)
    Spinner spinnerStasiunRadio;
    @BindView(R.id.id_select_date)
    ImageButton selectDate;
    @BindView(R.id.tanggal_radio)
    EditText textSelectTanggal;
    @BindView(R.id.list_view)
    ListView listAcaraView;
    @BindView(R.id.PodcastActivityBack)
    Button backBtn;

    private ArrayList<StasiunRadio> stasiunRadioArrayList = new ArrayList<>();
    private ArrayList<Acara> acaraArrayList = new ArrayList<>();
    private CustomSpinnerAdapter radioSpinnerAdapter;
    private StasiunRadio selectedSpinner = new StasiunRadio();
    private AcaraAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_podcast);
        prepareStasiunRadio();
        ButterKnife.bind(this);
        radioSpinnerAdapter = new CustomSpinnerAdapter(getApplicationContext(), R.layout.list_stasiun_radio, stasiunRadioArrayList);
        spinnerStasiunRadio.setAdapter(radioSpinnerAdapter);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DateDialog dialog = new DateDialog(textSelectTanggal);
                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth();
                        int year =  datePicker.getYear();

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
                        String date = dateFormat.format(calendar.getTime());

                        // List Channel hanya akan ditampilkan jika jumlah stasiun radio yang berhasil di list pada server lebih dari 0
                        if (stasiunRadioArrayList.size() > 0) {
                            selectedSpinner = stasiunRadioArrayList.get(spinnerStasiunRadio.getSelectedItemPosition());
                            if (!mAdapter.checkIfAnyPlayProgress() && !mAdapter.checkIfAnyDownloadProgress()) {
                                dialog.setDate(i, i1+1, i2);
                                prepareChannelAcara(selectedSpinner, date);
                            }
                        }
                    }
                });
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                dialog.show(ft, "Tanggal Siaran");
            }
        });
        mAdapter = new AcaraAdapter(acaraArrayList, getApplicationContext());
        listAcaraView.setAdapter(mAdapter);
        backBtn.setOnClickListener(this);
    }

    /**
     * Aksi ketika Exit dialog muncul dan dipilih
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (!mAdapter.checkIfAnyPlayProgress() && !mAdapter.checkIfAnyDownloadProgress()) {
                ExitDialog exitDialog = new ExitDialog(this, R.style.MyStyle);
                exitDialog.show();
                return false;
            } else {
                return false;
            }
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }

    }

    /**
     * Menyiapkan data stasiun radio untuk dimasukkan ke spinner podcast activity
     */
    public void prepareStasiunRadio() {
        RadioClient.get(this, ConfigUrl.radioList, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG,"Object "+response.toString());
                stasiunRadioArrayList.clear();
                try {
                    JSONArray array = response.getJSONArray("radio");
                    for (int i = 0; i < array.length(); i++) {
                        StasiunRadio sts = new StasiunRadio();
                        sts.setId(array.getJSONObject(i).getInt("id"));
                        sts.setNama(array.getJSONObject(i).getString("name"));
                        sts.setAlamat(array.getJSONObject(i).getString("address"));
                        stasiunRadioArrayList.add(sts);
                        Log.i(TAG," Sts "+sts.getNama());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                radioSpinnerAdapter = new CustomSpinnerAdapter(getApplicationContext(), R.layout.list_stasiun_radio, stasiunRadioArrayList);
                spinnerStasiunRadio.setAdapter(radioSpinnerAdapter);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(),"Koneksi time-out!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(),"Koneksi time-out!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(),"Koneksi time-out!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Mempersiapkan channel acara radio berdasarkan tanggal dan stasiun radio yang telah dipilih
     * melalui spinner
     * @param stRadio
     * @param tanggal
     */
    public void prepareChannelAcara(final StasiunRadio stRadio, String tanggal){
        String requested = ConfigUrl.channelList+stRadio.getId()+"/"+tanggal;
        RadioClient.get(this, requested, new JsonHttpResponseHandler(){
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                acaraArrayList.clear();
                mAdapter = new AcaraAdapter(acaraArrayList, getApplicationContext());
                listAcaraView.setAdapter(mAdapter);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(),"Koneksi time-out!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(),"Koneksi time-out!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                acaraArrayList.clear();
                mAdapter = new AcaraAdapter(acaraArrayList, getApplicationContext());
                listAcaraView.setAdapter(mAdapter);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(),"Koneksi time-out!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                acaraArrayList.clear();
                try {
                    if (response.getBoolean("status")) {
                        JSONArray array = response.getJSONArray("channel");
                        for (int i = 0; i < array.length(); i++) {
                            Acara acr = new Acara();
                            acr.setId(array.getJSONObject(i).getInt("id"));
                            acr.setNama(array.getJSONObject(i).getString("nameChannel"));
                            acr.setTanggal(array.getJSONObject(i).getString("date"));
                            acr.setWaktu(array.getJSONObject(i).getString("time"));
                            acr.setFileName(array.getJSONObject(i).getString("nameFile"));
                            acr.setDownload_url(ConfigUrl.BASE_URL + ConfigUrl.downloadChannel + acr.getId());
                            acr.setRadio_url(ConfigUrl.BASE_URL_MIN + array.getJSONObject(i).getString("file_path"));
                            //acr.setRadio_url(ConfigUrl.BASE_URL+ConfigUrl.streamingChannel+stRadio.getNama()+"/"+acr.getTanggal()+"/"+acr.getFileName()+".mp3");
                            //acr.setRadio_url(ConfigUrl.BASE_URL + ConfigUrl.streamingChannel + acr.getId());
                            acaraArrayList.add(acr);
                            Log.i(TAG, " Acara " + acr.getNama() + " " + acr.getRadio_url() + " " + acr.getDownload_url());
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Channel tidak tersedia!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mAdapter = new AcaraAdapter(acaraArrayList, getApplicationContext());
                listAcaraView.setAdapter(mAdapter);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                acaraArrayList.clear();
                mAdapter = new AcaraAdapter(acaraArrayList, getApplicationContext());
                listAcaraView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onClick(View v) {
        // Aksi tombol back bagian atas
        if (v.getId() == R.id.PodcastActivityBack) {
            if (!mAdapter.checkIfAnyPlayProgress() && !mAdapter.checkIfAnyDownloadProgress()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
