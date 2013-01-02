package tts.vm;

public class ContinueLoopException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final String file;
	public final int line;

	public ContinueLoopException(String file, int line) {
		this.file = file;
		this.line = line;
	}

	@Override
	public String toString() {
		return "File \"" + file + "\", line " + line;
	}
}
