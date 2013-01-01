package tts.eval;

public class IntegerEval implements IValueEval {

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
	public Type getType() {
		return Type.INTEGER;
	}
}
