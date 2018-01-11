package com.jimi.jiminal.annotation;

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
	
	/**
	 * 只对类型为int的字段有效，true表示为有符号，false为无符号，默认为false<br>
	 * 注意：只对32位以下数值有效，32位以上会表现为有符号
	 */
	boolean sign() default false;
	
}
