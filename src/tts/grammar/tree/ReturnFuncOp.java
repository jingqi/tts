package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.ReturnFuncException;

public class ReturnFuncOp extends Op {

	Op value;

	public ReturnFuncOp(Op v) {
		super(v.getSourceLocation());
		this.value = v;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval v = value.eval(vm);
		throw new ReturnFuncException(v, getSourceLocation().file,
				getSourceLocation().line);
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
