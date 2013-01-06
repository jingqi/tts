package tts.grammar.tree;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.ScriptRuntimeException;

public final class IncludeOp implements IOp {

	SourceLocation sl;
	String path;

	public IncludeOp(String path, String file, int line) {
		this.path = path;
		sl = new SourceLocation(file, line);
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

		vm.pushCallFrame(sl, SourceLocation.NATIVE_MODULE);
		vm.enterFrame();
		vm.pushScriptPath(dst);
		if (op != null)
			op.eval(vm);
		vm.popScriptPath();
		vm.leaveFrame();
		vm.popCallFrame();

		return VoidEval.instance;
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
