package com.cejensen.missedcalls;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LogHandler extends Handler {
	public MissedCallActivity	mActivity;

	@Override
	public void handleMessage(Message message) {
		Bundle data = message.getData();
		if (message.arg1 == Activity.RESULT_OK && data != null) {
			String logText = data.getString("logtext");
			Date logDate = new Date(data.getLong("date"));
			if (mActivity != null) {
				mActivity.logText(new LogEntry(logDate, logText));
			}
		}
		super.handleMessage(message);
	}
}
