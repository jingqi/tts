package tts.eval;

public final class IntegerEval implements IValueEval {

	long value;

	public IntegerEval(long v) {
		value = v;
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}

	@Override
	public EvalType getType() {
		return EvalType.INTEGER;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IntegerEval))
			return false;
		return ((IntegerEval) o).value == value;
	}
}
