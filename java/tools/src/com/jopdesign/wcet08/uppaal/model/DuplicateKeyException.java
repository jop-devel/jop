package com.jopdesign.wcet08.uppaal.model;

public class DuplicateKeyException extends Exception {
	private static final long serialVersionUID = 1L;

	public DuplicateKeyException() {
	}

	public DuplicateKeyException(String message) {
		super(message);
	}

	public DuplicateKeyException(Throwable cause) {
		super(cause);
	}

	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

}
