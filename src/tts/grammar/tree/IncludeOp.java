package tts.grammar.tree;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public final class IncludeOp implements IOp {

	String file;
	int line;
	String path;

	public IncludeOp(String path, String file, int line) {
		this.path = path;
		this.file = file;
		this.line = line;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		File cur = vm.getCurrentScriptPath();
		if (cur == null)
			throw new ScriptRuntimeException("file not found", this);

		File dst = new File(cur.getParentFile().getAbsolutePath() + "/" + path);
		IOp op;
		try {
			op = vm.loadScript(dst);
		} catch (IOException e) {
			throw new ScriptRuntimeException("can not load file:" + path, this);
		}

		vm.pushScriptPath(dst);
		try {
			if (op != null)
				op.eval(vm);
		} finally {
			vm.popScriptPath();
		}

		return null;
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "include \"" + path + "\"";
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
