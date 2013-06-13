package tts.vm.rtexcept;

import tts.util.SourceLocation;

/**
 * continue a loop
 */
public final class ContinueLoopException extends ScriptLogicException {

	private static final long serialVersionUID = 1L;

	public ContinueLoopException(String file, int line) {
		super(new SourceLocation(file, line));
	}

	public ContinueLoopException(SourceLocation sl) {
		super(sl);
	}
}
