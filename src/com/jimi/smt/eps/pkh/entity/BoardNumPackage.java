package com.jimi.smt.eps.pkh.entity;

import java.util.Date;

public class BoardNumPackage extends Package {

	private String line;

	private Date timestamp;

	private int boardNum;

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

	public int getBoardNum() {
		return boardNum;
	}

	public void setBoardNum(int boardNum) {
		this.boardNum = boardNum;
	}

	
}
