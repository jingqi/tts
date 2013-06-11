package tts.grammar.tree;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcept.*;

public final class DoWhileLoopOp extends Op {

	Op body, brk_exp;

	public DoWhileLoopOp(Op body, Op brk, String file, int line) {
		super(new SourceLocation(file, line));
		this.body = body;
		this.brk_exp = brk;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		while (true) {
			try {
				if (body != null)
					body.eval(vm);
			} catch (BreakLoopException e) {
				break;
			} catch (ContinueLoopException e) {
				// do nothing
			}

			IValueEval ve = brk_exp.eval(vm);
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("boolean value needed",
						brk_exp.getSourceLocation());
			BooleanEval be = (BooleanEval) ve;
			if (!be.getValue())
				break;
		}
		return VoidEval.instance;
	}

	@Override
	public Op optimize() {
		if (body != null)
			body = body.optimize();
		brk_exp = brk_exp.optimize();
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("do").append(body).append("while(").append(brk_exp)
				.append(");\n");
		return sb.toString();
	}
}
