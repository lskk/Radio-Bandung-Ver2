package org.pptik.radiostreaming.activity;


import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nineoldandroids.view.ViewHelper;

import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pptik.radiostreaming.R;
import org.pptik.radiostreaming.adapter.AcaraAdapter;
import org.pptik.radiostreaming.adapter.BaseMenuAdapter;
import org.pptik.radiostreaming.adapter.LoveListAdapter;
import org.pptik.radiostreaming.adapter.MainListAdapter;
import org.pptik.radiostreaming.database.DBManager;
import org.pptik.radiostreaming.model.Acara;
import org.pptik.radiostreaming.model.BaseMenu;
import org.pptik.radiostreaming.model.StasiunRadio;
import org.pptik.radiostreaming.service.RadioPlayService;
import org.pptik.radiostreaming.util.ConfigUrl;
import org.pptik.radiostreaming.util.Radio;
import org.pptik.radiostreaming.util.RadioClient;
import org.pptik.radiostreaming.util.RadioOperationInfo;
import org.pptik.radiostreaming.view.DragLayout;
import org.pptik.radiostreaming.view.ExitDialog;
import org.pptik.radiostreaming.view.DragLayout.DragListener;

import io.vov.vitamio.Vitamio;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {
    private String TAG = "[MainActivity]";

    private ListView MainMenuList;

    private ListView MainActivityList;

    private ListView MainLoveList;

    private ImageView mImage;

    private DragLayout mDragLayout;

    private RelativeLayout mLoveLayout;

    private Button radioControll;

    private Button mOpenLoveListDown;

    private Button mOpenLoveListUp;

    private TextView radioNameView;

    private MainListAdapter mMainListAdapter;

    private LoveListAdapter mLoveListAdapter;

    private RadioInfoChangeReceiver mReceiver = null;

    private ArrayList<StasiunRadio> mLoveRadioList = new ArrayList<StasiunRadio>();

    private ArrayList<StasiunRadio> mLoveRadio = new ArrayList<>();

    private ArrayList<StasiunRadio> mMainRadio = new ArrayList<>();

    private StasiunRadio radio;

    private int nextPosMain,nextPosLove;

    private boolean IsPlay = false;

    private DBManager mDBManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Vitamio.isInitialized(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity_layout);
        MainMenuList = (ListView) findViewById(R.id.MainMenuList);
        MainActivityList = (ListView) findViewById(R.id.MainActivityList);
        radioControll = (Button) findViewById(R.id.RadioControll);
        radioNameView = (TextView) findViewById(R.id.RadioName);
        initDragLayout();
        initMainMenu();
        initLoveListView();
        initMainListInfo();
        mMainListAdapter = new MainListAdapter(this, mMainRadio);
        MainActivityList.setAdapter(mMainListAdapter);
        radioControll.setOnClickListener(this);
        MainActivityList.setOnItemClickListener(this);
        initRadioService();

    }

    @Override
    protected void onResume() {

        super.onResume();
        initReceiver();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        stopService(new Intent(MainActivity.this, RadioPlayService.class));
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ExitDialog exitDialog = new ExitDialog(this, R.style.MyStyle);
            exitDialog.show();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void initLoveListinfo() {
        if (mLoveRadio != null) {
            mLoveRadio.removeAll(mLoveRadio);
        }

        mDBManager = new DBManager(this);
        mLoveRadioList = mDBManager.queryAll();
        for (int i = 0; i < mLoveRadioList.size(); i++) {
            mLoveRadio.add(mLoveRadioList.get(i));
        }
        mDBManager.closeDB();
    }

    private void initLoveListView() {
        mOpenLoveListDown = (Button) findViewById(R.id.MainOpenLoveListDown);
        mOpenLoveListUp = (Button) findViewById(R.id.MainOpenLoveListUp);
        mLoveLayout = (RelativeLayout) findViewById(R.id.LoveListlayout);
        MainLoveList = (ListView) findViewById(R.id.MainLoveList);
        mLoveListAdapter = new LoveListAdapter(this, mLoveRadio);
        MainLoveList.setAdapter(mLoveListAdapter);
        MainLoveList.setOnItemClickListener(this);
        mOpenLoveListDown.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mOpenLoveListDown.setVisibility(View.GONE);
                mOpenLoveListUp.setVisibility(View.VISIBLE);
                mLoveLayout.setVisibility(View.VISIBLE);
                MainActivityList.setVisibility(View.GONE);
                initLoveListinfo();
                mLoveListAdapter.notifyDataSetChanged();
            }
        });
        mOpenLoveListUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mOpenLoveListDown.setVisibility(View.VISIBLE);
                mOpenLoveListUp.setVisibility(View.GONE);
                mLoveLayout.setVisibility(View.GONE);
                MainActivityList.setVisibility(View.VISIBLE);
                initMainListInfo();
                mMainListAdapter.notifyDataSetChanged();

            }
        });
    }

    private void initMainListInfo() {
        if (mMainRadio != null) {
            mMainRadio.removeAll(mMainRadio);
        }
        prepareMainListRadio();
    }

    private void initDragLayout() {
        mImage = (ImageView) findViewById(R.id.MainImage);
        mImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDragLayout.open();
            }
        });
        mDragLayout = (DragLayout) findViewById(R.id.MainDragLayout);
        mDragLayout.setDragListener(new DragListener() {
            @Override
            public void onOpen() {
                MainMenuList.smoothScrollToPosition(0);
            }

            @Override
            public void onClose() {
                shake();
            }

            @Override
            public void onDrag(float percent) {
                ViewHelper.setAlpha(mImage, 1 - percent);
            }
        });
    }

    private void shake() {
        mImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    private void initMainMenu() {
        ArrayList<BaseMenu> menu = new ArrayList<>();
        menu.add(new BaseMenu(R.drawable.ic_record, "My Recording"));
        menu.add(new BaseMenu(R.drawable.ic_podcast, "Podcast Radio"));
        BaseMenuAdapter baseMenuAdapter = new BaseMenuAdapter(getApplicationContext(), R.layout.list_base_menu, menu);
        //ListAdapter adapterMenu = new ArrayAdapter<String>(MainActivity.this, R.layout.menu_list_adapter, menu);
        MainMenuList.setAdapter(baseMenuAdapter);
        MainMenuList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == MainActivityList) {
            radio = mMainRadio.get(position);
            nextPosMain = position + 1;
            Log.i(TAG, "Execute RadioPlay() action");
            RadioPlay();
        } else if (parent == MainLoveList) {
            radio = mLoveRadio.get(position);
            nextPosLove = position + 1;
            RadioPlay();
        } else if (parent == MainMenuList) {
            switch (position) {
                case 0:
                    mHandler.removeCallbacksAndMessages(null);
                    super.onDestroy();
                    stopService(new Intent(MainActivity.this, RadioPlayService.class));
                    if (mReceiver != null) {
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                    }
                    Intent intent = new Intent(this, RecordActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    mHandler.removeCallbacksAndMessages(null);
                    super.onDestroy();
                    stopService(new Intent(MainActivity.this, RadioPlayService.class));
                    if (mReceiver != null) {
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                    }
                    Intent intent1 = new Intent(this, PodcastActivity.class);
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
        }
    }

    private void initRadioService() {
        Intent intent = new Intent(MainActivity.this, RadioPlayService.class);
        intent.setAction(RadioOperationInfo.RADIO_OPERATION_ACTION);
        startService(intent);
    }

    private void RadioPlay() {
        Intent intent = new Intent();
        intent.setAction(RadioOperationInfo.RADIO_OPERATION_PLAY);
        intent.putExtra(RadioOperationInfo.RADIO_INFO_NAME, radio.getNama());
        intent.putExtra(RadioOperationInfo.RADIO_INFO_PATH, radio.getBroadcast_path());
        sendBroadcast(intent);
        Log.i(TAG, "Play Radio");

    }

    private void RadioStop() {
        Intent intent = new Intent();
        intent.setAction(RadioOperationInfo.RADIO_OPERATION_STOP);
        intent.putExtra(RadioOperationInfo.RADIO_INFO_NAME, radio.getNama());
        intent.putExtra(RadioOperationInfo.RADIO_INFO_PATH, radio.getBroadcast_path());
        sendBroadcast(intent);
        Log.i(TAG, "Stop Radio");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.RadioControll) {
            if (radio == null) {
                Toast.makeText(this, "Pilih stasiun radio streaming!", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "On Click " + radio.getNama() + " " + radio.getBroadcast_path());
                RadioPlay();
            }
        }
    }

    private void initReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mReceiver = new RadioInfoChangeReceiver();
        IntentFilter inFilter = new IntentFilter();
        inFilter.addAction(RadioOperationInfo.RADIO_OPERATION_ACTION);
        inFilter.addAction(RadioOperationInfo.RADIO_OPERATION_CHANGE);
        registerReceiver(mReceiver, inFilter);
    }

    private class RadioInfoChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (RadioOperationInfo.RADIO_OPERATION_ACTION.equals(intent.getAction())) {
                boolean tempIsPlay = intent.getBooleanExtra(RadioOperationInfo.RADIO_OPERATION_ISPLAY, false);
                String tempRadioName = intent.getStringExtra(RadioOperationInfo.RADIO_INFO_NAME);
                Message message = Message.obtain();
                message.what = 2;
                Bundle bundle = new Bundle();
                bundle.putBoolean(RadioOperationInfo.RADIO_OPERATION_ISPLAY, tempIsPlay);
                bundle.putString(RadioOperationInfo.RADIO_INFO_NAME, tempRadioName);
                message.setData(bundle);
                mHandler.sendMessage(message);
            } else if (RadioOperationInfo.RADIO_OPERATION_CHANGE.equals(intent.getAction())) {
                radioNameView.setText("Loading Radio...");
            } else if (RadioOperationInfo.RADIO_LOVE_LIST_UPDATE.equals(intent.getAction())) {
                int position = intent.getIntExtra(RadioOperationInfo.RADIO_INFO_NUM, 0);
                mLoveRadio.remove(position);
            }
        }

    }

    public void prepareMainListRadio() {
        RadioClient.get(getApplicationContext(), ConfigUrl.radioBroadcast, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                mMainRadio.clear();
                mMainListAdapter = new MainListAdapter(getApplicationContext(), mMainRadio);
                MainActivityList.setAdapter(mMainListAdapter);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(), "Koneksi time-out!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                mMainRadio.clear();
                mMainListAdapter = new MainListAdapter(getApplicationContext(), mMainRadio);
                MainActivityList.setAdapter(mMainListAdapter);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(), "Koneksi time-out!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mMainRadio.clear();
                mMainListAdapter = new MainListAdapter(getApplicationContext(), mMainRadio);
                MainActivityList.setAdapter(mMainListAdapter);
                if (throwable.getCause() instanceof ConnectTimeoutException) {
                    Toast.makeText(getApplicationContext(), "Koneksi time-out!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mMainRadio.clear();
                try {
                    if (response.getBoolean("status")) {
                        JSONArray array = response.getJSONArray("broadcast");
                        for (int i = 0; i < array.length(); i++) {
                            StasiunRadio acr = new StasiunRadio();
                            acr.setNama(array.getJSONObject(i).getString("name"));
                            acr.setAlamat(array.getJSONObject(i).getString("address"));
                            acr.setBroadcast_path(array.getJSONObject(i).getString("url_stream_stereo"));
                            acr.setAbout(array.getJSONObject(i).getString("about"));
                            //acr.setRadio_url(ConfigUrl.BASE_URL+ConfigUrl.streamingChannel+stRadio.getNama()+"/"+acr.getTanggal()+"/"+acr.getFileName()+".mp3");
                            //acr.setRadio_url(ConfigUrl.BASE_URL + ConfigUrl.streamingChannel + acr.getId());
                            if (!acr.getBroadcast_path().equals(null) && !acr.getBroadcast_path().equals("") && !acr.getBroadcast_path().equals(" ")) {
                                mMainRadio.add(acr);
                                Log.i(TAG, acr.toString());
                            }
                        }
                        mMainListAdapter = new MainListAdapter(getApplicationContext(), mMainRadio);
                        MainActivityList.setAdapter(mMainListAdapter);
                    } else {
                        Toast.makeText(getApplicationContext(), "Channel tidak tersedia!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                mMainRadio.clear();
                mMainListAdapter = new MainListAdapter(getApplicationContext(), mMainRadio);
                MainActivityList.setAdapter(mMainListAdapter);
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg != null && msg.what == 2) {
                Bundle bundle = msg.getData();
                boolean tempIsPlay = bundle.getBoolean(RadioOperationInfo.RADIO_OPERATION_ISPLAY);
                String tempRadioName = bundle.getString(RadioOperationInfo.RADIO_INFO_NAME);
                if (IsPlay != tempIsPlay) {
                    IsPlay = tempIsPlay;
                    radio.setNama(tempRadioName);
                    if (IsPlay == true) {
                        radioNameView.setText(radio.getNama());
                    }
                }

                if (radioNameView.getText().toString().equals(radio.getNama())) {
                    if (IsPlay == false) {
                        radioControll.setBackgroundResource(R.drawable.ic_play);
                    } else {
                        radioControll.setBackgroundResource(R.drawable.ic_pause);
                    }
                }
            }

        }

        ;
    };
}
