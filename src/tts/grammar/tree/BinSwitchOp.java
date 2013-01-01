package tts.grammar.tree;

import tts.eval.BooleanEval;
import tts.eval.IValueEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class BinSwitchOp implements IOp {

	IOp cond, true_value, false_value;

	public BinSwitchOp(IOp c, IOp t, IOp f) {
		this.cond = c;
		this.true_value = t;
		this.false_value = f;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval c = cond.eval(vm);
		if (c.getType() != IValueEval.EvalType.BOOLEAN)
			throw new ScriptRuntimeException("boolean value needed");

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
					throw new ScriptRuntimeException("boolean value needed");

				if (((BooleanEval) c).getValue())
					return true_value;
				return false_value;
			}
		}
		return this;
	}
}
