package tts.eval;

public class VoidEval implements IValueEval {

	private VoidEval() {
	}

	public static final VoidEval instance = new VoidEval();

	@Override
	public String toString() {
		return "void";
	}

	@Override
	public Type getType() {
		return Type.VOID;
	}
}
