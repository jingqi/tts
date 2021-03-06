package tts.eval;

public final class VoidEval implements IValueEval {

	private VoidEval() {
	}

	public static final VoidEval instance = new VoidEval();

	@Override
	public String toString() {
		return "void";
	}

	@Override
	public EvalType getType() {
		return EvalType.VOID;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return 875;
	}
}
