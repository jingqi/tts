package tts.grammar.tree;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class IfElseOp extends Op {

	Op cond, body, else_body;

	public IfElseOp(Op cond, Op body, Op else_body, String file, int line) {
		super(new SourceLocation(file, line));
		this.cond = cond;
		this.body = body;
		this.else_body = else_body;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval ve = cond.eval(vm);
		if (ve.getType() != IValueEval.EvalType.BOOLEAN)
			throw new ScriptRuntimeException("Boolean value needed", cond.getSourceLocation());

		BooleanEval be = (BooleanEval) ve;
		if (be.getValue()) {
			if (body != null)
				body.eval(vm);
		} else if (else_body != null) {
			else_body.eval(vm);
		}
		return VoidEval.instance;
	}

	@Override
	public Op optimize() {
		cond = cond.optimize();
		if (body != null)
			body = body.optimize();
		if (else_body != null)
			else_body = else_body.optimize();

		// 优化常量
		if (cond instanceof Operand) {
			if (((Operand) cond).isConst()) {
				IValueEval ve = cond.eval(null);
				if (ve.getType() != IValueEval.EvalType.BOOLEAN)
					throw new ScriptRuntimeException("Boolean value needed",
							cond.getSourceLocation());

				BooleanEval be = (BooleanEval) ve;
				if (be.getValue())
					return body;
				return else_body;
			}
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("if(").append(cond).append(")\n").append(body);
		if (else_body != null)
			sb.append("else\n").append(else_body);
		sb.append("\n");
		return sb.toString();
	}
}
