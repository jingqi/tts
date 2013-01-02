package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.*;

public final class BreakOp implements IOp {

	SourceLocation sl;

	public BreakOp(String file, int line) {
		this.sl = new SourceLocation(file, line);
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new BreakLoopException(sl);
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "break;";
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
