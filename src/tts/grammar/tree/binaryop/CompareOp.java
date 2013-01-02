package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.grammar.tree.IOp;
import tts.grammar.tree.Operand;
import tts.vm.*;

public final class CompareOp implements IOp {

	public enum OpType {
		EQ("=="), NOT_EQ("!="), LESS("<"), GREATER(">"), LESS_EQ("<="), GREATER_EQ(
				">=");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	OpType op;
	IOp left, right;

	public CompareOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	static boolean eq(IValueEval l, IValueEval r, IOp op) {
		switch (l.getType()) {
		case STRING:
			if (r.getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
			return ((StringEval) l).getValue().equals(
					((StringEval) r).getValue());

		case BOOLEAN:
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
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
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
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
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
			}

		default:
			throw new ScriptRuntimeException("type mismatch in comparison", op);
		}
	}

	static boolean less(IValueEval l, IValueEval r, IOp op) {
		switch (l.getType()) {
		case STRING:
			if (r.getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
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
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
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
				throw new ScriptRuntimeException("type mismatch in comparison",
						op);
			}

		default:
			throw new ScriptRuntimeException("type mismatch in comparison", op);
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval l = left.eval(vm), r = right.eval(vm);

		boolean rs = false;
		switch (op) {
		case EQ:
			rs = eq(l, r, this);
			break;

		case NOT_EQ:
			rs = !eq(l, r, this);
			break;

		case LESS:
			rs = less(l, r, this);
			break;

		case GREATER:
			rs = less(r, l, this);
			break;

		case LESS_EQ:
			rs = !less(r, l, this);
			break;

		case GREATER_EQ:
			rs = !less(l, r, this);
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
				return new Operand(eval(null), getSourceLocation());
			}
		}

		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(left).append(" ").append(op.op).append(" ").append(right);
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return left.getSourceLocation();
	}
}
