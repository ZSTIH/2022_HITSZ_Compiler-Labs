package cn.edu.hitsz.compiler.ir;

/**
 * IR 的种类
 */
public enum InstructionKind {
    ADD, SUB, MUL, MOV, RET;

    /**
     * @return IR 是否是二元的 (有返回值, 有两个参数)
     */
    public boolean isBinary() {
        return this != MOV && this != RET;
    }

    /**
     * @return IR 是否是一元的 (有返回值, 有一个参数)
     */
    public boolean isUnary() {
        return this == MOV;
    }

    /**
     * @return IR 是否为 RET 指令
     */
    public boolean isReturn() {
        return this == RET;
    }
}
