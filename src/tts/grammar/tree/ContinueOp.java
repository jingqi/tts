package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ContinueLoopException;
import tts.vm.ScriptVM;

public class ContinueOp implements IOp {

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new ContinueLoopException();
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "continue;";
	}
}
