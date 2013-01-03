package tts.vm.rtexcpt;

import tts.util.SourceLocation;

public class ContinueLoopException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final SourceLocation sl;

	public ContinueLoopException(String file, int line) {
		sl = new SourceLocation(file, line);
	}

	public ContinueLoopException(SourceLocation sl) {
		this.sl = sl;
	}

	@Override
	public String toString() {
		return "File \"" + sl.file + "\", line " + sl.line;
	}
}
