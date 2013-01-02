package tts.grammar.scanner;

public class GrammarException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String file;
	private int line;

	public GrammarException(String description, String file, int line) {
		super(description);
		this.file = file;
		this.line = line;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("File \"").append(file).append("\", line ").append(line)
				.append(": ").append(getMessage());
		sb.append("\n");
		return sb.toString();
	}
}
