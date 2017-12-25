package com.jimi.smt.eps.pkh.entity;

import com.jimi.smt.eps.pkh.annotation.Parse;
import com.jimi.smt.eps.pkh.constant.ClientDevice;
import com.jimi.smt.eps.pkh.constant.ControlledDevice;
import com.jimi.smt.eps.pkh.constant.Operation;

public class ControlPackage extends Package {
	
	@Parse(position= {1,2})
	private String line;

	private Operation operation;
	
	private ClientDevice clientDevice;
	
	private ControlledDevice controlledDevice;

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public ClientDevice getClientDevice() {
		return clientDevice;
	}

	public void setClientDevice(ClientDevice clientDevice) {
		this.clientDevice = clientDevice;
	}

	public ControlledDevice getControlledDevice() {
		return controlledDevice;
	}

	public void setControlledDevice(ControlledDevice controlledDevice) {
		this.controlledDevice = controlledDevice;
	}

}
