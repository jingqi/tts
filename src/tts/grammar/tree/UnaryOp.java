package tts.grammar.tree;

import tts.eval.*;
import tts.eval.IValueEval.Type;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

/**
 * 一元操作符
 */
public class UnaryOp implements IOp {

	public enum OpType {
		POSITIVE, NEGATIEVE, BIT_NOT, NOT
	}

	private OpType op;
	private IOp eval;

	public UnaryOp(OpType op, IOp e) {
		this.op = op;
		this.eval = e;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {

		IValueEval ve = eval.eval(vm);
		switch (op) {
		case POSITIVE:
			if (ve.getType() != IValueEval.Type.DOUBLE
					&& ve.getType() != IValueEval.Type.INTEGER)
				throw new ScriptRuntimeException();
			return ve;

		case NEGATIEVE:
			switch (ve.getType()) {
			case DOUBLE:
				return new DoubleEval(-((DoubleEval) ve).getValue());

			case INTEGER:
				return new IntegerEval(-((IntegerEval) ve).getValue());

			default:
				throw new ScriptRuntimeException();
			}

		case NOT:
			if (ve.getType() != IValueEval.Type.BOOLEAN)
				throw new ScriptRuntimeException();
			return BooleanEval.valueOf(!((BooleanEval) ve).getValue());

		case BIT_NOT:
			if (ve.getType() == Type.INTEGER)
				return new IntegerEval(~((IntegerEval) ve).getValue());
			else
				throw new ScriptRuntimeException();

		default:
			throw new RuntimeException();
		}
	}
}
