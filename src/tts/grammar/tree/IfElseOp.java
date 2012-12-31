package tts.grammar.tree;

import tts.eval.*;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class IfElseOp implements IOp {
	IOp cond, body, else_body;

	public IfElseOp(IOp cond, IOp body, IOp else_body) {
		this.cond = cond;
		this.body = body;
		this.else_body = else_body;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval ve = cond.eval(vm);
		if (ve.getType() != IValueEval.Type.BOOLEAN)
			throw new ScriptRuntimeException("");

		BooleanEval be = (BooleanEval) ve;
		if (be.getValue())
			body.eval(vm);
		else if (else_body != null)
			else_body.eval(vm);
		return VoidEval.instance;
	}
}
