package tts.eval;

public class LongIntEval implements IValueEval {

	long value;

	public LongIntEval(long v) {
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
		return Type.LONG_INT;
	}
}
