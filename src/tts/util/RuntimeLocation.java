package tts.util;

public class RuntimeLocation {

	public static final RuntimeLocation NATIVE = new RuntimeLocation(SourceLocation.NATIVE,
			SourceLocation.NATIVE_MODULE);

	public final SourceLocation sl;
	public final String module;

	public RuntimeLocation(SourceLocation sl, String module) {
		this.sl = sl;
		this.module = module;
	}
}
