package tts.grammar.tree;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

/**
 * 一元操作符
 */
public class UnaryOp implements IOp {

	public enum OpType {
		POSITIVE("-"), NEGATIEVE("+"), BIT_NOT("~"), NOT("!");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	private OpType op;
	private IOp eval;

	public UnaryOp(OpType op, IOp e) {
		this.op = op;
		this.eval = e;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {

		IValueEval ve = eval.eval(vm);
		switch (op) {
		case POSITIVE:
			if (ve.getType() != IValueEval.EvalType.DOUBLE
					&& ve.getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException();
			return ve;

		case NEGATIEVE:
			switch (ve.getType()) {
			case DOUBLE:
				return new DoubleEval(-((DoubleEval) ve).getValue());

			case INTEGER:
				return new IntegerEval(-((IntegerEval) ve).getValue());

			default:
				throw new ScriptRuntimeException();
			}

		case NOT:
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException();
			return BooleanEval.valueOf(!((BooleanEval) ve).getValue());

		case BIT_NOT:
			if (ve.getType() == EvalType.INTEGER)
				return new IntegerEval(~((IntegerEval) ve).getValue());
			else
				throw new ScriptRuntimeException();

		default:
			throw new RuntimeException();
		}
	}

	@Override
	public IOp optimize() {
		eval = eval.optimize();

		// 优化常量
		if (eval instanceof Operand) {
			if (((Operand) eval).isConst()) {
				return new Operand(eval(null));
			}
		}

		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(op.op).append(eval);
		return sb.toString();
	}
}
