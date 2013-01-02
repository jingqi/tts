package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ContinueLoopException;
import tts.vm.ScriptVM;

public final class ContinueOp implements IOp {

	String file;
	int line;

	public ContinueOp(String file, int line) {
		this.file = file;
		this.line = line;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		throw new ContinueLoopException(file, line);
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
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
