package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VariableEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

/**
 * 操作数
 */
public class Operand implements IOp {

	IValueEval eval;

	public Operand(IValueEval ve) {
		this.eval = ve;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		switch (eval.getType()) {
		case VOID:
		case BOOLEAN:
		case INTEGER:
		case LONG_INT:
		case FLOAT:
		case DOUBLE:
		case STRING:
			return eval;

		case VARIABLE:
			VariableEval ve = (VariableEval) eval;
			return vm.getVariable(ve.getName()).getValue();
		}
		throw new ScriptRuntimeException("wrong type of value");
	}
}
