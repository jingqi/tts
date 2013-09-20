package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.Op;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptNullPointerException;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class MemberOp extends Op {

	Op body;
	String member;

	public MemberOp(Op body, String member) {
		super(body.getSourceLocation());
		this.body = body;
		this.member = member;
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval b = body.eval(f);
		if (b.getType() == EvalType.NULL)
			throw new ScriptNullPointerException(body.getSourceLocation());
		else if (!(b instanceof ObjectEval))
			throw new ScriptRuntimeException("Value/object has no such member", body.getSourceLocation());

		return ((ObjectEval) b).member(member, getSourceLocation());
	}

	@Override
	public Op optimize() {
		body = body.optimize();
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(body).append(".").append(member);
		return sb.toString();
	}
}
