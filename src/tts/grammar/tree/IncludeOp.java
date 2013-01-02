package tts.grammar.tree;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.vm.*;

public final class IncludeOp implements IOp {

	SourceLocation sl;
	String path;

	public IncludeOp(String path, String file, int line) {
		this.path = path;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		File cur = vm.getCurrentScriptPath();
		if (cur == null)
			throw new ScriptRuntimeException("file not found", this);

		File dst = new File(cur.getParentFile().getAbsolutePath() + "/" + path);
		IOp op;
		try {
			op = vm.loadScript(dst, sl);
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
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
