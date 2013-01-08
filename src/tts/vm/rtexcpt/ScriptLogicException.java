package tts.vm.rtexcpt;

import tts.util.SourceLocation;

/**
 * 脚本执行正常流程控制
 */
public abstract class ScriptLogicException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final SourceLocation sl;

	public ScriptLogicException(SourceLocation sl) {
		this.sl = sl;
	}
}
