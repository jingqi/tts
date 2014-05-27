package tts.vm.rtexcept;

import tts.trace.SourceLocation;

/**
 * break a loop
 */
public final class BreakLoopException extends ScriptLogicException {

	private static final long serialVersionUID = 1L;

	public BreakLoopException(String file, int line) {
		super(new SourceLocation(file, line));
	}

	public BreakLoopException(SourceLocation sl) {
		super(sl);
	}
}
