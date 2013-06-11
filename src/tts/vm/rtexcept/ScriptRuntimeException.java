package tts.vm.rtexcept;

import tts.util.SourceLocation;

/**
 * 脚本运行过程中的异常
 */
public class ScriptRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final SourceLocation sl;

	public ScriptRuntimeException(String description, SourceLocation sl) {
		super(description);
		this.sl = sl;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("File \"").append(sl.file).append("\", line ")
				.append(sl.line).append(": ").append(getMessage());
		return sb.toString();
	}
}
