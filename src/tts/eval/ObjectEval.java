package tts.eval;

import tts.util.SourceLocation;

public abstract class ObjectEval implements IValueEval {

	public abstract IValueEval member(String name, SourceLocation sl);

	@Override
	public EvalType getType() {
		return EvalType.OBJECT;
	}
}
