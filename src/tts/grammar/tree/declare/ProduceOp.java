package tts.grammar.tree.declare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import tts.eval.IValueEval;
import tts.eval.StringEval;
import tts.eval.VoidEval;
import tts.grammar.tree.Op;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class ProduceOp extends Op {

	private Op path;

	public ProduceOp(Op path, String file, int line) {
		super(new SourceLocation(file, line));
		this.path = path;
	}

	@Override
	public IValueEval eval(Frame f) {
		final SourceLocation sl = getSourceLocation();
		IValueEval ve = path.eval(f);
		if (!(ve instanceof StringEval))
			throw new ScriptRuntimeException("String value expected", sl);
		String p = ((StringEval) ve).getValue();

		File of = new File(p);
		if (!of.isAbsolute()) {
			String parent = new File(sl.file).getParentFile().getAbsolutePath();
			of = new File(parent, p);
		}

		try {
			f.getVM().setTextOutput(new OutputStreamWriter(new FileOutputStream(of), "UTF-8"));
		} catch (IOException e) {
			throw new ScriptRuntimeException("Can not write to file: " + p, sl);
		}

		return VoidEval.instance;
	}

	@Override
	public Op optimize() {
		path = path.optimize();
		return this;
	}

	@Override
	public String toString() {
		return "produce " + path.toString() + ";";
	}
}
