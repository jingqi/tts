package tts.token.scanner;

public class Token {

	public enum TokenType {
		TEXT_TEMPLATE, BOOLEAN, INTEGER, DOUBLE, STRING, SEPARATOR, IDENTIFIER, KEY_WORD
	}

	public TokenType type;
	public Object value;

	public Token(TokenType t, Object value) {
		this.type = t;
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
