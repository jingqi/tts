package tts.eval;

import tts.eval.scope.EvalSlot;
import tts.eval.scope.Scope;

public class ModuleScopeEval extends ObjectEval {

	private final Scope scope;

	public ModuleScopeEval(Scope s) {
		this.scope = s;
	}

	@Override
	public IValueEval member(String name) {
		EvalSlot v = scope.getVariable(name);
		if (v != null)
			return v.getValue();
		return null;
	}

	@Override
	public EvalSlot lvalueMember(String name) {
		return scope.getVariable(name);
	}
}
