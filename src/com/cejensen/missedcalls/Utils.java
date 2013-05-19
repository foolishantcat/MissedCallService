package com.cejensen.missedcalls;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
	public static String getFormattedDateTime(Date date) {
		DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		String formattedDate = dateFormatter.format(date);
		return formattedDate;
	}
}
