package cn.edu.hitsz.compiler.symtab;

/**
 * 符号表条目
 */
public class SymbolTableEntry {
    /**
     * @param text 符号的文本表示. 对于标识符符号, 该参数应该为标识符文本.
     */
    public SymbolTableEntry(String text) {
        this.text = text;
        this.type = null;
    }

    /**
     * @return 符号的文本表示
     */
    public String getText() {
        return text;
    }

    /**
     * @return 该标识符符号可以绑定到的源语言对象的类型
     */
    public SourceCodeType getType() {
        return type;
    }

    /**
     * 由于这个类型严格来说只能在语法分析后才能获得, 所以为了在词法分析时就构造出符号表,
     * 我们只能暴露出该接口用以修改该成员. 该成员应该且只应该被修改一次.
     *
     * @param type 该标识符符号可以绑定到的源语言对象的类型
     */
    public void setType(SourceCodeType type) {
        if (this.type != null) {
            throw new RuntimeException("Can NOT set type for an entry twice");
        }

        this.type = type;
    }

    private final String text;
    private SourceCodeType type;
}
