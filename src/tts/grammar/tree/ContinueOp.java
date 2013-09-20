package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.util.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ContinueLoopException;

public final class ContinueOp extends Op {

	public ContinueOp(String file, int line) {
		super(new SourceLocation(file, line));
	}

	@Override
	public IValueEval eval(Frame f) {
		throw new ContinueLoopException(getSourceLocation());
	}

	@Override
	public Op optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "continue;";
	}
}
