package tts.vm;

/**
 * 脚本运行过程中的异常
 */
public class ScriptRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ScriptRuntimeException(String description) {
		super(description);
	}
}
