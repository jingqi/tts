package tts.vm.rtexcpt;

import tts.util.SourceLocation;

/**
 * break a loop
 */
public class BreakLoopException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final SourceLocation sl;

	public BreakLoopException(String file, int line) {
		sl = new SourceLocation(file, line);
	}

	public BreakLoopException(SourceLocation sl) {
		this.sl = sl;
	}

	@Override
	public String toString() {
		return "File \"" + sl.file + "\", line " + sl.line;
	}
}
