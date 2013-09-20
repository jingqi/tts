package tts.lexer.scanner;

public class Token {

	public enum TokenType {
		TEXT_TEMPLATE,
		BOOLEAN,
		INTEGER,
		DOUBLE,
		STRING,
		SEPARATOR,
		IDENTIFIER,
		KEY_WORD
	}

	public final TokenType type;
	public final Object value;
	public final String file;
	public final int line;

	public Token(TokenType t, Object value, String file, int line) {
		this.type = t;
		this.value = value;
		this.file = file;
		this.line = line;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
