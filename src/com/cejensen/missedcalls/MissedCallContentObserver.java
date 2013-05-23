package com.cejensen.missedcalls;

import java.util.Date;
import java.util.HashSet;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog.Calls;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MissedCallContentObserver extends ContentObserver {
	private HashSet<Long>			mIdsSent	= new HashSet<Long>();
	private MissedCallService	mService;

	public MissedCallContentObserver(MissedCallService service, LogHandler handler) {
		super(handler);
		this.mService = service;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		MissedCallDatabase db = new MissedCallDatabase(mService.getBaseContext());
		db.open();
		if (db.getActive()) {
			handleMissedCalls();
		}
		db.close();
	}

	public void handleMissedCalls() {
		try {
			PreferencesReader pr = new PreferencesReader(mService.getApplicationContext());
			PreferencesReader.EmailForwardOptionsCall emailForward = pr.getEmailForwardOptionCall();

			if (emailForward != PreferencesReader.EmailForwardOptionsCall.Nothing) {
				Cursor cursor = mService
						.getApplicationContext()
						.getContentResolver()
						.query(Calls.CONTENT_URI, null, Calls.TYPE + " = ? AND " + Calls.NEW + " = ?", new String[] { Integer.toString(Calls.MISSED_TYPE), "1" },
								Calls.DATE + " DESC ");

				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex("_id"));
					if (!mIdsSent.contains(id)) {
						Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
						String number = cursor.getString(cursor.getColumnIndex("number"));

						String subject = String.format(mService.getApplicationContext().getString(R.string.missed_call_subject_received), number,
								Utils.getFormattedDateTime(date));

						if (Utils.sendMail(pr.getUserName(), pr.getPassword(), pr.getReceiverEmail(), subject, ""))
							mIdsSent.add(id);

						mService.log(date, subject);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("handleMissedCalls", e.toString());
		}
	}
}
