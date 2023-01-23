package cn.edu.hitsz.compiler.ir;

/**
 * IR 中的立即数
 */
public class IRImmediate implements IRValue {
    public static IRImmediate of(int value) {
        return new IRImmediate(value);
    }

    public int getValue() {
        return value;
    }

    private final int value;

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    private IRImmediate(int value) {
        this.value = value;
    }
}
