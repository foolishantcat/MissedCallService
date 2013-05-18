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

public class MissedCallService extends Service
{
	private ContentResolver						m_missedCallContentResolver;
	public MissedCallContentObserver	m_missedCallContentObserver;
	private ContentResolver						m_unreadSmsContentResolver;
	public UnreadSmsContentObserver		m_unreadSmsContentObserver;
	private final MissedCallBinder		m_binder	= new MissedCallBinder();
	private Messenger									m_logMessenger;

	@Override
	public void onCreate()
	{
		Log.d("MissedCallService", "onCreate");
		super.onCreate();
		registerSMSObserver();
		registerCallObserver();
	}

	@Override
	public void onDestroy()
	{
		Log.d("MissedCallService", "onDestroy");
		super.onDestroy();
		unregisterSMSObserver();
		unregisterCallObserver();
	}

	private void registerSMSObserver()
	{
		MissedCallApplication app = (MissedCallApplication) getApplicationContext();
		PreferencesReader pr = new PreferencesReader(app);
		PreferencesReader.EmailForwardOptionsSMS emailForward = pr.getEmailForwardOptionSMS();

		Log.d("MissedCallService", "registerSMSObserver - emailForward: " + emailForward);
		if (emailForward != PreferencesReader.EmailForwardOptionsSMS.Nothing)
		{
			if (m_unreadSmsContentObserver == null)
			{
				m_unreadSmsContentObserver = new UnreadSmsContentObserver(this, app, new LogHandler());
				m_unreadSmsContentResolver = getContentResolver();
				m_unreadSmsContentResolver.registerContentObserver(UnreadSmsContentObserver.m_mmssmsContent, true, m_unreadSmsContentObserver);
				Log.d("MissedCallService", "SMS Observer registered.");
			}
		}
	}

	private void unregisterSMSObserver()
	{
		if (m_unreadSmsContentObserver != null)
		{
			if (m_unreadSmsContentResolver != null)
			{
				m_unreadSmsContentResolver.unregisterContentObserver(m_unreadSmsContentObserver);
			}
			m_unreadSmsContentObserver = null;
		}
		Log.d("MissedCallService", "SMS Observer unregistered.");
	}

	private void registerCallObserver()
	{
		MissedCallApplication app = (MissedCallApplication) getApplicationContext();
		PreferencesReader pr = new PreferencesReader(app);
		PreferencesReader.EmailForwardOptionsCall emailForward = pr.getEmailForwardOptionCall();

		Log.d("MissedCallService", "registerCallObserver - emailForward: " + emailForward);
		if (emailForward != PreferencesReader.EmailForwardOptionsCall.Nothing)
		{
			if (m_missedCallContentObserver == null)
			{
				m_missedCallContentObserver = new MissedCallContentObserver(this, app, new LogHandler());
				m_missedCallContentResolver = getContentResolver();
				m_missedCallContentResolver.registerContentObserver(Calls.CONTENT_URI, true, m_missedCallContentObserver);
				Log.d("MissedCallService", "Missed call Observer registered.");
			}
		}
	}

	private void unregisterCallObserver()
	{
		if (m_missedCallContentObserver != null)
		{
			if (m_missedCallContentResolver != null)
			{
				m_missedCallContentResolver.unregisterContentObserver(m_missedCallContentObserver);
			}
			m_missedCallContentObserver = null;
		}
		Log.d("MissedCallService", "Missed call Observer unregistered.");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	public MissedCallBinder onBind(Intent intent)
	{
		Log.d("MissedCallService", "onBind(" + intent + ")");
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			m_logMessenger = (Messenger) extras.get(MissedCallActivity.logMessenger);
		}
		return m_binder;
	}

	public class MissedCallBinder extends Binder
	{
		MissedCallService getService()
		{
			return MissedCallService.this;
		}
	}

	public void log(Date date, String logText)
	{
		Message logMsg = Message.obtain();
		logMsg.arg1 = Activity.RESULT_OK;
		Bundle bundle = new Bundle();
		bundle.putString("logtext", logText);
		bundle.putLong("date", date.getTime());
		logMsg.setData(bundle);
		try
		{
			Log.d("log", "m_logMessenger:" + m_logMessenger);
			if (m_logMessenger != null)
			{
				m_logMessenger.send(logMsg);
			}
		}
		catch (android.os.RemoteException e1)
		{
			Log.w(getClass().getName(), "Exception sending message", e1);
		}

	}
}
