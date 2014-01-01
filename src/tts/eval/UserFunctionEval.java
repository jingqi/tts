package tts.eval;

import java.util.ArrayList;
import java.util.List;

import tts.eval.scope.*;
import tts.grammar.tree.Op;
import tts.grammar.tree.binaryop.AssignOp;
import tts.util.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.*;

/**
 * 用户函数
 */
public final class UserFunctionEval extends FunctionEval {

	/**
	 * 形参信息
	 */
	public static class ParamInfo {
		String name;
		VarType type;

		public ParamInfo(String n, VarType vt) {
			this.name = n;
			this.type = vt;
		}
	}

	String funcName;
	Scope upperScope; // 函数定义的范围
	Op ops;
	ArrayList<ParamInfo> params;

	public UserFunctionEval(String func, Scope upperScope, Op ops, ArrayList<ParamInfo> params) {
		this.funcName = func;
		this.upperScope = upperScope;
		this.ops = ops;
		this.params = params;
	}

	@Override
	public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
		if (args.size() != params.size())
			throw new ScriptRuntimeException(
					"count of argument not match in calling function", sl);

		Scope s = new Scope(upperScope);
		f.pushScope(s);
		f.pushFuncCalling(sl, funcName);
		IValueEval ret = VoidEval.instance;
		try {
			// 设置参数
			for (int i = 0, size = params.size(); i < size; ++i) {
				ParamInfo p = params.get(i);
				EvalSlot v = new EvalSlot(p.type, null);
				AssignOp.assign(v, args.get(i), sl);
				f.addVariable(p.name, v, sl);
			}

			// 调用函数
			if (ops != null)
				ops.eval(f);
		} catch (BreakLoopException e) {
			f.popFuncCalling();
			f.popScope();
			throw new ScriptRuntimeException("Break without loop", e.sl);
		} catch (ContinueLoopException e) {
			f.popFuncCalling();
			f.popScope();
			throw new ScriptRuntimeException("Continue without loop", e.sl);
		} catch (ReturnFuncException e) {
			ret = e.value;
		} catch (ScriptRuntimeException e) {
			f.popFuncCalling();
			f.popScope();
			throw e;
		}
		f.popFuncCalling();
		f.popScope();
		return ret;
	}

	public void optimize() {
		if (ops != null)
			ops = ops.optimize();
	}

	@Override
	public String getFunctionName() {
		return funcName;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}
