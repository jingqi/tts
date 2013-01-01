package tts.eval;

public interface IValueEval {

	public enum EvalType {
		VOID, BOOLEAN, INTEGER, DOUBLE, STRING, VARIABLE, ARRAY,
	}

	EvalType getType();
}
