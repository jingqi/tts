package tts.vm.rtexcpt;

import tts.util.SourceLocation;

/**
 * 脚本中的null异常
 */
public class ScriptNullPointerException extends ScriptRuntimeException {

	private static final long serialVersionUID = 1L;

	public ScriptNullPointerException(SourceLocation sl) {
		super("Null pointer exception", sl);
	}
}
