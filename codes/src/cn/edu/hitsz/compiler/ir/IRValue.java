package cn.edu.hitsz.compiler.ir;

/**
 * 代表 IR 中的 "值", 可能是 IR 变量, 也可能是 IR 立即数. 总之就是可以作为 Instruction 的参数的东西.
 */
public interface IRValue {
    default boolean isIRVariable() {
        return this instanceof IRVariable;
    }

    default boolean isImmediate() {
        return this instanceof IRImmediate;
    }
}
