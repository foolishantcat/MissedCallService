package com.cejensen.missedcalls;

import java.util.Date;
import java.util.HashSet;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog.Calls;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MissedCallContentObserver extends ContentObserver {
	private HashSet<Long>					m_idsSent	= new HashSet<Long>();
	private MissedCallService			m_service;
	private MissedCallApplication	m_app;

	public MissedCallContentObserver(MissedCallService service, MissedCallApplication app, Handler handler) {
		super(handler);
		m_app = app;
		m_service = service;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		MissedCallDatabase db = new MissedCallDatabase(m_service.getBaseContext());
		db.open();
		if (db.getActive()) {
			handleMissedCalls();
		}
		db.close();
	}

	public void handleMissedCalls() {
		try {
			PreferencesReader pr = new PreferencesReader(m_app);
			PreferencesReader.EmailForwardOptionsCall emailForward = pr.getEmailForwardOptionCall();

			if (emailForward != PreferencesReader.EmailForwardOptionsCall.Nothing) {
				Cursor cursor = m_app.getContentResolver().query(Calls.CONTENT_URI, null, Calls.TYPE + " = ? AND " + Calls.NEW + " = ?",
						new String[] { Integer.toString(Calls.MISSED_TYPE), "1" }, Calls.DATE + " DESC ");

				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex("_id"));
					if (!m_idsSent.contains(id)) {
						Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
						String number = cursor.getString(cursor.getColumnIndex("number"));
						// int duration =
						// cursor.getInt(cursor.getColumnIndex("duration"));

						String subject = String.format(m_app.getString(R.string.missed_call_subject_received), number, Utils.getFormattedDateTime(date));

						if (m_app.sendMail(subject, ""))
							m_idsSent.add(id);

						m_service.log(date, subject);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
