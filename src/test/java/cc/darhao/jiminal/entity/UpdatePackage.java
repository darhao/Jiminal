package cc.darhao.jiminal.entity;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.pack.BasePackage;
/**
 * 更新包
 * @type UpdatePackage
 * @Company 几米物联技术有限公司-自动化部
 * @author 汤如杰
 * @date 2018年9月11日
 */
@Protocol(0x55)
public class UpdatePackage extends BasePackage {

	@Parse({0,2})
	private int controllId;
	@Parse(value={2,32}, utf8=true)
	private String fingerName;
	@Parse(value= {34,32},utf8=true)
	private String headName;
	@Parse(value= {66,16},utf8=true)
	private String faceName;
	@Parse({82,1})
	private int firstCode;
	@Parse({83,1})
	private int secondCode;
	@Parse({84,1})
	private int debugCode;
	@Parse(value= {85,12},utf8=true)
	private String suffixTime;
	@Parse(value= {97,32},utf8=true)
	private String md5;
	
	
	public int getControllId() {
		return controllId;
	}
	public void setControllId(int controllId) {
		this.controllId = controllId;
	}
	public String getFingerName() {
		return fingerName;
	}
	public void setFingerName(String fingerName) {
		this.fingerName = fingerName;
	}
	public String getHeadName() {
		return headName;
	}
	public void setHeadName(String headName) {
		this.headName = headName;
	}
	public String getFaceName() {
		return faceName;
	}
	public void setFaceName(String faceName) {
		this.faceName = faceName;
	}
	public int getFirstCode() {
		return firstCode;
	}
	public void setFirstCode(int firstCode) {
		this.firstCode = firstCode;
	}
	public int getSecondCode() {
		return secondCode;
	}
	public void setSecondCode(int secondCode) {
		this.secondCode = secondCode;
	}
	public int getDebugCode() {
		return debugCode;
	}
	public void setDebugCode(int debugCode) {
		this.debugCode = debugCode;
	}
	public String getSuffixTime() {
		return suffixTime;
	}
	public void setSuffixTime(String suffixTime) {
		this.suffixTime = suffixTime;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
}
