package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.grammar.tree.IOp;
import tts.grammar.tree.Operand;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class CompareOp implements IOp {

	public enum OpType {
		EQ, NOT_EQ, LESS, GREATER, LESS_EQ, GREATER_EQ
	}

	OpType op;
	IOp left, right;

	public CompareOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	static boolean eq(IValueEval l, IValueEval r) {
		switch (l.getType()) {
		case STRING:
			if (r.getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException();
			return ((StringEval) l).getValue().equals(
					((StringEval) r).getValue());

		case BOOLEAN:
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("");
			return l == r;

		case DOUBLE:
			switch (r.getType()) {
			case DOUBLE:
				return ((DoubleEval) l).getValue() == ((DoubleEval) r)
						.getValue();

			case INTEGER:
				return ((DoubleEval) l).getValue() == ((IntegerEval) r)
						.getValue();

			default:
				throw new ScriptRuntimeException("");
			}

		case INTEGER:
			switch (r.getType()) {
			case DOUBLE:
				return ((IntegerEval) l).getValue() == ((DoubleEval) r)
						.getValue();

			case INTEGER:
				return ((IntegerEval) l).getValue() == ((IntegerEval) r)
						.getValue();

			default:
				throw new ScriptRuntimeException("");
			}

		default:
			throw new ScriptRuntimeException("");
		}
	}

	static boolean less(IValueEval l, IValueEval r) {
		switch (l.getType()) {
		case STRING:
			if (r.getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException();
			return ((StringEval) l).getValue().compareTo(
					((StringEval) r).getValue()) < 0;

		case DOUBLE:
			switch (r.getType()) {
			case DOUBLE:
				return ((DoubleEval) l).getValue() < ((DoubleEval) r)
						.getValue();

			case INTEGER:
				return ((DoubleEval) l).getValue() < ((IntegerEval) r)
						.getValue();

			default:
				throw new ScriptRuntimeException("");
			}

		case INTEGER:
			switch (r.getType()) {
			case DOUBLE:
				return ((IntegerEval) l).getValue() < ((DoubleEval) r)
						.getValue();

			case INTEGER:
				return ((IntegerEval) l).getValue() < ((IntegerEval) r)
						.getValue();

			default:
				throw new ScriptRuntimeException("");
			}

		default:
			throw new ScriptRuntimeException("");
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval l = left.eval(vm), r = right.eval(vm);

		boolean rs = false;
		switch (op) {
		case EQ:
			rs = eq(l, r);
			break;

		case NOT_EQ:
			rs = !eq(l, r);
			break;

		case LESS:
			rs = less(l, r);
			break;

		case GREATER:
			rs = less(r, l);
			break;

		case LESS_EQ:
			rs = !less(r, l);
			break;

		case GREATER_EQ:
			rs = !less(l, r);
		}
		return BooleanEval.valueOf(rs);
	}

	@Override
	public IOp optimize() {
		left = left.optimize();
		right = right.optimize();

		// 优化常量运算
		if (left instanceof Operand && right instanceof Operand) {
			if (((Operand) left).isConst() && ((Operand) right).isConst()) {
				return new Operand(eval(null));
			}
		}

		return this;
	}
}
