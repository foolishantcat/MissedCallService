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
	public static final String					KEY_TEXT							= "text";
	public static final String					KEY_DATE							= "date";

	static final int										MSG_REGISTER_CLIENT		= 1;
	static final int										MSG_UNREGISTER_CLIENT	= 2;
	static final int										MSG_LOG_TO_CLIENT			= 3;

	private ContentResolver							missedCallContentResolver;
	public MissedCallContentObserver		missedCallContentObserver;
	private ContentResolver							unreadSmsContentResolver;
	public UnreadSmsContentObserver			unreadSmsContentObserver;
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
			data.putString(KEY_DATE, Utils.getFormattedDateTime(date));
			data.putString(KEY_TEXT, logText);
			msg.setData(data);
			for (Messenger client : mClients) {
				client.send(msg);
			}
		} catch (RemoteException e) {
		}
	}
}
