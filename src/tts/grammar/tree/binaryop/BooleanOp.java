package tts.grammar.tree.binaryop;

import tts.eval.BooleanEval;
import tts.eval.IValueEval;
import tts.grammar.tree.IOp;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class BooleanOp implements IOp {

	public enum OpType {
		AND, OR
	}

	OpType op;
	IOp left, right;

	public BooleanOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval _l = left.eval(vm);
		if (_l.getType() != IValueEval.EvalType.BOOLEAN)
			throw new ScriptRuntimeException();
		BooleanEval l = (BooleanEval) _l;

		switch (op) {
		case AND: {
			if (!l.getValue())
				return BooleanEval.FALSE;
			IValueEval r = right.eval(vm);
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException();
			return r;
		}

		case OR: {
			if (l.getValue())
				return BooleanEval.TRUE;
			IValueEval r = right.eval(vm);
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException();
			return r;
		}

		default:
			throw new IllegalStateException();
		}
	}
}
