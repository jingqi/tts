package tts.eval;

import java.util.List;

import tts.util.SourceLocation;
import tts.vm.ScriptVM;

public abstract class FunctionEval implements IValueEval {

	public abstract IValueEval call(List<IValueEval> args, ScriptVM vm,
			SourceLocation sl);

	public String getModuleName() {
		return SourceLocation.NATIVE_MODULE;
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
