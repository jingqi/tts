package tts.vm;

/**
 * break a loop
 */
public class BreakLoopException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final String file;
	public final int line;

	public BreakLoopException(String file, int line) {
		this.file = file;
		this.line = line;
	}

	@Override
	public String toString() {
		return "File \"" + file + "\", line " + line;
	}
}
