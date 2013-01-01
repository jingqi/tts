package tts.grammar.tree;

import tts.eval.*;
import tts.vm.*;

public class ForLoopOp implements IOp {

	IOp init_exp, break_exp, fin_exp, body;

	public ForLoopOp(IOp init, IOp brk, IOp fin, IOp body) {
		this.init_exp = init;
		this.break_exp = brk;
		this.fin_exp = fin;
		this.body = body;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		if (init_exp != null)
			init_exp.eval(vm);

		vm.enterFrame();
		while (true) {
			if (break_exp != null) {
				IValueEval ve = break_exp.eval(vm);
				if (ve.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("");
				BooleanEval be = (BooleanEval) ve;
				if (!be.getValue())
					break;
			}

			try {
				if (body != null)
					body.eval(vm);
			} catch (BreakLoopException e) {
				break;
			} catch (ContinueLoopException e) {
				// do nothing
			}

			if (fin_exp != null)
				fin_exp.eval(vm);
		}
		vm.leaveFrame();

		return VoidEval.instance;
	}

	@Override
	public IOp optimize() {
		if (init_exp != null)
			init_exp = init_exp.optimize();
		if (break_exp != null)
			break_exp = break_exp.optimize();
		if (fin_exp != null)
			fin_exp = fin_exp.optimize();
		if (body != null)
			body = body.optimize();
		return this;
	}
}
