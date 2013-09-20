package tts.eval;

import java.util.List;

import tts.util.SourceLocation;
import tts.vm.Frame;

public abstract class FunctionEval implements IValueEval {

	public abstract IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl);

	public String getFunctionName() {
		return SourceLocation.NATIVE_FUNCTION;
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
