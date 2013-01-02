package tts.vm;

import tts.eval.IValueEval;

public class ReturnFuncException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final SourceLocation sl;
	public final IValueEval value;

	public ReturnFuncException(IValueEval v, String file, int line) {
		this.value = v;
		sl = new SourceLocation(file, line);
	}

	public ReturnFuncException(IValueEval v, SourceLocation sl) {
		this.value = v;
		this.sl = sl;
	}

	@Override
	public String toString() {
		return "File \"" + sl.file + "\", line " + sl.line;
	}
}
