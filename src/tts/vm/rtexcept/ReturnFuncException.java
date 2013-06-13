package tts.vm.rtexcept;

import tts.eval.IValueEval;
import tts.util.SourceLocation;

/**
 * return from function
 */
public final class ReturnFuncException extends ScriptLogicException {

	private static final long serialVersionUID = 1L;

	public final IValueEval value;

	public ReturnFuncException(IValueEval v, String file, int line) {
		super(new SourceLocation(file, line));
		this.value = v;
	}

	public ReturnFuncException(IValueEval v, SourceLocation sl) {
		super(sl);
		this.value = v;
	}
}
