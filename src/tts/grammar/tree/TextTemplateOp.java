package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.ScriptVM;

public class TextTemplateOp implements IOp {

	String template;

	public TextTemplateOp(String t) {
		template = t;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		System.out.println(template);
		return VoidEval.instance;
	}
}
