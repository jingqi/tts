package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.*;

public class TextTemplateOp implements IOp {

	String template;

	public TextTemplateOp(String t) {
		template = t;
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
					throw new ScriptRuntimeException("");
				sb.append(v.getValue().toString());
				state = 0;
				break;
			}
		}
		if (state != 0)
			throw new ScriptRuntimeException("");

		System.out.println(sb.toString());
		return VoidEval.instance;
	}
}
