package tts.eval;

import tts.util.SourceLocation;
import tts.vm.Scope;
import tts.vm.Variable;

public class ModuleScopeEval extends ObjectEval {

	private final Scope scope;

	public ModuleScopeEval(Scope s) {
		this.scope = s;
	}

	@Override
	public IValueEval member(String name, SourceLocation sl) {
		Variable v = scope.getVariable(name);
		if (v != null)
			return v.getValue();
		return null;
	}

}
