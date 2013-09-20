package tts.grammar.tree;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.*;

public final class WhileLoop extends Op {

	Op brk_exp, body;

	public WhileLoop(Op brk, Op body, SourceLocation sl) {
		super(sl);
		this.brk_exp = brk;
		this.body = body;
	}

	public WhileLoop(Op brk, Op body, String file, int line) {
		this(brk, body, new SourceLocation(file, line));
	}

	@Override
	public IValueEval eval(Frame f) {

		while (true) {

			IValueEval ve = brk_exp.eval(f);
			if (ve.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException("Boolean value needed", brk_exp.getSourceLocation());
			BooleanEval be = (BooleanEval) ve;
			if (!be.getValue())
				break;

			try {
				if (body != null)
					body.eval(f);
			} catch (BreakLoopException e) {
				break;
			} catch (ContinueLoopException e) {
				// do nothing
			}
		}
		return VoidEval.instance;
	}

	@Override
	public Op optimize() {
		brk_exp = brk_exp.optimize();
		if (body != null)
			body = body.optimize();

		// 优化常量
		if (brk_exp instanceof Operand) {
			if (((Operand) brk_exp).isConst()) {
				IValueEval ve = brk_exp.eval(null);
				if (ve.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("Boolean value needed",
							brk_exp.getSourceLocation());
				BooleanEval be = (BooleanEval) ve;

				if (!be.getValue())
					return null;
			}
		}

		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("while(").append(brk_exp).append(")\n").append(body);
		sb.append("\n");
		return sb.toString();
	}
}
