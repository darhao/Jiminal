package com.jimi.smt.eps.pkh.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Protocol {

	/**
	 * 协议号
	 * @return
	 */
	int code();
	
	/**
	 * 协议名
	 * @return
	 */
	String name();
	
}
