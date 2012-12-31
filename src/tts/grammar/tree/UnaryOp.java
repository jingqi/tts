package tts.grammar.tree;

import tts.eval.*;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

/**
 * 一元操作符
 */
public class UnaryOp implements IOp {

	public enum Op {
		POSITIVE, NEGATIEVE,
	}

	private Op op;
	private IOp eval;

	public UnaryOp(Op op, IOp e) {
		this.op = op;
		this.eval = e;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {

		IValueEval ve = eval.eval(vm);
		if (op == Op.POSITIVE)
			return ve;

		switch (ve.getType()) {
		case INTEGER:
			IntegerEval ie = (IntegerEval) ve;
			return new IntegerEval(-ie.getValue());

		case LONG_INT:
			LongIntEval lie = (LongIntEval) ve;
			return new LongIntEval(-lie.getValue());

		case FLOAT:
			FloatEval fe = (FloatEval) ve;
			return new FloatEval(-fe.getValue());

		case DOUBLE:
			DoubleEval de = (DoubleEval) ve;
			return new DoubleEval(-de.getValue());
		}

		throw new ScriptRuntimeException("illegal operation");
	}
}
