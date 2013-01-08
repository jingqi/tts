package tts.vm.rtexcpt;

import tts.util.SourceLocation;

/**
 * exit program
 */
public final class ExitException extends ScriptLogicException {

	private static final long serialVersionUID = 1L;

	public ExitException(SourceLocation sl) {
		super(sl);
	}

}
