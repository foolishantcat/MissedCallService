package com.cejensen.missedcalls;

import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.CallLog.Calls;
import android.util.Log;

public class MissedCallService extends Service {
	public static final String					KEY_LOGENTRY					= "logentry";

	static final int										MSG_REGISTER_CLIENT		= 1;
	static final int										MSG_UNREGISTER_CLIENT	= 2;
	static final int										MSG_LOG_TO_CLIENT			= 3;

	private ContentResolver							mMissedCallContentResolver;
	public MissedCallContentObserver		mMissedCallContentObserver;
	private ContentResolver							mUnreadSmsContentResolver;
	public UnreadSmsContentObserver			mUnreadSmsContentObserver;
	private static ArrayList<Messenger>	mClients							= new ArrayList<Messenger>();

	/**
	 * Handler of incoming messages from clients.
	 */
	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger	mMessenger	= new Messenger(new IncomingHandler());

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("MissedCallService", "onBind(" + intent + ")");
		return mMessenger.getBinder();
	}

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
	}

	private void registerSMSObserver() {
		PreferencesReader pr = new PreferencesReader(getApplicationContext());
		PreferencesReader.EmailForwardOptionsSMS emailForward = pr.getEmailForwardOptionSMS();

		Log.d("MissedCallService", "registerSMSObserver - emailForward: " + emailForward);
		if (emailForward != PreferencesReader.EmailForwardOptionsSMS.Nothing) {
			if (mUnreadSmsContentObserver == null) {
				mUnreadSmsContentObserver = new UnreadSmsContentObserver(this, new LogHandler());
				mUnreadSmsContentResolver = getContentResolver();
				mUnreadSmsContentResolver.registerContentObserver(UnreadSmsContentObserver.mMmsSmsContent, true, mUnreadSmsContentObserver);
				Log.d("MissedCallService", "SMS Observer registered.");
			}
		}
	}

	private void unregisterSMSObserver() {
		if (mUnreadSmsContentObserver != null) {
			if (mUnreadSmsContentResolver != null) {
				mUnreadSmsContentResolver.unregisterContentObserver(mUnreadSmsContentObserver);
			}
			mUnreadSmsContentObserver = null;
		}
		Log.d("MissedCallService", "SMS Observer unregistered.");
	}

	private void registerCallObserver() {
		PreferencesReader pr = new PreferencesReader(getApplicationContext());
		PreferencesReader.EmailForwardOptionsCall emailForward = pr.getEmailForwardOptionCall();

		Log.d("MissedCallService", "registerCallObserver - emailForward: " + emailForward);
		if (emailForward != PreferencesReader.EmailForwardOptionsCall.Nothing) {
			if (mMissedCallContentObserver == null) {
				mMissedCallContentObserver = new MissedCallContentObserver(this, new LogHandler());
				mMissedCallContentResolver = getContentResolver();
				mMissedCallContentResolver.registerContentObserver(Calls.CONTENT_URI, true, mMissedCallContentObserver);
				Log.d("MissedCallService", "Missed call Observer registered.");
			}
		}
	}

	private void unregisterCallObserver() {
		if (mMissedCallContentObserver != null) {
			if (mMissedCallContentResolver != null) {
				mMissedCallContentResolver.unregisterContentObserver(mMissedCallContentObserver);
			}
			mMissedCallContentObserver = null;
		}
		Log.d("MissedCallService", "Missed call Observer unregistered.");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		return super.onStartCommand(intent, flags, startId);
	}

	public class MissedCallBinder extends Binder {
		MissedCallService getService() {
			return MissedCallService.this;
		}
	}

	public void log(Date date, String logText) {
		try {
			Message msg = Message.obtain(null, MissedCallService.MSG_LOG_TO_CLIENT);
			msg.replyTo = mMessenger;
			Bundle data = new Bundle();
			LogEntry le = new LogEntry(date, logText);
			data.putSerializable(KEY_LOGENTRY, le);
			msg.setData(data);
			for (Messenger client : mClients) {
				client.send(msg);
			}
		} catch (RemoteException e) {
		}
	}
}
