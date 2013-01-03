package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.util.SourceLocation;
import tts.vm.*;
import tts.vm.rtexcpt.ContinueLoopException;

public final class ContinueOp implements IOp {

	SourceLocation sl;

	public ContinueOp(String file, int line) {
		this.sl = new SourceLocation(file, line);
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new ContinueLoopException(sl);
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "continue;";
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
