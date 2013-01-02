package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VariableEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

/**
 * 操作数
 */
public final class Operand implements IOp {

	String file;
	int line;
	IValueEval eval;

	public Operand(IValueEval ve, String file, int line) {
		this.eval = ve;
		this.file = file;
		this.line = line;
	}

	public IValueEval getOperand() {
		return eval;
	}

	public boolean isConst() {
		switch (eval.getType()) {
		case VOID:
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
		switch (eval.getType()) {
		case VOID:
		case BOOLEAN:
		case INTEGER:
		case DOUBLE:
		case STRING:
		case FUNCTION:
			return eval;

		case VARIABLE:
			VariableEval ve = (VariableEval) eval;
			IValueEval ret = vm.getVariable(ve.getName()).getValue();
			if (ret == null)
				throw new ScriptRuntimeException("variable not initialized",
						this);
			return ret;
		}
		throw new ScriptRuntimeException("wrong type of value", this);
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return eval.toString();
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
