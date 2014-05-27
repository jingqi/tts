package tts.lexer.scanner;

import tts.trace.SourceLocation;

public class LexerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private SourceLocation sl;

	public LexerException(String description, String file, int line) {
		super(description);
		sl = new SourceLocation(file, line);
	}

	public LexerException(String description, SourceLocation sl) {
		super(description);
		this.sl = sl;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("File \"").append(sl.file).append("\", line ")
				.append(sl.line).append(": ").append(getMessage());
		return sb.toString();
	}
}
