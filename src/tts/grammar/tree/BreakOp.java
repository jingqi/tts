package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.BreakLoopException;

public final class BreakOp extends Op {

	public BreakOp(String file, int line) {
		super(new SourceLocation(file, line));
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new BreakLoopException(getSourceLocation());
	}

	@Override
	public Op optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "break;";
	}
}
