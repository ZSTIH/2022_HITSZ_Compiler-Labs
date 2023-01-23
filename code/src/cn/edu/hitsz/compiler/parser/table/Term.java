package cn.edu.hitsz.compiler.parser.table;

/**
 * 文法符号, 你不应该修改此文件
 * <br>
 * 该类为所有文法符号 (终止符与非终止符) 的基类
 */
public abstract class Term {
    /**
     * 获得该文法符号的名字 (就是出现在文法文件里的描述符)
     *
     * @return 名字
     */
    public String getTermName() {
        return termName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Term term
            && term.termName.equals(termName);
    }

    @Override
    public int hashCode() {
        return termName.hashCode();
    }

    @Override
    public String toString() {
        return termName;
    }

    protected Term(String termName) {
        this.termName = termName;
    }

    private final String termName;
}
