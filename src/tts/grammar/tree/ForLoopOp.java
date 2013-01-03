package tts.grammar.tree;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.*;
import tts.vm.rtexcpt.*;

public final class ForLoopOp implements IOp {

	SourceLocation sl;
	IOp init_exp, break_exp, fin_exp, body;

	public ForLoopOp(IOp init, IOp brk, IOp fin, IOp body, String file, int line) {
		this.init_exp = init;
		this.break_exp = brk;
		this.fin_exp = fin;
		this.body = body;
		this.sl = new SourceLocation(file, line);
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		vm.enterFrame();
		if (init_exp != null)
			init_exp.eval(vm);
		while (true) {
			if (break_exp != null) {
				IValueEval ve = break_exp.eval(vm);
				if (ve.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("boolean value needed",
							break_exp);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("for(");
		if (init_exp != null)
			sb.append(init_exp);
		sb.append(";");
		if (break_exp != null)
			sb.append(break_exp);
		sb.append(";");
		if (fin_exp != null)
			sb.append(fin_exp);
		sb.append(")\n").append(body);
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
