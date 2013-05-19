package com.cejensen.missedcalls;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class MissedCallActivity extends Activity {
	public static final String		logMessenger	= "LOGMESSENGER";
	public MissedCallApplication	m_app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		m_app = (MissedCallApplication) getApplication();
		m_app.m_logHandler.m_activity = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missed_call);
		refreshStartStop();
		refreshLog();
	}

	@Override
	protected void onStart() {
		Intent intentService = new Intent(this, MissedCallService.class);
		Messenger messenger = new Messenger(m_app.m_logHandler);
		intentService.putExtra(logMessenger, messenger);
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_missed_call, menu);
		return true;
	}

	private void refreshStartStop() {
		Switch startButton = (Switch) this.findViewById(R.id.startButton);
		MissedCallDatabase db = new MissedCallDatabase(this);
		db.open();
		boolean isActive = db.getActive();
		startButton.setChecked(isActive);
		db.close();
		if (isActive) {
			Intent intentService = new Intent(this, MissedCallService.class);
			this.startService(intentService);
		}
	}

	public void onStartButton(View view) {
		MissedCallDatabase db = new MissedCallDatabase(this);
		db.open();
		if (db.getActive()) {
			logText(R.string.inactive);
			db.setActive(false);
		} else {
			logText(R.string.active);
			db.setActive(true);
		}
		db.close();
		refreshStartStop();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about: {
			try {
				PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
				String message = MissedCallActivity.this.getResources().getString(R.string.app_name) + " "
						+ MissedCallActivity.this.getResources().getString(R.string.txt_version) + " " + pInfo.versionName;
				Dialog aboutDialog = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setMessage(message).setTitle(R.string.about).create();

				aboutDialog.show();
			} catch (NameNotFoundException e) {
			}

		}
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings: {
			Intent settingsIntent = new Intent(this, PrefsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	public void refreshLog() {
		final TextView logView = (TextView) findViewById(R.id.editLog);
		MissedCallDatabase db = new MissedCallDatabase(this);
		db.open();
		LogEntry[] logEntries = db.getAllLogEntries();
		db.close();

		StringBuilder sb = new StringBuilder();
		for (LogEntry log : logEntries) {
			sb.append(log.toString());
			sb.append('\n');
		}
		logView.setText(sb.toString());
	}

	public void logText(LogEntry log) {
		MissedCallDatabase db = new MissedCallDatabase(this);
		db.open();
		db.insertLogEntry(log);
		db.close();

		refreshLog();
	}

	public void logText(int id) {
		Date date = new Date();
		String txt = getString(id);
		logText(new LogEntry(date, txt));
	}
}
