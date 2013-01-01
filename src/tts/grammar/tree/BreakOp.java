package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.BreakLoopException;
import tts.vm.ScriptVM;

public class BreakOp implements IOp {

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new BreakLoopException();
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "break;";
	}
}
