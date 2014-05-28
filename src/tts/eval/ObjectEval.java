package tts.eval;

import tts.scope.Variable;


public abstract class ObjectEval implements IValueEval {

	/**
	 * 取右值成员
	 */
	public abstract IValueEval member(String name);

	/**
	 * 取左值成员
	 */
	public abstract Variable lvalueMember(String name);

	@Override
	public EvalType getType() {
		return EvalType.OBJECT;
	}
}
