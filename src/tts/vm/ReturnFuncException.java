package tts.vm;

import tts.eval.IValueEval;

public class ReturnFuncException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final String file;
	public final int line;
	public final IValueEval value;

	public ReturnFuncException(IValueEval v, String file, int line) {
		this.value = v;
		this.file = file;
		this.line = line;
	}

	@Override
	public String toString() {
		return "File \"" + file + "\", line " + line;
	}
}
