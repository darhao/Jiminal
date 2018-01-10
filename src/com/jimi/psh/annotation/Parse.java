package com.jimi.psh.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于注解Package的子类的属性
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Parse{
	
	/**
	 * 接收一个长度为2的数组<br>第一个元素为该字段对应数据包<b> 信息内容 </b>的第一个字节位置（从0算起）<br>
	 * 第二个元素为该字段的字节长度<br>
	 * PS：如果字段类型为布尔型，则第一个元素表示该布尔值对应数据包的<b> 信息内容 </b>字节位置（从0算起）<br>
	 * 第二个元素为该值对应字节的bit位置（从0算起，第一位为最右边）
	 */
	int[] value();
	
}
