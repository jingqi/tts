package tts.eval;

public interface IValueEval {

	public enum EvalType {
		VOID, NULL, BOOLEAN, INTEGER, DOUBLE, STRING, VARIABLE, ARRAY, OBJECT, FUNCTION, MAP
	}

	EvalType getType();
}
