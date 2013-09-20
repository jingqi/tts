package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.Frame;
import tts.vm.rtexcept.ReturnFuncException;

public class ReturnFuncOp extends Op {

	Op value;

	public ReturnFuncOp(Op v) {
		super(v.getSourceLocation());
		this.value = v;
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval v = value.eval(f);
		throw new ReturnFuncException(v, getSourceLocation());
	}

	@Override
	public Op optimize() {
		value = value.optimize();
		return this;
	}

	@Override
	public String toString() {
		return "return " + value.toString();
	}
}
