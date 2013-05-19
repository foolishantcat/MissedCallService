package com.cejensen.missedcalls;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;

public class MissedCallApplication extends Application {
	public boolean						m_isRunning		= false;
	public List<LogEntry>			m_log					= new ArrayList<LogEntry>();
	public LogHandler					m_logHandler	= new LogHandler();

	public ServiceConnection	m_connection	= new ServiceConnection() {
																						public void onServiceConnected(ComponentName className, IBinder binder) {
																						}

																						public void onServiceDisconnected(ComponentName className) {
																						}
																					};

	public MissedCallApplication() {
		// TODO Auto-generated constructor stub
	}

	private class SendMailTask extends AsyncTask<Mail, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Mail... params) {
			boolean res = false;
			try {
				res = params[0].send();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		}

		// This is called when doInBackground() is finished
		protected void onPostExecute(Boolean result) {
			// if (result)
			// Toast.makeText(MissedCallActivity.this, R.string.email_sent,
			// Toast.LENGTH_LONG).show();
			// else
			// Toast.makeText(MissedCallActivity.this, R.string.email_not_sent,
			// Toast.LENGTH_LONG).show();
		}

	}

	public Boolean sendMail(String subject, String body) {
		PreferencesReader pr = new PreferencesReader(this);

		Mail m = new Mail(pr.getUserName(), pr.getPassword());
		String[] toArr = { pr.getReceiverEmail() };
		m.setTo(toArr);
		m.setFrom(pr.getUserName());
		m.setSubject(subject);
		m.setBody(body);

		SendMailTask smt = new SendMailTask();
		smt.execute(m);
		// logText(subject);
		return true;
	}

}
