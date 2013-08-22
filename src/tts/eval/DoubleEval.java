package tts.eval;

public final class DoubleEval implements IValueEval {

	private final double value;

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
	public EvalType getType() {
		return EvalType.DOUBLE;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DoubleEval))
			return false;
		return ((DoubleEval) o).value == value;
	}
}
