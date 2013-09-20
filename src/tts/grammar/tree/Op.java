package tts.grammar.tree;


import tts.eval.IValueEval;
import tts.util.SourceLocation;
import tts.vm.Frame;

/**
 * 语法树节点
 *
 * @author jingqi
 */
public abstract class Op {

	private final SourceLocation sourceLocation;

	public Op(SourceLocation sl) {
		sourceLocation = sl;
	}

	public abstract IValueEval eval(Frame f);

	public Op optimize() {
		// 默认实现是不优化
		return this;
	}

	public final SourceLocation getSourceLocation() {
		return sourceLocation;
	}
}
