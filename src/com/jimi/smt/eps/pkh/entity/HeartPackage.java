package com.jimi.smt.eps.pkh.entity;

import java.util.Date;

public class HeartPackage extends Package {

	private String line;

	private Date timestamp;

	private boolean isAlarmEnabled;

	private boolean isConveyorEnabled;

	private boolean isInfraredEnabled;

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isAlarmEnabled() {
		return isAlarmEnabled;
	}

	public void setAlarmEnabled(boolean isAlarmEnabled) {
		this.isAlarmEnabled = isAlarmEnabled;
	}

	public boolean isConveyorEnabled() {
		return isConveyorEnabled;
	}

	public void setConveyorEnabled(boolean isConveyorEnabled) {
		this.isConveyorEnabled = isConveyorEnabled;
	}

	public boolean isInfraredEnabled() {
		return isInfraredEnabled;
	}

	public void setInfraredEnabled(boolean isInfraredEnabled) {
		this.isInfraredEnabled = isInfraredEnabled;
	}

}
