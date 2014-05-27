package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.BreakLoopException;

public final class BreakOp extends Op {

	public BreakOp(String file, int line) {
		super(new SourceLocation(file, line));
	}

	@Override
	public IValueEval eval(Frame f) {
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
