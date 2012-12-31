package tts.eval;

public interface IValueEval {

	public enum Type {
		VOID, BOOLEAN, INTEGER, LONG_INT, FLOAT, DOUBLE, STRING, VARIABLE,
	}

	Type getType();
}
