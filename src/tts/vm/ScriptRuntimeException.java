package tts.vm;

import tts.grammar.tree.IOp;

/**
 * 脚本运行过程中的异常
 */
public class ScriptRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String file;
	private int line;

	public ScriptRuntimeException(String description, String file, int line) {
		super(description);
		this.file = file;
		this.line = line;
	}

	public ScriptRuntimeException(String description, IOp op) {
		super(description);
		this.file = op.getFile();
		this.line = op.getLine();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("File \"").append(file).append("\", line ").append(line)
				.append(": ").append(getMessage());
		sb.append("\n");
		return sb.toString();
	}

	public String toString(String module) {
		StringBuilder sb = new StringBuilder();
		sb.append("File \"").append(file).append("\", line ").append(line)
				.append(", module ").append(module).append(": ")
				.append(getMessage());
		sb.append("\n");
		return sb.toString();
	}
}
