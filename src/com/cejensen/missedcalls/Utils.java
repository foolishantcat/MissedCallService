package com.cejensen.missedcalls;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.AsyncTask;
import android.util.Log;

public class Utils {
	public static String getFormattedDateTime(Date date) {
		DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		String formattedDate = dateFormatter.format(date);
		return formattedDate;
	}

	private static class SendMailTask extends AsyncTask<Mail, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Mail... params) {
			boolean res = false;
			try {
				res = params[0].send();
			} catch (Exception e) {
				Log.e("handleMissedCalls", e.toString());
			}
			return res;
		}

	}

	public static Boolean sendMail(String accountUserName, String accountPassword, String receiverEmail, String subject, String body) {
		Mail m = new Mail(accountUserName, accountPassword);
		String[] toArr = { receiverEmail };
		m.setTo(toArr);
		m.setFrom(accountUserName);
		m.setSubject(subject);
		m.setBody(body);

		SendMailTask smt = new SendMailTask();
		smt.execute(m);
		return true;
	}

}
