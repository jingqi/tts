package tts.eval;

public class VoidValue implements IEvalValue {

	private VoidValue() {
	}

	public static final VoidValue instance = new VoidValue();
}
