package tts.grammar.tree;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.binaryop.AssignOp;
import tts.grammar.tree.binaryop.MathOp;
import tts.vm.*;

/**
 * 一元操作符
 */
public final class UnaryOp implements IOp {

	public enum OpType {
		POSITIVE("-"), NEGATIEVE("+"), BIT_NOT("~"), NOT("!"), PRE_INCREMENT(
				"++"), PRE_DECREMENT("--"), POST_INCREMENT("++"), POST_DECREMENT(
				"--");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	private String file;
	private int line;
	private OpType op;
	private IOp eval;

	public UnaryOp(OpType op, IOp e, String file, int line) {
		this.op = op;
		this.eval = e;
		this.file = file;
		this.line = line;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		if (op == OpType.PRE_INCREMENT || op == OpType.PRE_DECREMENT
				|| op == OpType.POST_INCREMENT || op == OpType.POST_DECREMENT) {
			if (!(eval instanceof Operand))
				throw new ScriptRuntimeException("operand can not be assigned",
						eval);
			IValueEval ve = ((Operand) eval).eval;
			if (ve.getType() != IValueEval.EvalType.VARIABLE)
				throw new ScriptRuntimeException("operand can not be assigned",
						eval);
			String name = ((VariableEval) ve).getName();
			Variable v = vm.getVariable(name);

			switch (op) {
			case PRE_INCREMENT: {
				IValueEval rs = new MathOp(eval, MathOp.OpType.ADD,
						new Operand(new IntegerEval(1), file, line)).eval(vm);
				AssignOp.assign(v, rs, file, line);
				return rs;
			}

			case PRE_DECREMENT: {
				IValueEval rs = new MathOp(eval, MathOp.OpType.SUB,
						new Operand(new IntegerEval(1), file, line)).eval(vm);
				AssignOp.assign(v, rs, file, line);
				return rs;
			}

			case POST_INCREMENT: {
				IValueEval ret = v.getValue();
				IValueEval rs = new MathOp(eval, MathOp.OpType.ADD,
						new Operand(new IntegerEval(1), file, line)).eval(vm);
				AssignOp.assign(v, rs, file, line);
				return ret;
			}

			case POST_DECREMENT: {
				IValueEval ret = v.getValue();
				IValueEval rs = new MathOp(eval, MathOp.OpType.SUB,
						new Operand(new IntegerEval(1), file, line)).eval(vm);
				AssignOp.assign(v, rs, file, line);
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
				throw new ScriptRuntimeException("type mismatch for operation",
						eval);
			return ve;

		case NEGATIEVE:
			switch (ve.getType()) {
			case DOUBLE:
				return new DoubleEval(-((DoubleEval) ve).getValue());

			case INTEGER:
				return new IntegerEval(-((IntegerEval) ve).getValue());

			default:
				throw new ScriptRuntimeException("type mismatch for operation",
						eval);
			}

		case NOT:
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("type mismatch for operation",
						eval);
			return BooleanEval.valueOf(!((BooleanEval) ve).getValue());

		case BIT_NOT:
			if (ve.getType() == EvalType.INTEGER)
				return new IntegerEval(~((IntegerEval) ve).getValue());
			else
				throw new ScriptRuntimeException("type mismatch for operation",
						eval);

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
				return new Operand(eval(null), file, line);
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

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
