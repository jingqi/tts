package tts.grammar.tree;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.ScriptRuntimeException;

/**
 * 操作数
 */
public final class Operand implements IOp {

	SourceLocation sl;
	IValueEval eval;

	public Operand(IValueEval ve, SourceLocation sl) {
		this.sl = sl;
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
	public IValueEval eval(ScriptVM vm) {
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
			IValueEval ret = vm.getVariable(ve.getName(), sl).getValue();
			if (ret == null)
				throw new ScriptRuntimeException("variable not initialized",
						this);
			return ret;
		}
		throw new ScriptRuntimeException("wrong type of value", this);
	}

	@Override
	public IOp optimize() {
		if (eval instanceof UserFunctionEval) {
			((UserFunctionEval) eval).optimize();
		}
		return this;
	}

	@Override
	public String toString() {
		return eval.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
