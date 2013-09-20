package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.Frame;
import tts.vm.Scope;
import tts.vm.rtexcept.ScriptLogicException;

public final class ScopeOp extends Op {

	Op op;

	public ScopeOp(Op op) {
		super(op.getSourceLocation());
		this.op = op;
	}

	@Override
	public IValueEval eval(Frame f) {
		Scope s = new Scope(f.currentScope());
		f.pushScope(s);
		IValueEval ret = VoidEval.instance;
		try {
			if (op != null)
				ret = op.eval(f);
		} catch (ScriptLogicException e) {
			f.popScope();
			throw e;
		}
		f.popScope();
		return ret;
	}

	@Override
	public Op optimize() {
		if (op != null)
			op = op.optimize();
		if (op == null)
			return null;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n").append(op).append("\n}\n");
		return sb.toString();
	}
}
