package tts.grammar.tree;

import tts.eval.BooleanEval;
import tts.eval.IValueEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public final class BinSwitchOp implements IOp {

	String file;
	int line;
	IOp cond, true_value, false_value;

	public BinSwitchOp(IOp c, IOp t, IOp f) {
		this.cond = c;
		this.true_value = t;
		this.false_value = f;
		this.file = c.getFile();
		this.line = c.getLine();
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval c = cond.eval(vm);
		if (c.getType() != IValueEval.EvalType.BOOLEAN)
			throw new ScriptRuntimeException("boolean value needed", cond);

		if (((BooleanEval) c).getValue())
			return true_value.eval(vm);
		return false_value.eval(vm);
	}

	@Override
	public IOp optimize() {
		cond = cond.optimize();
		true_value = true_value.optimize();
		false_value = false_value.optimize();

		if (cond instanceof Operand) {
			if (((Operand) cond).isConst()) {
				IValueEval c = cond.eval(null);
				if (c.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("boolean value needed",
							cond);

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

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
