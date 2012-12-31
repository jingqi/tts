package tts.eval;

public class DoubleEval implements IValueEval {

	private double value;

	public DoubleEval(double v) {
		value = v;
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	@Override
	public Type getType() {
		return Type.DOUBLE;
	}
}
