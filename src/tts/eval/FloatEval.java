package tts.eval;

public class FloatEval implements IValueEval {

	float value;

	public FloatEval(float v) {
		value = v;
	}

	public float getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Float.toString(value);
	}

	@Override
	public Type getType() {
		return Type.FLOAT;
	}
}
