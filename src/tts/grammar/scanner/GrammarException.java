package tts.grammar.scanner;

public class GrammarException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GrammarException() {
	}

	public GrammarException(String description) {
		super(description);
	}
}
