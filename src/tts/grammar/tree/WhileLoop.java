package tts.grammar.tree;

import tts.eval.*;
import tts.vm.*;

public class WhileLoop implements IOp {

	IOp brk_exp, body;

	public WhileLoop(IOp brk, IOp body) {
		this.brk_exp = brk;
		this.body = body;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {

		while (true) {

			IValueEval ve = brk_exp.eval(vm);
			if (ve.getType() != IValueEval.Type.BOOLEAN)
				throw new ScriptRuntimeException("");
			BooleanEval be = (BooleanEval) ve;
			if (!be.getValue())
				break;

			try {
				body.eval(vm);
			} catch (BreakLoopException e) {
				break;
			} catch (ContinueLoopException e) {
				// do nothing
			}
		}
		return VoidEval.instance;
	}

}
