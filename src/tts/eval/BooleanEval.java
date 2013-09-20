package tts.eval;

public final class BooleanEval implements IValueEval {

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
		return b ? TRUE : FALSE;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public String toString() {
		return Boolean.toString(value);
	}

	@Override
	public EvalType getType() {
		return EvalType.BOOLEAN;
	}
}
