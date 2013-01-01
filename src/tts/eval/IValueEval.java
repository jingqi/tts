package tts.eval;

public interface IValueEval {

	public enum Type {
		VOID, BOOLEAN, INTEGER, DOUBLE, STRING, VARIABLE,
	}

	Type getType();
}
