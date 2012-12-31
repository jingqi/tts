package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.Type;
import tts.grammar.tree.IOp;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class BitOp implements IOp {

	public enum OpType {
		BIT_AND, BIT_OR, BIT_XOR, SHIFT_LEFT, SHIFT_RIGHT, CIRCLE_SHIFT_RIGHT,
	}

	OpType op;
	IOp left, right;

	public BitOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	static IValueEval bitBooleanOp(IValueEval l, OpType op, IValueEval r) {
		long ll = 0;
		boolean toLong = false;
		switch (l.getType()) {
		case LONG_INT:
			ll = ((LongIntEval) l).getValue();
			toLong = true;
			break;

		case INTEGER:
			ll = ((IntegerEval) l).getValue();
			break;

		default:
			throw new ScriptRuntimeException();
		}

		long rr = 0;
		switch (r.getType()) {
		case LONG_INT:
			ll = ((LongIntEval) r).getValue();
			toLong = true;
			break;

		case INTEGER:
			ll = ((IntegerEval) r).getValue();
			break;

		default:
			throw new ScriptRuntimeException();
		}

		long rs = 0;
		switch (op) {
		case BIT_AND:
			rs = ll & rr;
			break;

		case BIT_OR:
			rs = ll | rr;
			break;

		case BIT_XOR:
			rs = ll ^ rr;
			break;

		default:
			throw new RuntimeException();
		}

		if (toLong)
			return new LongIntEval(rs);
		return new IntegerEval((int) rs);
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval l = left.eval(vm), r = right.eval(vm);

		switch (op) {
		case BIT_AND:
		case BIT_OR:
		case BIT_XOR:
			return bitBooleanOp(l, op, r);
		}

		long rr = 0;
		if (r.getType() == Type.LONG_INT)
			rr = ((LongIntEval) r).getValue();
		else if (r.getType() == Type.INTEGER)
			rr = ((IntegerEval) r).getValue();

		switch (op) {
		case SHIFT_LEFT:
			switch (l.getType()) {
			case LONG_INT:
				return new LongIntEval(((LongIntEval) l).getValue() << rr);

			case INTEGER:
				return new IntegerEval(((IntegerEval) l).getValue() << rr);

			default:
				throw new ScriptRuntimeException();
			}

		case SHIFT_RIGHT:
			switch (l.getType()) {
			case LONG_INT:
				return new LongIntEval(((LongIntEval) l).getValue() >> rr);

			case INTEGER:
				return new IntegerEval(((IntegerEval) l).getValue() >> rr);

			default:
				throw new ScriptRuntimeException();
			}

		case CIRCLE_SHIFT_RIGHT:
			switch (l.getType()) {
			case LONG_INT:
				return new LongIntEval(((LongIntEval) l).getValue() >>> rr);

			case INTEGER:
				return new IntegerEval(((IntegerEval) l).getValue() >>> rr);

			default:
				throw new ScriptRuntimeException();
			}

		default:
			throw new RuntimeException();
		}
	}
}
