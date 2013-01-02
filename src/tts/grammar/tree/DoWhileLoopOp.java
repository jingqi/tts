package tts.grammar.tree;

import tts.eval.*;
import tts.vm.*;

public final class DoWhileLoopOp implements IOp {

	String file;
	int line;
	IOp body, brk_exp;

	public DoWhileLoopOp(IOp body, IOp brk, String file, int line) {
		this.body = body;
		this.brk_exp = brk;
		this.file = file;
		this.line = line;
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
						brk_exp);
			BooleanEval be = (BooleanEval) ve;
			if (!be.getValue())
				break;
		}
		return VoidEval.instance;
	}

	@Override
	public IOp optimize() {
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

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
