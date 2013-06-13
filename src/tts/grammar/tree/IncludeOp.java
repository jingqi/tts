package tts.grammar.tree;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcept.*;

public final class IncludeOp extends Op {

	String path;

	public IncludeOp(String path, String file, int line) {
		super(new SourceLocation(file, line));
		this.path = path;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		File cur = vm.getCurrentScriptPath();
		if (cur == null)
			throw new ScriptRuntimeException("File not found", getSourceLocation());

		File dst = new File(cur.getParentFile().getAbsolutePath() + "/" + path);
		Op op;
		try {
			op = vm.loadScript(dst, getSourceLocation());
		} catch (IOException e) {
			throw new ScriptRuntimeException("Can not load file:" + path, getSourceLocation());
		}

		vm.enterScriptFile(dst);
		vm.pushFrameLocation(getSourceLocation(), SourceLocation.NATIVE_MODULE);
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
	public Op optimize() {
		return this;
	}

	@Override
	public String toString() {
		return "include \"" + path + "\"";
	}
}
