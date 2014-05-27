package tts.trace;

public class SourceLocation {

	public static final String NATIVE_FUNCTION = "<native_module>";
	public static final SourceLocation NATIVE = new SourceLocation("<native_file>", -1);

	public final String file;
	public final int line;

	public SourceLocation(String file, int line) {
		this.file = file;
		this.line = line;
	}
}
