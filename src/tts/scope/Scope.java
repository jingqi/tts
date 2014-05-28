package tts.scope;

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
	private final Map<String, Variable> variables = new HashMap<String, Variable>();

	public Scope(Scope upper) {
		this.upper = upper;
	}

	/**
	 * 获取变量的值
	 */
	public Variable getVariable(String name) {
		return variables.get(name);
	}

	/**
	 * 获取变量的值
	 */
	public Variable getVariableUpward(String name) {
		Scope current = this;
		while (current != null) {
			Variable ret = current.getVariable(name);
			if (ret != null)
				return ret;
			current = current.upper;
		}
		return null;
	}

	/**
	 * 添加定义的变量
	 */
	public void addVariable(String name, Variable var, SourceLocation sl) {
		if (variables.containsKey(name))
			throw new ScriptRuntimeException("Variable " + name + " redefined", sl);
		variables.put(name, var);
	}
}
