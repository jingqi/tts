package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.ScriptLogicException;

public final class ScopOp extends Op {

	Op op;

	public ScopOp(Op op) {
		super(op.getSourceLocation());
		this.op = op;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		vm.enterScope();
		IValueEval ret = VoidEval.instance;
		try {
			if (op != null)
				ret = op.eval(vm);
		} catch (ScriptLogicException e) {
			vm.leaveScope();
			throw e;
		}
		vm.leaveScope();
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
