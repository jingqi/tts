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
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("");
			BooleanEval be = (BooleanEval) ve;
			if (!be.getValue())
				break;

			try {
				if (body != null)
					body.eval(vm);
			} catch (BreakLoopException e) {
				break;
			} catch (ContinueLoopException e) {
				// do nothing
			}
		}
		return VoidEval.instance;
	}

	@Override
	public IOp optimize() {
		brk_exp = brk_exp.optimize();
		if (body != null)
			body = body.optimize();

		// 优化常量
		if (brk_exp instanceof Operand) {
			if (((Operand) brk_exp).isConst()) {
				IValueEval ve = brk_exp.eval(null);
				if (ve.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("");
				BooleanEval be = (BooleanEval) ve;

				if (!be.getValue())
					return null;
			}
		}

		return this;
	}
}
