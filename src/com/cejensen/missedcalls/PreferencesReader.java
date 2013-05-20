package com.cejensen.missedcalls;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesReader {
	public enum EmailForwardOptionsSMS {
		Nothing, From, FromAndSubject, FromAndSubjectAndBody
	}

	public enum EmailForwardOptionsCall {
		Nothing, From
	}

	private Context	context;

	public PreferencesReader(Context activity) {
		context = activity;
	}

	public String getUserName() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString("username", "");
	}

	public String getPassword() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString("password", "");
	}

	public String getReceiverEmail() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString("receiver", "");
	}

	public EmailForwardOptionsSMS getEmailForwardOptionSMS() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		EmailForwardOptionsSMS returnValue = EmailForwardOptionsSMS.Nothing;
		int value;
		try {
			value = Integer.parseInt(sharedPrefs.getString("emailForwardOptionsSMS", "0"));
			for (EmailForwardOptionsSMS x : EmailForwardOptionsSMS.values()) {
				if (x.ordinal() == value)
					returnValue = x;
			}
		} catch (NumberFormatException nfe) {
			returnValue = EmailForwardOptionsSMS.Nothing;
		}
		return returnValue;
	}

	public EmailForwardOptionsCall getEmailForwardOptionCall() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		EmailForwardOptionsCall returnValue = EmailForwardOptionsCall.Nothing;
		int value;
		try {
			value = Integer.parseInt(sharedPrefs.getString("emailForwardOptionsCall", "0"));
			for (EmailForwardOptionsCall x : EmailForwardOptionsCall.values()) {
				if (x.ordinal() == value)
					returnValue = x;
			}
		} catch (NumberFormatException nfe) {
			returnValue = EmailForwardOptionsCall.Nothing;
		}
		return returnValue;
	}
}
