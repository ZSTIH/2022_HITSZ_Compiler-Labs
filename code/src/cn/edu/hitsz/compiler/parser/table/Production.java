package cn.edu.hitsz.compiler.parser.table;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示一条产生式, 你不应该改动此文件
 * <br>
 * 产生式的等价性由其 index 唯一确定. 即, 两条产生式 equals 当且仅当它们 index 相等.
 *
 * @param index 该产生式的索引, 为其在 grammar.txt 文件内的行号, 从 1 开始
 * @param head  该产生式的头
 * @param body  该产生式的体
 */
public record Production(int index, NonTerminal head, List<Term> body) {
    @Override
    public String toString() {
        final var bodyStr = body.stream().map(Objects::toString).collect(Collectors.joining(" "));
        return "%s -> %s".formatted(head, bodyStr);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Production production
            && production.index == index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}
