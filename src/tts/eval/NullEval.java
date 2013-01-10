package tts.eval;

public final class NullEval implements IValueEval {

	private NullEval() {
	}

	public static final NullEval instance = new NullEval();

	@Override
	public String toString() {
		return "(null)";
	}

	@Override
	public EvalType getType() {
		return EvalType.NULL;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return 457;
	}
}
