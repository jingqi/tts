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
		} else if (op == OpType.MULTIPLY && l.getType() == Type.STRING) {
			String s = ((StringEval) l).getValue();
			long v = 0;
			if (r.getType() == Type.INTEGER)
				v = ((IntegerEval) r).getValue();
			else if (r.getType() == Type.LONG_INT)
				v = ((LongIntEval) r).getValue();
			else
				throw new ScriptRuntimeException();
			StringBuilder sb = new StringBuilder((int) (s.length() * v));
			for (int i = 0; i < v; ++i)
				sb.append(s);
			return new StringEval(sb.toString());
		}

		double ld = 0;
		float lf = 0;
		long ll = 0;
		int li = 0;
		switch (l.getType()) {
		case DOUBLE:
			ld = (double) ((DoubleEval) l).getValue();
			lf = (float) ((DoubleEval) l).getValue();
			ll = (long) ((DoubleEval) l).getValue();
			li = (int) ((DoubleEval) l).getValue();
			break;

		case FLOAT:
			ld = (double) ((FloatEval) l).getValue();
			lf = (float) ((FloatEval) l).getValue();
			ll = (long) ((FloatEval) l).getValue();
			li = (int) ((FloatEval) l).getValue();
			break;

		case LONG_INT:
			ld = (double) ((LongIntEval) l).getValue();
			lf = (float) ((LongIntEval) l).getValue();
			ll = (long) ((LongIntEval) l).getValue();
			li = (int) ((LongIntEval) l).getValue();
			break;

		case INTEGER:
			ld = (double) ((IntegerEval) l).getValue();
			lf = (float) ((IntegerEval) l).getValue();
			ll = (long) ((IntegerEval) l).getValue();
			li = (int) ((IntegerEval) l).getValue();
			break;

		default:
			throw new ScriptRuntimeException();
		}

		double rd = 0;
		float rf = 0;
		long rl = 0;
		int ri = 0;
		switch (r.getType()) {
		case DOUBLE:
			rd = (double) ((DoubleEval) r).getValue();
			rf = (float) ((DoubleEval) r).getValue();
			rl = (long) ((DoubleEval) r).getValue();
			ri = (int) ((DoubleEval) r).getValue();
			break;

		case FLOAT:
			rd = (double) ((FloatEval) r).getValue();
			rf = (float) ((FloatEval) r).getValue();
			rl = (long) ((FloatEval) r).getValue();
			ri = (int) ((FloatEval) r).getValue();
			break;

		case LONG_INT:
			rd = (double) ((LongIntEval) r).getValue();
			rf = (float) ((LongIntEval) r).getValue();
			rl = (long) ((LongIntEval) r).getValue();
			ri = (int) ((LongIntEval) r).getValue();
			break;

		case INTEGER:
			rd = (double) ((IntegerEval) r).getValue();
			rf = (float) ((IntegerEval) r).getValue();
			rl = (long) ((IntegerEval) r).getValue();
			ri = (int) ((IntegerEval) r).getValue();
			break;

		default:
			throw new ScriptRuntimeException();
		}

		double rsd = 0;
		float rsf = 0;
		long rsl = 0;
		int rsi = 0;
		switch (op) {
		case ADD:
			rsd = ld + rd;
			rsf = lf + rf;
			rsl = ll + rl;
			rsi = li + ri;
			break;

		case SUB:
			rsd = ld - rd;
			rsf = lf - rf;
			rsl = ll - rl;
			rsi = li - ri;
			break;

		case MULTIPLY:
			rsd = ld * rd;
			rsf = lf * rf;
			rsl = ll * rl;
			rsi = li * ri;
			break;

		case DIVID:
			rsd = ld / rd;
			rsf = lf / rf;
			rsl = ll / rl;
			rsi = li / ri;
			break;

		case MOD:
			rsd = ld % rd;
			rsf = lf % rf;
			rsl = ll % rl;
			rsi = li % ri;
			break;

		default:
			throw new RuntimeException();
		}

		switch (l.getType()) {
		case DOUBLE:
			return new DoubleEval(rsd);

		case FLOAT:
			if (r.getType() == Type.FLOAT)
				return new FloatEval(rsf);
			return new DoubleEval(rsd);

		case LONG_INT:
			if (r.getType() == Type.INTEGER || r.getType() == Type.LONG_INT)
				return new LongIntEval(rsl);
			return new DoubleEval(rsd);

		case INTEGER:
			if (r.getType() == Type.INTEGER)
				return new IntegerEval(rsi);
			else if (r.getType() == Type.LONG_INT)
				return new LongIntEval(rsl);
			return new DoubleEval(rsd);

		default:
			throw new RuntimeException();
		}
	}
}
