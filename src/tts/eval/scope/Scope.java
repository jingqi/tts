package tts.eval.scope;

import java.util.HashMap;
import java.util.Map;

import tts.trace.SourceLocation;
import tts.vm.rtexcept.ScriptRuntimeException;

/**
 * 代码段有效范围
 */
public final class Scope {

	// upper scope
	private final Scope upper;

	// 局部变量
	private final Map<String, EvalSlot> variables = new HashMap<String, EvalSlot>();

	public Scope(Scope upper) {
		this.upper = upper;
	}

	/**
	 * 获取变量的值
	 */
	public EvalSlot getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * 获取变量的值
	 */
	public EvalSlot getVariableUpward(String name) {
		Scope current = this;
		while (current != null) {
			EvalSlot ret = current.getVariable(name);
			if (ret != null)
				return ret;
			current = current.upper;
		}
		return null;
	}

	/**
	 * 添加定义的变量
	 */
	public void addVariable(String name, EvalSlot var, SourceLocation sl) {
		if (variables.containsKey(name))
			throw new ScriptRuntimeException("Variable " + name + " redefined", sl);
		variables.put(name, var);
	}
}
