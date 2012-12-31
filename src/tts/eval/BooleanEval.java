package tts.eval;

public class BooleanEval implements IValueEval {

	private boolean value;

	public static final BooleanEval TRUE = new BooleanEval(true);
	public static final BooleanEval FALSE = new BooleanEval(false);

	private BooleanEval(boolean v) {
		value = v;
	}

	public boolean getValue() {
		return value;
	}

	public static BooleanEval valueOf(boolean b) {
		if (b)
			return TRUE;
		return FALSE;
	}

	@Override
	public String toString() {
		return Boolean.toString(value);
	}

	@Override
	public Type getType() {
		return Type.BOOLEAN;
	}
}
