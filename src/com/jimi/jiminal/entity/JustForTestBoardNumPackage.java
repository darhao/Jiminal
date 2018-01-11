package com.jimi.jiminal.entity;

import java.util.Date;

import com.jimi.jiminal.annotation.Parse;
import com.jimi.jiminal.annotation.Protocol;
import com.jimi.jiminal.constant.JustForTestLine;

@Protocol(0x42)
public class JustForTestBoardNumPackage extends BasePackage {

	@Parse({ 0, 1 })
	private JustForTestLine justForTestLine;
	@Parse({ 1, 4 })
	private Date timestamp;
	@Parse(value={ 5, 3 }, sign=true)
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
