package com.cejensen.missedcalls;

import java.util.Date;
import java.util.HashSet;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class UnreadSmsContentObserver extends ContentObserver {
	private HashSet<Long>					m_idsSent				= new HashSet<Long>();
	private MissedCallService			m_service;
	private MissedCallApplication	m_app;
	public static Uri							m_mmssmsContent	= Uri.parse("content://mms-sms/conversations/");
	public Uri										m_smsContent		= Uri.parse("content://sms");
	public Uri										m_mmsContent		= Uri.parse("content://mms");

	public UnreadSmsContentObserver(MissedCallService service, MissedCallApplication app, Handler handler) {
		super(handler);
		m_app = app;
		m_service = service;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);

		MissedCallDatabase db = new MissedCallDatabase(m_service.getBaseContext());
		db.open();
		if (db.getActive()) {
			handleUnreadSms();
		}
		db.close();
	}

	public void handleUnreadSms() {
		try {
			PreferencesReader pr = new PreferencesReader(m_app);
			PreferencesReader.EmailForwardOptionsSMS emailForward = pr.getEmailForwardOptionSMS();

			if (emailForward != PreferencesReader.EmailForwardOptionsSMS.Nothing) {
				Cursor cursor = m_app.getContentResolver().query(m_smsContent, null, "read = 0", null, "date DESC");
				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex("_id"));
					if (!m_idsSent.contains(id)) {
						String basis;
						Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
						String body = cursor.getString(cursor.getColumnIndex("body"));
						String from = cursor.getString(cursor.getColumnIndex("address"));

						// SMS
						basis = m_app.getString(R.string.sms_received, from, Utils.getFormattedDateTime(date));
						if (emailForward == PreferencesReader.EmailForwardOptionsSMS.From) {
							body = "";
						} else {
							basis += " " + body;
							body = "";
						}

						if (m_app.sendMail(basis, ""))
							m_idsSent.add(id);

						m_service.log(date, basis);
					}
				}
				cursor.close();

				cursor = m_app.getContentResolver().query(m_mmsContent, null, "read = 0", null, "date DESC");
				while (cursor.moveToNext()) {
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						if (cursor.getString(i) != null)
							Log.d("", cursor.getColumnName(i) + "=" + cursor.getString(i));
					}
					long id = cursor.getLong(cursor.getColumnIndex("_id"));
					// getAllText(id);
					if (!m_idsSent.contains(id)) {
						String basis;

						// MMS
						Date date = new Date();
						basis = "En MMS blev modtaget " + Utils.getFormattedDateTime(date);

						if (m_app.sendMail(basis, ""))
							m_idsSent.add(id);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public void getAllText(long mmsId)
	// {
	// String selectionPart = "mid=" + mmsId;
	// Uri uri = Uri.parse("content://mms/part");
	// Cursor cursor = m_activity.getContentResolver().query(uri, null, null/*
	// selectionPart */, null, null);
	// while (cursor.moveToNext())
	// {
	// String partId = cursor.getString(cursor.getColumnIndex("_id"));
	// String type = cursor.getString(cursor.getColumnIndex("ct"));
	// for (int i = 0; i < cursor.getColumnCount(); i++)
	// {
	// if (cursor.getString(i)!=null)
	// Log.d("", cursor.getColumnName(i) + "=" + cursor.getString(i));
	// }
	// if ("text/plain".equals(type))
	// {
	// String data = cursor.getString(cursor.getColumnIndex("_data"));
	// String body;
	// if (data != null)
	// {
	// // implementation of this method below
	// body = getMmsText(partId);
	// } else
	// {
	// body = cursor.getString(cursor.getColumnIndex("text"));
	// }
	// Log.d(type+", " +partId, body);
	// }
	// }
	// while (cursor.moveToNext());
	//
	// }
	//
	// public String getMmsText(String id)
	// {
	// Uri partURI = Uri.parse("content://mms/part/" + id);
	// InputStream is = null;
	// StringBuilder sb = new StringBuilder();
	// try
	// {
	// is = m_activity.getContentResolver().openInputStream(partURI);
	// if (is != null)
	// {
	// InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	// BufferedReader reader = new BufferedReader(isr);
	// String temp = reader.readLine();
	// while (temp != null)
	// {
	// sb.append(temp);
	// temp = reader.readLine();
	// }
	// }
	// } catch (IOException e)
	// {
	// } finally
	// {
	// if (is != null)
	// {
	// try
	// {
	// is.close();
	// } catch (IOException e)
	// {
	// }
	// }
	// }
	// return sb.toString();
	// }

}
