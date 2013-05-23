package com.cejensen.missedcalls;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class MissedCallActivity extends Activity {
	Messenger				mService		= null;
	boolean					mIsBound;
	final Messenger	mMessenger	= new Messenger(new IncomingHandler(this));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missed_call);
		refreshStartStop();
		refreshLog();
	}

	@Override
	protected void onStart() {
		doBindService();
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
			doBindService();
		} else {
			doUnbindService();
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

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(MissedCallActivity.this, MissedCallService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, MissedCallService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection	mConnection	= new ServiceConnection() {
																					public void onServiceConnected(ComponentName className, IBinder service) {
																						// This is called when the
																						// connection with the service has
																						// been
																						// established, giving us the
																						// service object we can use to
																						// interact with the service. We are
																						// communicating with our
																						// service through an IDL interface,
																						// so get a client-side
																						// representation of that from the
																						// raw service object.
																						mService = new Messenger(service);

																						try {
																							Message msg = Message.obtain(null, MissedCallService.MSG_REGISTER_CLIENT);
																							msg.replyTo = mMessenger;
																							mService.send(msg);

																						} catch (RemoteException e) {
																						}

																					}

																					public void onServiceDisconnected(ComponentName className) {
																						// This is called when the
																						// connection with the service has
																						// been
																						// unexpectedly disconnected -- that
																						// is, its process crashed.
																						mService = null;
																					}
																				};
}
