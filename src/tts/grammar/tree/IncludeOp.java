package tts.grammar.tree;

import java.io.File;
import java.io.IOException;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.util.SourceLocation;
import tts.vm.Frame;

public final class IncludeOp extends Op {

	private final String path;

	public IncludeOp(String path, String file, int line) {
		super(new SourceLocation(file, line));
		this.path = path;
	}

	@Override
	public IValueEval eval(Frame f) {
		File cur = new File(getSourceLocation().file);
		File dst = new File(cur.getParentFile().getAbsolutePath() + "/" + path);
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
