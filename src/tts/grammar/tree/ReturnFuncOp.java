package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ReturnFuncException;
import tts.vm.ScriptVM;

public class ReturnFuncOp implements IOp {
	IOp value;

	public ReturnFuncOp(IOp v) {
		this.value = v;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval v = value.eval(vm);
		throw new ReturnFuncException(v, getFile(), getLine());
	}

	@Override
	public IOp optimize() {
		value = value.optimize();
		return this;
	}

	@Override
	public String getFile() {
		return value.getFile();
	}

	@Override
	public int getLine() {
		return value.getLine();
	}

	@Override
	public String toString() {
		return "return " + value.toString();
	}
}
