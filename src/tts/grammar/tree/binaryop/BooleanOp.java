package tts.grammar.tree.binaryop;

import tts.eval.BooleanEval;
import tts.eval.IValueEval;
import tts.grammar.tree.Op;
import tts.grammar.tree.Operand;
import tts.vm.ScriptVM;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class BooleanOp extends Op {

	public enum OpType {
		AND("&&"), OR("||");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	OpType op;
	Op left, right;

	public BooleanOp(Op left, OpType op, Op right) {
		super(left.getSourceLocation());
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval _l = left.eval(vm);
		if (_l.getType() != IValueEval.EvalType.BOOLEAN)
			throw new ScriptRuntimeException(
					"type mismatch for boolean operation", getSourceLocation());
		BooleanEval l = (BooleanEval) _l;

		switch (op) {
		case AND: {
			if (!l.getValue())
				return BooleanEval.FALSE;
			IValueEval r = right.eval(vm);
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException(
						"type mismatch for boolean operation", getSourceLocation());
			return r;
		}

		case OR: {
			if (l.getValue())
				return BooleanEval.TRUE;
			IValueEval r = right.eval(vm);
			if (r.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException(
						"type mismatch for boolean operation", getSourceLocation());
			return r;
		}

		default:
			throw new IllegalStateException();
		}
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
