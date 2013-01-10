package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;

public final class TextTemplateOp implements IOp {

	SourceLocation sl;
	ArrayList<Object> template = new ArrayList<Object>();

	public TextTemplateOp(ArrayList<Object> t, SourceLocation sl) {
		this.sl = sl;
		this.template = t;
	}

	public TextTemplateOp(ArrayList<Object> t, String file, int line) {
		this.sl = new SourceLocation(file, line);
		this.template = t;
	}

	public static String resolveValue(IValueEval ve) {
		switch (ve.getType()) {
		case BOOLEAN:
			return Boolean.toString(((BooleanEval) ve).getValue());

		case INTEGER:
			return Long.toString(((IntegerEval) ve).getValue());

		case DOUBLE:
			return Double.toString(((DoubleEval) ve).getValue());

		case STRING:
			return ((StringEval) ve).getValue();

		case VOID:
			return "";

		case NULL:
		case ARRAY:
		case MAP:
			return ve.toString();

		default:
			throw new RuntimeException();
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, size = template.size(); i < size; ++i) {
			Object o = template.get(i);
			if (o instanceof String) {
				sb.append(o);
			} else if (o instanceof IOp) {
				IValueEval ve = ((IOp) o).eval(vm);
				sb.append(resolveValue(ve));
			} else {
				throw new RuntimeException();
			}
		}

		vm.writeText(sb.toString());
		return VoidEval.instance;
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, size = template.size(); i < size; ++i) {
			Object o = template.get(i);
			if (o instanceof String) {
				sb.append(o.toString());
			} else if (o instanceof IOp) {
				sb.append("${").append(o.toString()).append("}");
			}
		}
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
