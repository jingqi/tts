package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.Type;
import tts.grammar.tree.IOp;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class MathOp implements IOp {

	public enum OpType {
		ADD, SUB, MULTIPLY, DIVID, MOD
	}

	OpType op;
	IOp left, right;

	public MathOp(IOp left, OpType op, IOp right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval l = left.eval(vm), r = right.eval(vm);
		if (op == OpType.ADD && l.getType() == Type.STRING
				&& r.getType() == Type.STRING) {
			return new StringEval(((StringEval) l).getValue()
					+ ((StringEval) r).getValue());
		} else if (op == OpType.MULTIPLY && l.getType() == Type.STRING
				&& r.getType() == Type.INTEGER) {
			String s = ((StringEval) l).getValue();
			long v = ((IntegerEval) r).getValue();
			StringBuilder sb = new StringBuilder((int) (s.length() * v));
			for (int i = 0; i < v; ++i)
				sb.append(s);
			return new StringEval(sb.toString());
		}

		double ld = 0;
		long ll = 0;
		switch (l.getType()) {
		case DOUBLE:
			ld = (double) ((DoubleEval) l).getValue();
			ll = (long) ((DoubleEval) l).getValue();
			break;

		case INTEGER:
			ld = (double) ((IntegerEval) l).getValue();
			ll = (long) ((IntegerEval) l).getValue();
			break;

		default:
			throw new ScriptRuntimeException();
		}

		double rd = 0;
		long rl = 0;
		switch (r.getType()) {
		case DOUBLE:
			rd = (double) ((DoubleEval) r).getValue();
			rl = (long) ((DoubleEval) r).getValue();
			break;

		case INTEGER:
			rd = (double) ((IntegerEval) r).getValue();
			rl = (long) ((IntegerEval) r).getValue();
			break;

		default:
			throw new ScriptRuntimeException();
		}

		double rsd = 0;
		long rsl = 0;
		switch (op) {
		case ADD:
			rsd = ld + rd;
			rsl = ll + rl;
			break;

		case SUB:
			rsd = ld - rd;
			rsl = ll - rl;
			break;

		case MULTIPLY:
			rsd = ld * rd;
			rsl = ll * rl;
			break;

		case DIVID:
			rsd = ld / rd;
			rsl = ll / rl;
			break;

		case MOD:
			rsd = ld % rd;
			rsl = ll % rl;
			break;

		default:
			throw new RuntimeException();
		}

		switch (l.getType()) {
		case DOUBLE:
			return new DoubleEval(rsd);

		case INTEGER:
			if (r.getType() == Type.INTEGER)
				return new IntegerEval(rsl);
			return new DoubleEval(rsd);

		default:
			throw new RuntimeException();
		}
	}
}
