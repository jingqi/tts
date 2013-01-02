package tts.grammar.tree.binaryop;

import tts.eval.IValueEval;
import tts.eval.IntegerEval;
import tts.grammar.tree.IOp;
import tts.grammar.tree.Operand;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public final class BitOp implements IOp {

	public enum OpType {
		BIT_AND("&"), BIT_OR("|"), BIT_XOR("^"), SHIFT_LEFT("<<"), SHIFT_RIGHT(
				">>"), CIRCLE_SHIFT_LEFT("<<<"), CIRCLE_SHIFT_RIGHT(">>>");

		String op;

		OpType(String op) {
			this.op = op;
		}
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
			throw new ScriptRuntimeException("type not match in bit operation",
					this);

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
			throw new RuntimeException();
		}
	}

	@Override
	public IOp optimize() {
		left = left.optimize();
		right = right.optimize();

		// 优化常量运算
		if (left instanceof Operand && right instanceof Operand) {
			if (((Operand) left).isConst() && ((Operand) right).isConst()) {
				return new Operand(eval(null), getFile(), getLine());
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
	public String getFile() {
		return left.getFile();
	}

	@Override
	public int getLine() {
		return left.getLine();
	}
}
