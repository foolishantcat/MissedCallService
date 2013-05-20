package com.cejensen.missedcalls;

import java.util.Date;

public class LogEntry {
	private long		id;
	private Date		date;
	private String	logText;

	public LogEntry(Date date, String logText) {
		this.id = 0;
		this.date = date;
		this.logText = logText;
	}

	public LogEntry(long id, Date date, String logText) {
		this(date, logText);
		this.id = id;
		this.date = date;
		this.logText = logText;
	}

	public long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public String getLogText() {
		return logText;
	}

	public String toString() {
		return Utils.getFormattedDateTime(date) + " : " + logText;
	}
}
