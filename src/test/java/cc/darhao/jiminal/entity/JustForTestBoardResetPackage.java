package cc.darhao.jiminal.entity;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.constant.JustForTestBoardResetReson;
import cc.darhao.jiminal.constant.JustForTestClientDevice;
import cc.darhao.jiminal.constant.JustForTestLine;
import cc.darhao.jiminal.core.BasePackage;

@Protocol(0x52)
public class JustForTestBoardResetPackage extends BasePackage {
	
	@Parse({0,1})
	private JustForTestClientDevice justForTestClientDevice;
	@Parse({1,1})
	private JustForTestLine justForTestLine;
	@Parse({2,1})
	private JustForTestBoardResetReson justForTestBoardResetReson;
	
	public JustForTestClientDevice getClientDevice() {
		return justForTestClientDevice;
	}
	public void setClientDevice(JustForTestClientDevice justForTestClientDevice) {
		this.justForTestClientDevice = justForTestClientDevice;
	}
	public JustForTestLine getLine() {
		return justForTestLine;
	}
	public void setLine(JustForTestLine justForTestLine) {
		this.justForTestLine = justForTestLine;
	}
	public JustForTestBoardResetReson getBoardResetReson() {
		return justForTestBoardResetReson;
	}
	public void setBoardResetReson(JustForTestBoardResetReson justForTestBoardResetReson) {
		this.justForTestBoardResetReson = justForTestBoardResetReson;
	}


}
