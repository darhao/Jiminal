package com.jimi.jiminal.entity;

import com.jimi.jiminal.annotation.Parse;
import com.jimi.jiminal.annotation.Protocol;
import com.jimi.jiminal.constant.JustForTestClientDevice;
import com.jimi.jiminal.constant.JustForTestControlledDevice;
import com.jimi.jiminal.constant.JustForTestLine;
import com.jimi.jiminal.constant.JustForTestOperation;

@Protocol((byte) 0xFF)
public class JustForTestControl2Package extends BasePackage {
	
	@Parse({1,1})
	private JustForTestLine justForTestLine;
	@Parse({3,1})
	private Object operation;
	@Parse({0,1})
	private JustForTestClientDevice justForTestClientDevice;
	@Parse({2,1})
	private JustForTestControlledDevice justForTestControlledDevice;

	public JustForTestLine getLine() {
		return justForTestLine;
	}

	public void setLine(JustForTestLine justForTestLine) {
		this.justForTestLine = justForTestLine;
	}

	public JustForTestOperation getOperation() {
		return (JustForTestOperation) operation;
	}

	public void setOperation(JustForTestOperation justForTestOperation) {
		this.operation = justForTestOperation;
	}

	public JustForTestClientDevice getClientDevice() {
		return justForTestClientDevice;
	}

	public void setClientDevice(JustForTestClientDevice justForTestClientDevice) {
		this.justForTestClientDevice = justForTestClientDevice;
	}

	public JustForTestControlledDevice getControlledDevice() {
		return justForTestControlledDevice;
	}

	public void setControlledDevice(JustForTestControlledDevice justForTestControlledDevice) {
		this.justForTestControlledDevice = justForTestControlledDevice;
	}

}
