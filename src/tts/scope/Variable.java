package tts.scope;

import tts.eval.IValueEval;

/**
 * 变量
 */
public final class Variable {

	private final VarType type;
	private IValueEval value;

	public Variable(VarType t, IValueEval v) {
		type = t;
		value = v;
	}

	public VarType getVarType() {
		return type;
	}

	public IValueEval getValue() {
		return value;
	}

	public void setValue(IValueEval v) {
		value = v;
	}
}
