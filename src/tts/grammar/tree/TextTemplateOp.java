package tts.grammar.tree;

import tts.eval.*;
import tts.vm.*;

public class TextTemplateOp implements IOp {

	String template;

	public TextTemplateOp(String t) {
		template = t;
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

		case ARRAY:
			return ve.toString();

		default:
			throw new RuntimeException();
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		StringBuilder sb = new StringBuilder(template.length());
		StringBuilder name = new StringBuilder();
		int state = 0;
		for (int i = 0, len = template.length(); i < len; ++i) {
			char c = template.charAt(i);
			switch (state) {
			case 0:
				if (c != '$') {
					sb.append(c);
					break;
				}
				state = 1;
				break;

			case 1:
				if (c != '{') {
					sb.append('$');
					sb.append(c);
					state = 0;
					break;
				}
				state = 2;
				break;

			case 2:
				if (c != '}') {
					name.append(c);
					break;
				}
				if (sb.length() == 0) {
					sb.append("${}");
					state = 0;
					break;
				}

				Variable v = vm.getVariable(name.toString());
				if (v == null || v.getValue() == null)
					throw new ScriptRuntimeException("variable " + name
							+ " not found");
				sb.append(resolveValue(v.getValue()));
				state = 0;
				break;
			}
		}
		if (state != 0)
			throw new ScriptRuntimeException("");

		vm.writeText(sb.toString());
		return VoidEval.instance;
	}

	@Override
	public IOp optimize() {
		return this;
	}

	@Override
	public String toString() {
		return template;
	}
}
