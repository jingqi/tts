package tts.eval;

public class IntegerEval implements IValueEval {

	private int value;

	public IntegerEval(int v) {
		value = v;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

	@Override
	public Type getType() {
		return Type.INTEGER;
	}
}
