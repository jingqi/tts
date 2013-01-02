package tts.token.scanner;

import tts.vm.SourceLocation;

public class ScannerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private SourceLocation sl;

	public ScannerException(String description, String file, int line) {
		super(description);
		sl = new SourceLocation(file, line);
	}

	public ScannerException(String description, SourceLocation sl) {
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
