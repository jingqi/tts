package tts.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码段有效范围
 */
class Scope {

	// 局部变量
	Map<String, Variable> variables = new HashMap<String, Variable>();

	public Variable getVariable(String name) {
		return variables.get(name);
	}
}
