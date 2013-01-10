package tts.grammar.tree;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.binaryop.AssignOp;
import tts.grammar.tree.binaryop.MathOp;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.Variable;
import tts.vm.rtexcpt.ScriptRuntimeException;

/**
 * 一元操作符
 */
public final class UnaryOp extends Op {

	public enum OpType {
		POSITIVE("-"), NEGATIEVE("+"), BIT_NOT("~"), NOT("!"), PRE_INCREMENT(
				"++"), PRE_DECREMENT("--"), POST_INCREMENT("++"), POST_DECREMENT(
				"--");

		String op;

		OpType(String op) {
			this.op = op;
		}
	}

	private SourceLocation sl;
	private OpType op;
	private Op eval;

	public UnaryOp(OpType op, Op e, SourceLocation sl) {
		super(sl);
		this.op = op;
		this.eval = e;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		if (op == OpType.PRE_INCREMENT || op == OpType.PRE_DECREMENT
				|| op == OpType.POST_INCREMENT || op == OpType.POST_DECREMENT) {
			if (!(eval instanceof Operand))
				throw new ScriptRuntimeException("operand can not be assigned",
						eval.getSourceLocation());
			IValueEval ve = ((Operand) eval).eval;
			if (ve.getType() != IValueEval.EvalType.VARIABLE)
				throw new ScriptRuntimeException("operand can not be assigned",
						eval.getSourceLocation());
			String name = ((VariableEval) ve).getName();
			Variable v = vm.getVariable(name, sl);

			switch (op) {
			case PRE_INCREMENT: {
				IValueEval rs = new MathOp(eval, MathOp.OpType.ADD,
						new Operand(new IntegerEval(1), sl)).eval(vm);
				AssignOp.assign(v, rs, sl);
				return rs;
			}

			case PRE_DECREMENT: {
				IValueEval rs = new MathOp(eval, MathOp.OpType.SUB,
						new Operand(new IntegerEval(1), sl)).eval(vm);
				AssignOp.assign(v, rs, sl);
				return rs;
			}

			case POST_INCREMENT: {
				IValueEval ret = v.getValue();
				IValueEval rs = new MathOp(eval, MathOp.OpType.ADD,
						new Operand(new IntegerEval(1), sl)).eval(vm);
				AssignOp.assign(v, rs, sl);
				return ret;
			}

			case POST_DECREMENT: {
				IValueEval ret = v.getValue();
				IValueEval rs = new MathOp(eval, MathOp.OpType.SUB,
						new Operand(new IntegerEval(1), sl)).eval(vm);
				AssignOp.assign(v, rs, sl);
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
						eval.getSourceLocation());
			return ve;

		case NEGATIEVE:
			switch (ve.getType()) {
			case DOUBLE:
				return new DoubleEval(-((DoubleEval) ve).getValue());

			case INTEGER:
				return new IntegerEval(-((IntegerEval) ve).getValue());

			default:
				throw new ScriptRuntimeException("type mismatch for operation",
						eval.getSourceLocation());
			}

		case NOT:
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("type mismatch for operation",
						eval.getSourceLocation());
			return BooleanEval.valueOf(!((BooleanEval) ve).getValue());

		case BIT_NOT:
			if (ve.getType() == EvalType.INTEGER)
				return new IntegerEval(~((IntegerEval) ve).getValue());
			else
				throw new ScriptRuntimeException("type mismatch for operation",
						eval.getSourceLocation());

		default:
			throw new RuntimeException();
		}
	}

	@Override
	public Op optimize() {
		eval = eval.optimize();

		// 优化常量
		if (eval instanceof Operand) {
			if (((Operand) eval).isConst()) {
				return new Operand(eval(null), getSourceLocation());
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
