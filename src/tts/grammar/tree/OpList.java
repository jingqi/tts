package tts.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.ScriptVM;

/**
 * 操作列表
 */
public class OpList implements IOp {

	List<IOp> list = new ArrayList<IOp>();

	public void add(IOp op) {
		list.add(op);
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval ret = VoidEval.instance;
		for (int i = 0, size = list.size(); i < size; ++i) {
			ret = list.get(i).eval(vm);
		}
		return ret;
	}

}
