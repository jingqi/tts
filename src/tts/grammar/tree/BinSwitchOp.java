package tts.grammar.tree;

import tts.eval.BooleanEval;
import tts.eval.IValueEval;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class BinSwitchOp extends Op {

	Op cond, true_value, false_value;

	public BinSwitchOp(Op c, Op t, Op f) {
		super(c.getSourceLocation());
		this.cond = c;
		this.true_value = t;
		this.false_value = f;
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval c = cond.eval(f);
		if (c.getType() != IValueEval.EvalType.BOOLEAN)
			throw new ScriptRuntimeException("Boolean value needed", cond.getSourceLocation());

		if (((BooleanEval) c).getValue())
			return true_value.eval(f);
		return false_value.eval(f);
	}

	@Override
	public Op optimize() {
		cond = cond.optimize();
		true_value = true_value.optimize();
		false_value = false_value.optimize();

		if (cond instanceof Operand) {
			if (((Operand) cond).isConst()) {
				IValueEval c = cond.eval(null);
				if (c.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("Boolean value needed",
							cond.getSourceLocation());

				if (((BooleanEval) c).getValue())
					return true_value;
				return false_value;
			}
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(cond).append(" ? ").append(true_value).append(" : ")
				.append(false_value);
		return sb.toString();
	}
}
