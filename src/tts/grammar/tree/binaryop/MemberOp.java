package tts.grammar.tree.binaryop;

import tts.eval.IValueEval;
import tts.eval.ObjectEval;
import tts.grammar.tree.IOp;
import tts.vm.*;

public final class MemberOp implements IOp {

	IOp body;
	String member;

	public MemberOp(IOp body, String member) {
		this.body = body;
		this.member = member;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval b = body.eval(vm);
		if (!(b instanceof ObjectEval))
			throw new ScriptRuntimeException("value/object has no member", body);

		return ((ObjectEval) b).member(member, getSourceLocation());
	}

	@Override
	public IOp optimize() {
		body = body.optimize();
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(body).append(".").append(member);
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return body.getSourceLocation();
	}

}
