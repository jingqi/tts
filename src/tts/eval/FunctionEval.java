package tts.eval;

import java.util.List;

public abstract class FunctionEval implements IValueEval {

	public static final String NATIVE_MODULE = "<native_module>";
	public static final String NATIVE_FILE = "<native_file>";
	public static final int NATIVE_LINE = -1;

	public abstract IValueEval call(List<IValueEval> args);

	public String getModuleName() {
		return NATIVE_MODULE;
	}

	@Override
	public EvalType getType() {
		return EvalType.FUNCTION;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}
