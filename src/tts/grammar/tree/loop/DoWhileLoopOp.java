package tts.grammar.tree.loop;

import tts.eval.*;
import tts.grammar.tree.Op;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.*;

public final class DoWhileLoopOp extends Op {

	Op body, brk_exp;

	public DoWhileLoopOp(Op body, Op brk, String file, int line) {
		super(new SourceLocation(file, line));
		this.body = body;
		this.brk_exp = brk;
	}

	@Override
	public IValueEval eval(Frame f) {
		while (true) {
			try {
				if (body != null)
					body.eval(f);
			} catch (BreakLoopException e) {
				break;
			} catch (ContinueLoopException e) {
				// do nothing
			}

			IValueEval ve = brk_exp.eval(f);
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("Boolean value needed", brk_exp.getSourceLocation());
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
