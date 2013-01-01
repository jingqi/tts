package tts.eval;

import java.util.List;

public abstract class FunctionEval implements IValueEval {

	public abstract IValueEval call(List<IValueEval> args);

	@Override
	public EvalType getType() {
		return EvalType.FUNCTION;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}
