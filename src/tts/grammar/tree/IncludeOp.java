package tts.grammar.tree;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.*;

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

		vm.enterScriptFile(dst);
		vm.pushFrameLocation(sl, SourceLocation.NATIVE_MODULE);
		try {
			if (op != null)
				op.eval(vm);
		} catch (BreakLoopException e) {
			throw new ScriptRuntimeException("Break without loop", e.sl);
		} catch (ContinueLoopException e) {
			throw new ScriptRuntimeException("Continue without loop", e.sl);
		} catch (ReturnFuncException e) {
			throw new ScriptRuntimeException("Return without function", e.sl);
		} catch (ScriptLogicException e) {
			vm.popFrameLocation();
			vm.leaveScriptFile();
			throw e;
		}
		vm.popFrameLocation();
		vm.leaveScriptFile();

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
