package tts.grammar.tree;

import tts.eval.*;
import tts.eval.function.UserFunctionEval;
import tts.eval.scope.EvalSlot;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

/**
 * 操作数
 */
public final class Operand extends Op {

	IValueEval eval;

	public Operand(IValueEval ve, SourceLocation sl) {
		super(sl);
		this.eval = ve;
	}

	public Operand(IValueEval ve, String file, int line) {
		this(ve, new SourceLocation(file, line));
	}

	public IValueEval getOperand() {
		return eval;
	}

	public boolean isConst() {
		switch (eval.getType()) {
		case VOID:
		case NULL:
		case BOOLEAN:
		case INTEGER:
		case DOUBLE:
		case STRING:
			return true;
		}
		return false;
	}

	@Override
	public IValueEval eval(Frame f) {
		if (eval == null)
			return null;

		switch (eval.getType()) {
		case VOID:
		case NULL:
		case BOOLEAN:
		case INTEGER:
		case DOUBLE:
		case FUNCTION:
			return eval;

		case STRING:
			return ((StringEval)eval).clone();

		case VARIABLE:
			VariableEval ve = (VariableEval) eval;
			EvalSlot v = f.getVariable(ve.getName());
			if (v == null)
				throw new ScriptRuntimeException("Variable " + ve.getName() + " not found", getSourceLocation());

			IValueEval ret = v.getValue();
			if (ret == null)
				throw new ScriptRuntimeException("Variable " + ve.getName() + " not initialized", getSourceLocation());
			return ret;
		}
		throw new ScriptRuntimeException("Wrong type of value", getSourceLocation());
	}

	@Override
	public Op optimize() {
		if (eval instanceof UserFunctionEval) {
			((UserFunctionEval) eval).optimize();
		}
		return this;
	}

	@Override
	public String toString() {
		return eval.toString();
	}
}
