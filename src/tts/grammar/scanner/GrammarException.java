package tts.grammar.scanner;

import tts.lexer.scanner.TokenStream;
import tts.trace.SourceLocation;

public class GrammarException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private SourceLocation sl;

	public GrammarException(String description, TokenStream ts) {
		this(description, ts.getFile(), ts.getLine());
	}

	public GrammarException(String description, String file, int line) {
		this(description, new SourceLocation(file, line));
	}

	public GrammarException(String description, SourceLocation sl) {
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
