package cn.edu.hitsz.compiler.parser.table;

/**
 * 表示文法符号中的非终结符, 你不应该修改此文件
 */
public class NonTerminal extends Term {
    public NonTerminal(String id) {
        super(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NonTerminal && super.equals(obj);
    }
}
