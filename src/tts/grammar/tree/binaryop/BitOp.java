package tts.grammar.tree.binaryop;

import tts.eval.IValueEval;
import tts.eval.IntegerEval;
import tts.grammar.tree.IOp;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class BitOp implements IOp {

	public enum OpType {
		BIT_AND, BIT_OR, BIT_XOR, SHIFT_LEFT, SHIFT_RIGHT, CIRCLE_SHIFT_LEFT, CIRCLE_SHIFT_RIGHT,
	}

	OpType op;
	IOp left, right;

	public BitOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval l = left.eval(vm), r = right.eval(vm);
		if (l.getType() != IValueEval.EvalType.INTEGER
				|| r.getType() != IValueEval.EvalType.INTEGER)
			throw new ScriptRuntimeException();

		long ll = ((IntegerEval) l).getValue();
		long rr = ((IntegerEval) r).getValue();

		switch (op) {
		case BIT_AND:
			return new IntegerEval(ll & rr);

		case BIT_OR:
			return new IntegerEval(ll | rr);

		case BIT_XOR:
			return new IntegerEval(ll ^ rr);

		case SHIFT_LEFT:
			return new IntegerEval(ll << rr);

		case SHIFT_RIGHT:
			return new IntegerEval(ll >> rr);

		case CIRCLE_SHIFT_LEFT:
			return new IntegerEval(ll >>> (64 - (rr % 64)));

		case CIRCLE_SHIFT_RIGHT:
			return new IntegerEval(ll >>> rr);

		default:
			throw new ScriptRuntimeException();
		}
	}
}
