package com.jimi.psh.entity;

import java.util.Date;

import com.jimi.psh.annotation.Parse;
import com.jimi.psh.annotation.Protocol;
import com.jimi.psh.constant.JustForTestLine;

@Protocol(0x42)
public class JustForTestBoardNumPackage extends BasePackage {

	@Parse({ 0, 1 })
	private JustForTestLine justForTestLine;
	@Parse({ 1, 4 })
	private Date timestamp;
	@Parse({ 5, 3 })
	private int boardNum;

	public JustForTestLine getLine() {
		return justForTestLine;
	}

	public void setLine(JustForTestLine justForTestLine) {
		this.justForTestLine = justForTestLine;
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
