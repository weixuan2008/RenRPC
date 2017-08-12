package com.hy.ren.rpc.model;

/**
 * 
 * Description: The encapsulation for RPC response.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class Response extends Packet{
	private static final long serialVersionUID = -3245882881289452416L;
	
	private Throwable exception;
	private Object result;
	
	public Throwable getException() {
		return exception;
	}
	
	public void setException(Throwable exception) {
		this.exception = exception;
	}
	
	public Object getResult() {
		return result;
	}
	
	public void setResult(Object result) {
		this.result = result;
	}
}
