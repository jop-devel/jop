package com.jopdesign.wcet08.uppaal.model;

public class XmlSerializationException extends Exception {
	private static final long serialVersionUID = 1L;

	public XmlSerializationException() {
	}

	public XmlSerializationException(String message) {
		super(message);
	}

	public XmlSerializationException(Throwable cause) {
		super(cause);
	}

	public XmlSerializationException(String message, Throwable cause) {
		super(message, cause);
	}

}
