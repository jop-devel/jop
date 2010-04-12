package com.jopdesign.wcet.annotations;

public class Token {
	int kind;    // token kind
	int pos;     // token position in the source text (starting at 0)
	int col;     // token column (starting at 1)
	int line;    // token line (starting at 1)
	String val;  // token value
	Token next;  // ML 2005-03-11 Peek tokens are kept in linked list
}
