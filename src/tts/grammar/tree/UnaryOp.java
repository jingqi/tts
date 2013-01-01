package tts.grammar.tree;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.binaryop.AssignOp;
import tts.grammar.tree.binaryop.MathOp;
import tts.vm.*;

/**
 * 一元操作符
 */
public class UnaryOp implements IOp {

	public enum OpType {
		POSITIVE("-"), NEGATIEVE("+"), BIT_NOT("~"), NOT("!"), PRE_INCREMENT(
				"++"), PRE_DECREMENT("--"), POST_INCREMENT("++"), POST_DECREMENT(
				"--");

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
		if (op == OpType.PRE_INCREMENT || op == OpType.PRE_DECREMENT
				|| op == OpType.POST_INCREMENT || op == OpType.POST_DECREMENT) {
			if (!(eval instanceof Operand))
				throw new ScriptRuntimeException();
			IValueEval ve = ((Operand) eval).eval;
			if (ve.getType() != IValueEval.EvalType.VARIABLE)
				throw new ScriptRuntimeException();
			String name = ((VariableEval) ve).getName();
			Variable v = vm.getVariable(name);

			switch (op) {
			case PRE_INCREMENT: {
				IValueEval rs = new MathOp(eval, MathOp.OpType.ADD,
						new Operand(new IntegerEval(1))).eval(vm);
				AssignOp.assign(v, rs);
				return rs;
			}

			case PRE_DECREMENT: {
				IValueEval rs = new MathOp(eval, MathOp.OpType.SUB,
						new Operand(new IntegerEval(1))).eval(vm);
				AssignOp.assign(v, rs);
				return rs;
			}

			case POST_INCREMENT: {
				IValueEval ret = v.getValue();
				IValueEval rs = new MathOp(eval, MathOp.OpType.ADD,
						new Operand(new IntegerEval(1))).eval(vm);
				AssignOp.assign(v, rs);
				return ret;
			}

			case POST_DECREMENT: {
				IValueEval ret = v.getValue();
				IValueEval rs = new MathOp(eval, MathOp.OpType.SUB,
						new Operand(new IntegerEval(1))).eval(vm);
				AssignOp.assign(v, rs);
				return ret;
			}

			default:
				throw new RuntimeException();
			}
		}

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
