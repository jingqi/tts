package tts.scanner;

public class Token {

	public enum TokenType {
		TEXT_TEMPLATE, BOOLEAN, INTEGER, LONG_INT, FLOAT, DOUBLE, STRING, SEPARATOR, IDENTIFIER, KEY_WORD
	}

	TokenType type;
	Object value;

	public Token(TokenType t, Object value) {
		this.type = t;
		this.value = value;
	}
}
