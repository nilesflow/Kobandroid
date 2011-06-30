package com.example.android.kobandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class Kobandroid extends Activity {
	private MediaPlayer mMediaPlayer;
	private AudioManager mAudioManager;
	private EditText mEditText;
	private SharedPreferences mPref;
	final int DLG_ID_VOLUME = 1;
	final int DLG_ID_KEYNUMBER = 2;
	final int DLG_ID_WRONGNUMBER = 3;
	final String mStKeyNumber = "key_number";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // アラーム音の準備
        mMediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        Log.d(R.string.app_name + "MediaPlayer","mMediaPlayer:" + mMediaPlayer);
        if(mMediaPlayer == null) {
        	Log.e("MediaPlayer", "error:null");
        	return;
        }
        mMediaPlayer.setLooping(true);
        
        // 音量設定変更用
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        	
    	// 音量変更選択ダイアログ表示
    	showDialog(DLG_ID_VOLUME);

    	// 暗証番号設定ダイアログ表示（暗所番号未設定時）
    	mPref = getPreferences(MODE_PRIVATE);
    	if(mPref!=null) {
    		String str = mPref.getString(mStKeyNumber, "");
    		if(str!=null) {
    	    	Log.d(R.string.app_name + "getPreferences", "mStKeyNumber" + str);
    			if(str.compareTo("")==0) {
		        	showDialog(DLG_ID_KEYNUMBER);
		    	}
    		}
    	}

        // システム通知設定
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver_, intentFilter);
    }
 
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 音量設定を戻す
//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMediaPlayer.release();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if(keyCode != KeyEvent.KEYCODE_BACK) {
    		return super.onKeyDown(keyCode, event);
    	} else {
    		if(mMediaPlayer != null)
    		{
	    		mMediaPlayer.stop();
    		}
//    		return false;
    		return super.onKeyDown(keyCode, event);
    	}
    }

    @Override
	protected Dialog onCreateDialog(int id) {
    	switch(id) {
    	// 音量設定変更ダイアログ
    	case DLG_ID_VOLUME:
    		return new AlertDialog.Builder(this)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setTitle(R.string.dialog_manner_title)
    		.setMessage(R.string.dialog_manner_message)
    		.setPositiveButton(android.R.string.ok,
        		new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 50, 0);
					}
    			}
    		)
    		.setNegativeButton(android.R.string.cancel,
    			new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				}
    		)
    		.create();

    	// 暗証番号設定ダイアログ
    	case DLG_ID_KEYNUMBER:
    		mEditText = new EditText(this);
    		return new AlertDialog.Builder(this)
    		.setIcon(android.R.drawable.ic_dialog_info)
    		.setTitle(R.string.dialog_keynumber_title)
//    		.setMessage(R.string.dialog_keynumber_message)
    		.setView(mEditText)
    		.setPositiveButton(android.R.string.ok,
        		new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						String str = mEditText.getText().toString();
						// 4桁でない場合は、保存しない
						try {
							Log.d(R.string.app_name + "dialog", "length" + str.length());
							if(str.length()!=4)
							{
								showDialog(DLG_ID_WRONGNUMBER);
								return;
							}
							Log.d(R.string.app_name + "dialog", "save prefrence");
							Editor e = mPref.edit();
							e.putString(mStKeyNumber, str);
							e.commit();
						}
						catch(NumberFormatException e) {
							Log.d(R.string.app_name + "dialog", "error numbererror");
							showDialog(DLG_ID_WRONGNUMBER);
						}
					}
    			}
    		)
    		.setNegativeButton(android.R.string.cancel,
    			new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}
    		)
    		.create();

        // 暗証番号誤りダイアログ
    	case DLG_ID_WRONGNUMBER:
    		return new AlertDialog.Builder(this)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setTitle(R.string.dialog_wrong_number_title)
//    		.setMessage(R.string.dialog_manner_message)
    		.setPositiveButton(android.R.string.ok,
        		new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						showDialog(DLG_ID_KEYNUMBER);
					}
    			}
    		)
    		.create();
    	}
		return null;
	}

    private BroadcastReceiver broadcastReceiver_ = new BroadcastReceiver() {
    	private int batteryStatus;    	

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {

            	// 充電状態  
                switch (intent.getIntExtra("status", 0)) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    Log.d(R.string.app_name + "BatteryChange", "Status : CHARGING");
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    Log.d(R.string.app_name + "BatteryChange", "Status : DISCHARGING");
                    // Battery状態変化
                    if(batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING)
                    {
                    	mMediaPlayer.start();
                    }
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    Log.d(R.string.app_name + "BatteryChange", "Status : FULL");
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    Log.d(R.string.app_name + "BatteryChange", "Status : NOT CHARGING");
                    break;
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    Log.d(R.string.app_name + "BatteryChange", "Status : UNKNOWN");
                    break;
                }
                // Battery状態保持
            	batteryStatus = intent.getIntExtra("status", 0);

            }
        }
    };
};
