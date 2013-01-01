package tts.eval;

public abstract class ObjectEval implements IValueEval {

	public abstract IValueEval member(String name);

	@Override
	public EvalType getType() {
		return EvalType.OBJECT;
	}
}
