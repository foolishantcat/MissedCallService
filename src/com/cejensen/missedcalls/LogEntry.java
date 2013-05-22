package com.cejensen.missedcalls;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private long							mId;
	private Date							mDate;
	private String						mLogText;

	public LogEntry(Date date, String logText) {
		this.mId = 0;
		this.mDate = date;
		this.mLogText = logText;
	}

	public LogEntry(long id, Date date, String logText) {
		this(date, logText);
		this.mId = id;
		this.mDate = date;
		this.mLogText = logText;
	}

	public long getId() {
		return mId;
	}

	public Date getDate() {
		return mDate;
	}

	public String getLogText() {
		return mLogText;
	}

	public String toString() {
		return Utils.getFormattedDateTime(mDate) + " : " + mLogText;
	}
}
