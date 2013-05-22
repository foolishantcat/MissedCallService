package com.cejensen.missedcalls;

import java.util.Date;
import java.util.HashSet;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class UnreadSmsContentObserver extends ContentObserver {
	private HashSet<Long>			mIdsSent				= new HashSet<Long>();
	private MissedCallService	mService;
	public static Uri					mMmsSmsContent	= Uri.parse("content://mms-sms/conversations/");
	private Uri								mSmsContent			= Uri.parse("content://sms");
	private Uri								mMmsContent			= Uri.parse("content://mms");

	public UnreadSmsContentObserver(MissedCallService service, LogHandler handler) {
		super(handler);
		this.mService = service;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);

		MissedCallDatabase db = new MissedCallDatabase(mService.getBaseContext());
		db.open();
		if (db.getActive()) {
			handleUnreadSms();
		}
		db.close();
	}

	public void handleUnreadSms() {
		try {
			PreferencesReader pr = new PreferencesReader(mService.getApplicationContext());
			PreferencesReader.EmailForwardOptionsSMS emailForward = pr.getEmailForwardOptionSMS();

			if (emailForward != PreferencesReader.EmailForwardOptionsSMS.Nothing) {
				Cursor cursor = mService.getApplicationContext().getContentResolver().query(mSmsContent, null, "read = 0", null, "date DESC");
				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex("_id"));
					if (!mIdsSent.contains(id)) {
						String basis;
						Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
						String body = cursor.getString(cursor.getColumnIndex("body"));
						String from = cursor.getString(cursor.getColumnIndex("address"));

						// SMS
						basis = mService.getApplicationContext().getString(R.string.sms_received, from, Utils.getFormattedDateTime(date));
						if (emailForward == PreferencesReader.EmailForwardOptionsSMS.From) {
							body = "";
						} else {
							basis += " " + body;
							body = "";
						}

						if (Utils.sendMail(pr.getUserName(), pr.getPassword(), pr.getReceiverEmail(), basis, ""))
							mIdsSent.add(id);

						mService.log(date, basis);
					}
				}
				cursor.close();

				cursor = mService.getApplicationContext().getContentResolver().query(mMmsContent, null, "read = 0", null, "date DESC");
				while (cursor.moveToNext()) {
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						if (cursor.getString(i) != null)
							Log.d("", cursor.getColumnName(i) + "=" + cursor.getString(i));
					}
					long id = cursor.getLong(cursor.getColumnIndex("_id"));

					if (!mIdsSent.contains(id)) {
						String basis;

						// MMS
						Date date = new Date();
						basis = "En MMS blev modtaget " + Utils.getFormattedDateTime(date);

						if (Utils.sendMail(pr.getUserName(), pr.getPassword(), pr.getReceiverEmail(), basis, ""))
							mIdsSent.add(id);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			Log.e("handleMissedCalls", e.toString());
		}
	}
}
