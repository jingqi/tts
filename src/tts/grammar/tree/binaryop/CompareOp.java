package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.Op;
import tts.grammar.tree.Operand;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class CompareOp extends Op {

	public enum OpType {
		REF_EQ("==="), REF_NOT_EQ("!=="), EQ("=="), NOT_EQ("!="), LESS("<"), GREATER(
				">"), LESS_EQ("<="), GREATER_EQ(">=");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	OpType op;
	Op left, right;

	public CompareOp(Op left, OpType op, Op right) {
		super(left.getSourceLocation());
		this.left = left;
		this.op = op;
		this.right = right;
	}

	static boolean eq(IValueEval l, IValueEval r, SourceLocation sl) {
		// null 需要特殊处理
		if (l.getType() == IValueEval.EvalType.NULL)
			return r.getType() == IValueEval.EvalType.NULL;
		else if (r.getType() == IValueEval.EvalType.NULL)
			return false;

		switch (l.getType()) {
		case BOOLEAN:
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
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
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
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
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
			}

		default:
			if (r.getType() != EvalType.NULL && l.getType() != EvalType.NULL && r.getType() != l.getType())
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
			return l.equals(r);
		}
	}

	static boolean less(IValueEval l, IValueEval r, SourceLocation sl) {
		switch (l.getType()) {
		case STRING:
			if (r.getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
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
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
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
				throw new ScriptRuntimeException("Type mismatch in comparison",
						sl);
			}

		default:
			throw new ScriptRuntimeException("Type mismatch in comparison", sl);
		}
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval l = left.eval(f), r = right.eval(f);

		boolean rs = false;
		switch (op) {
		case REF_EQ:
			return BooleanEval.valueOf(l == r);

		case REF_NOT_EQ:
			return BooleanEval.valueOf(l != r);

		case EQ:
			rs = eq(l, r, getSourceLocation());
			break;

		case NOT_EQ:
			rs = !eq(l, r, getSourceLocation());
			break;

		case LESS:
			rs = less(l, r, getSourceLocation());
			break;

		case GREATER:
			rs = less(r, l, getSourceLocation());
			break;

		case LESS_EQ:
			rs = !less(r, l, getSourceLocation());
			break;

		case GREATER_EQ:
			rs = !less(l, r, getSourceLocation());
		}
		return BooleanEval.valueOf(rs);
	}

	@Override
	public Op optimize() {
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
}
