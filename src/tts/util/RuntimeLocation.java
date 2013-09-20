package tts.util;

public class RuntimeLocation {

	public static final RuntimeLocation NATIVE = new RuntimeLocation(SourceLocation.NATIVE,
			SourceLocation.NATIVE_FUNCTION);

	public final SourceLocation sl;
	public final String funcName;

	public RuntimeLocation(SourceLocation sl, String module) {
		this.sl = sl;
		this.funcName = module;
	}
}
