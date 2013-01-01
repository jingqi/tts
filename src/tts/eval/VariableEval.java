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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof VariableEval))
			return false;
		return ((VariableEval) o).name.equals(name);
	}
}
