package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.BreakLoopException;
import tts.vm.ScriptVM;

public final class BreakOp implements IOp {

	String file;
	int line;

	public BreakOp(String file, int line) {
		this.file = file;
		this.line = line;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new BreakLoopException(file, line);
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
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
