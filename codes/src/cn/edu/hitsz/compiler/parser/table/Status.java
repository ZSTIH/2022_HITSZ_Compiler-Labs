package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示 LR 分析表中的一个状态, 你不应该修改此文件
 * <br>
 * 状态的等价性由其编号唯一决定. 即两状态 equals 当且仅当它们的 index 相同
 *
 * @param index  状态在 LR 表中的索引/编号
 * @param action 在该状态下遇到终结符后应该转移到哪个状态
 * @param goto_  在该状态下规约到非终结符后应该转移到哪个状态
 */
public record Status(int index, Map<TokenKind, Action> action, Map<NonTerminal, Status> goto_) {
    /**
     * 构造一个状态
     *
     * @param index 状态的索引/编号
     * @return 构造出的状态
     */
    public static Status create(int index) {
        if (index < 0) {
            throw new RuntimeException("Index of status can NOT smaller than zero");
        }

        return new Status(index);
    }

    /**
     * @return 获得代表错误的状态
     */
    public static Status error() {
        return errorInstance;
    }

    public boolean isError() {
        return this == errorInstance;
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Status status
            && status.index == index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    /**
     * 当遇到该终结符 (Token 类型) 时, 应该转移到哪个状态
     *
     * @param terminal 终结符 (Token 类型)
     * @return 应该转移到的状态
     */
    public Action getAction(TokenKind terminal) {
        return action.getOrDefault(terminal, Action.error());
    }

    /**
     * 当遇到该词法单元时, 应该转移到哪个状态
     *
     * @param token 词法单元
     * @return 应该转移到的状态
     */
    public Action getAction(Token token) {
        return getAction(token.getKind());
    }

    /**
     * 当规约到该非终结符时, 应该转移到哪个状态
     *
     * @param nonTerminal 非终结符
     * @return 应该转移到的状态
     */
    public Status getGoto(NonTerminal nonTerminal) {
        return goto_.getOrDefault(nonTerminal, Status.error());
    }

    //==================== 以下为实现相关代码 ==============================//

    void setAction(TokenKind terminal, Action action) {
        // 有可能 set 相同的 action, 这时候不能报错
        if (inAndNotEqual(this.action, terminal, action)) {
            throw new RuntimeException("Action conflict at %s on %d".formatted(terminal, index));
        }

        this.action.put(terminal, action);
    }

    void setGoto(NonTerminal nonTerminal, Status goto_) {
        // 有可能 set 相同的 goto, 这时候不能报错
        if (inAndNotEqual(this.goto_, nonTerminal, goto_)) {
            throw new RuntimeException("Goto conflict at %s on %d".formatted(nonTerminal, index));
        }

        this.goto_.put(nonTerminal, goto_);
    }

    private static <K, V> boolean inAndNotEqual(Map<K, V> map, K key, V newValue) {
        return map.containsKey(key) && !newValue.equals(map.get(key));
    }

    private Status(int index) {
        this(index, new HashMap<>(), new HashMap<>());
    }

    private static final Status errorInstance = new Status(-1);
}
