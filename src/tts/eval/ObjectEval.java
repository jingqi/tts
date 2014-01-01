package tts.eval;

import tts.eval.scope.EvalSlot;


public abstract class ObjectEval implements IValueEval {

	/**
	 * 取右值成员
	 */
	public abstract IValueEval member(String name);

	/**
	 * 取左值成员
	 */
	public abstract EvalSlot lvalueMember(String name);

	@Override
	public EvalType getType() {
		return EvalType.OBJECT;
	}
}
