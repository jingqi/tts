package tts.eval.scope;

import tts.eval.IValueEval;

/**
 * 用来存放值的孔
 */
public final class EvalSlot {

	private final VarType type;
	private IValueEval value;

	public EvalSlot(VarType t, IValueEval v) {
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
