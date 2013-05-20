package com.cejensen.missedcalls;

import java.util.Date;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.provider.CallLog.Calls;
import android.util.Log;

public class MissedCallService extends Service {
	private ContentResolver						missedCallContentResolver;
	public MissedCallContentObserver	missedCallContentObserver;
	private ContentResolver						unreadSmsContentResolver;
	public UnreadSmsContentObserver		unreadSmsContentObserver;
	private final MissedCallBinder		binder	= new MissedCallBinder();
	private Messenger									logMessenger;

	@Override
	public void onCreate() {
		Log.d("MissedCallService", "onCreate");
		super.onCreate();
		registerSMSObserver();
		registerCallObserver();
	}

	@Override
	public void onDestroy() {
		Log.d("MissedCallService", "onDestroy");
		super.onDestroy();
		unregisterSMSObserver();
		unregisterCallObserver();
		Root.getSingleton().setServiceStopped();
	}

	private void registerSMSObserver() {
		PreferencesReader pr = new PreferencesReader(getApplicationContext());
		PreferencesReader.EmailForwardOptionsSMS emailForward = pr.getEmailForwardOptionSMS();

		Log.d("MissedCallService", "registerSMSObserver - emailForward: " + emailForward);
		if (emailForward != PreferencesReader.EmailForwardOptionsSMS.Nothing) {
			if (unreadSmsContentObserver == null) {
				unreadSmsContentObserver = new UnreadSmsContentObserver(this, new LogHandler());
				unreadSmsContentResolver = getContentResolver();
				unreadSmsContentResolver.registerContentObserver(UnreadSmsContentObserver.mmssmsContent, true, unreadSmsContentObserver);
				Log.d("MissedCallService", "SMS Observer registered.");
			}
		}
	}

	private void unregisterSMSObserver() {
		if (unreadSmsContentObserver != null) {
			if (unreadSmsContentResolver != null) {
				unreadSmsContentResolver.unregisterContentObserver(unreadSmsContentObserver);
			}
			unreadSmsContentObserver = null;
		}
		Log.d("MissedCallService", "SMS Observer unregistered.");
	}

	private void registerCallObserver() {
		PreferencesReader pr = new PreferencesReader(getApplicationContext());
		PreferencesReader.EmailForwardOptionsCall emailForward = pr.getEmailForwardOptionCall();

		Log.d("MissedCallService", "registerCallObserver - emailForward: " + emailForward);
		if (emailForward != PreferencesReader.EmailForwardOptionsCall.Nothing) {
			if (missedCallContentObserver == null) {
				missedCallContentObserver = new MissedCallContentObserver(this, new LogHandler());
				missedCallContentResolver = getContentResolver();
				missedCallContentResolver.registerContentObserver(Calls.CONTENT_URI, true, missedCallContentObserver);
				Log.d("MissedCallService", "Missed call Observer registered.");
			}
		}
	}

	private void unregisterCallObserver() {
		if (missedCallContentObserver != null) {
			if (missedCallContentResolver != null) {
				missedCallContentResolver.unregisterContentObserver(missedCallContentObserver);
			}
			missedCallContentObserver = null;
		}
		Log.d("MissedCallService", "Missed call Observer unregistered.");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public MissedCallBinder onBind(Intent intent) {
		Log.d("MissedCallService", "onBind(" + intent + ")");
		Bundle extras = intent.getExtras();
		Log.d("MissedCallService", "extras: " + extras);
		if (extras != null) {
			logMessenger = (Messenger) extras.get(Root.logMessenger);
			Log.d("MissedCallService", "logMessenger: " + logMessenger);
		}
		return binder;
	}

	public class MissedCallBinder extends Binder {
		MissedCallService getService() {
			return MissedCallService.this;
		}
	}

	public void log(Date date, String logText) {
		Message logMsg = Message.obtain();
		logMsg.arg1 = Activity.RESULT_OK;
		Bundle bundle = new Bundle();
		bundle.putString("logtext", logText);
		bundle.putLong("date", date.getTime());
		logMsg.setData(bundle);
		try {
			Log.d("log", "logMessenger:" + logMessenger);
			if (logMessenger != null) {
				logMessenger.send(logMsg);
			}
		} catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}

	}
}
