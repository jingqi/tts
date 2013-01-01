package tts.eval;


public class VariableEval implements IValueEval {

	private String name;

	public VariableEval(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public EvalType getType() {
		return EvalType.VARIABLE;
	}
}
