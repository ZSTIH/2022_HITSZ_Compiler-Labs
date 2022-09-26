package cn.edu.hitsz.compiler.ir;

/**
 * IR 中的 "变量"
 * <br>
 * 我们允许 IR 中的变量保存一些有关源语言的信息 -- 比如它在源语言里对应的是具体的源语言变量还是源语言中的临时变量. 这些信息用一个字符串
 * name 来表示. 临时变量的 name 都是 "$[0-9]+", 而非临时变量的 name 都是 "[a-zA-Z_][a-zA-Z0-9_]*"
 * <br>
 * IR 变量的等价性由 name 唯一确定.
 */
public class IRVariable implements IRValue {
    /**
     * @param name 源语言中变量的名字
     * @return 一个对应于源语言中具体变量的 IRVariable
     */
    public static IRVariable named(String name) {
        return new IRVariable(name);
    }

    /**
     * @return 一个对应于源语言中的临时变量的新 IRVariable
     */
    public static IRVariable temp() {
        return new IRVariable("$" + count++);
    }

    public String getName() {
        return name;
    }

    public boolean isTemp() {
        return name.startsWith("$");
    }

    public boolean isNamed() {
        return !isTemp();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRVariable reg && name.equals(reg.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private IRVariable(String name) {
        this.name = name;
    }

    private final String name;
    private static int count = 0;
}
