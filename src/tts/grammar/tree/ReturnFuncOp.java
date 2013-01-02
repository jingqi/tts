package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.*;

public class ReturnFuncOp implements IOp {

	IOp value;

	public ReturnFuncOp(IOp v) {
		this.value = v;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval v = value.eval(vm);
		throw new ReturnFuncException(v, getSourceLocation().file,
				getSourceLocation().line);
	}

	@Override
	public IOp optimize() {
		value = value.optimize();
		return this;
	}

	@Override
	public String toString() {
		return "return " + value.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return value.getSourceLocation();
	}
}
