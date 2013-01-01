package tts.eval;

public class StringEval implements IValueEval {

	private String value;

	public StringEval(String s) {
		value = s;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}

	@Override
	public EvalType getType() {
		return EvalType.STRING;
	}
}
