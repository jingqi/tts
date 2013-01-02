package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.IOp;
import tts.grammar.tree.Operand;
import tts.vm.*;

public final class MathOp implements IOp {

	public enum OpType {
		ADD("+"), SUB("-"), MULTIPLY("*"), DIVID("/"), MOD("%");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	OpType op;
	IOp left, right;

	public MathOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval l = left.eval(vm), r = right.eval(vm);
		switch (op) {
		case ADD:
			if (l.getType() == EvalType.STRING
					&& r.getType() == EvalType.STRING) {
				return new StringEval(((StringEval) l).getValue()
						+ ((StringEval) r).getValue());
			} else if (l.getType() == EvalType.ARRAY
					&& r.getType() == EvalType.ARRAY) {
				ArrayEval ret = new ArrayEval();
				ret.addAll((ArrayEval) l);
				ret.addAll((ArrayEval) r);
				return ret;
			}
			break;

		case MULTIPLY:
			if (l.getType() == EvalType.STRING
					&& r.getType() == EvalType.INTEGER) {
				String s = ((StringEval) l).getValue();
				long v = ((IntegerEval) r).getValue();
				StringBuilder sb = new StringBuilder((int) (s.length() * v));
				for (int i = 0; i < v; ++i)
					sb.append(s);
				return new StringEval(sb.toString());
			}
			break;

		}

		double ld = 0;
		long ll = 0;
		switch (l.getType()) {
		case DOUBLE:
			ld = (double) ((DoubleEval) l).getValue();
			ll = (long) ((DoubleEval) l).getValue();
			break;

		case INTEGER:
			ld = (double) ((IntegerEval) l).getValue();
			ll = (long) ((IntegerEval) l).getValue();
			break;

		default:
			throw new ScriptRuntimeException("type mismatch in math operation",
					this);
		}

		double rd = 0;
		long rl = 0;
		switch (r.getType()) {
		case DOUBLE:
			rd = (double) ((DoubleEval) r).getValue();
			rl = (long) ((DoubleEval) r).getValue();
			break;

		case INTEGER:
			rd = (double) ((IntegerEval) r).getValue();
			rl = (long) ((IntegerEval) r).getValue();
			break;

		default:
			throw new ScriptRuntimeException("type mismatch in math operation",
					this);
		}

		double rsd = 0;
		long rsl = 0;
		switch (op) {
		case ADD:
			rsd = ld + rd;
			rsl = ll + rl;
			break;

		case SUB:
			rsd = ld - rd;
			rsl = ll - rl;
			break;

		case MULTIPLY:
			rsd = ld * rd;
			rsl = ll * rl;
			break;

		case DIVID:
			rsd = ld / rd;
			rsl = ll / rl;
			break;

		case MOD:
			rsd = ld % rd;
			rsl = ll % rl;
			break;

		default:
			throw new RuntimeException();
		}

		switch (l.getType()) {
		case DOUBLE:
			return new DoubleEval(rsd);

		case INTEGER:
			if (r.getType() == EvalType.INTEGER)
				return new IntegerEval(rsl);
			return new DoubleEval(rsd);

		default:
			throw new RuntimeException();
		}
	}

	@Override
	public IOp optimize() {
		left = left.optimize();
		right = right.optimize();

		// 优化常量
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
