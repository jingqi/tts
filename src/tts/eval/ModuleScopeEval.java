package tts.eval;

import tts.scope.Variable;
import tts.scope.Scope;

public class ModuleScopeEval extends ObjectEval {

	private final Scope scope;

	public ModuleScopeEval(Scope s) {
		this.scope = s;
	}

	@Override
	public IValueEval member(String name) {
		Variable v = scope.getVariable(name);
		if (v != null)
			return v.getValue();
		return null;
	}

	@Override
	public Variable lvalueMember(String name) {
		return scope.getVariable(name);
	}
}
