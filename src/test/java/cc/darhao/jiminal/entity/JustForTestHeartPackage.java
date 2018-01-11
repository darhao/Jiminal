package cc.darhao.jiminal.entity;

import java.util.Date;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.constant.JustForTestLine;
import cc.darhao.jiminal.core.BasePackage;

@Protocol(0x48)
public class JustForTestHeartPackage extends BasePackage {

	@Parse({0,1})
	private JustForTestLine justForTestLine;
	@Parse({1,4})
	private Date timestamp;
	@Parse({5,2})
	private boolean isAlarmEnabled;
	@Parse({5,1})
	private boolean isConveyorEnabled;
	@Parse({5,0})
	private boolean isInfraredEnabled;

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
