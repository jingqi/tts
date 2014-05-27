package tts.grammar.tree.declare;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.eval.StringEval;
import tts.eval.VoidEval;
import tts.grammar.tree.Op;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class IncludeOp extends Op {

	private final Op path;

	public IncludeOp(Op path, String file, int line) {
		super(new SourceLocation(file, line));
		this.path = path;
	}

	@Override
	public IValueEval eval(Frame f) {
		// evaluate path string value
		IValueEval v = path.eval(f);
		if (!(v instanceof StringEval))
			throw new ScriptRuntimeException("String value expected", getSourceLocation());
		String p = ((StringEval) v).getValue();

		// include
		File cur = new File(getSourceLocation().file);
		File dst = new File(cur.getParentFile(), p);
		final Op op;
		try {
			op = f.getVM().loadScript(dst, getSourceLocation());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (op != null)
			op.eval(f);

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
