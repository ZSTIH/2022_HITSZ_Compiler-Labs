package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示 LR 文法分析表, 你不应该修改此文件
 * <br>
 */
public class LRTable {
    /**
     * 根据当前状态与当前词法单元获取对应动作
     *
     * @param status 当前状态
     * @param token  当前词法单元
     * @return 应采取的动作
     */
    public Action getAction(Status status, Token token) {
        final var tokenKind = token.getKind();
        return status.getAction(tokenKind);
    }

    /**
     * 根据当前状态与规约到非终结符获得应转移到的状态
     *
     * @param status      当前状态
     * @param nonTerminal 规约出的非终结符
     * @return 应转移到的状态
     */
    public Status getGoto(Status status, NonTerminal nonTerminal) {
        return status.getGoto(nonTerminal);
    }

    /**
     * @return 起始状态
     */
    public Status getInit() {
        return statusInIndexOrder.get(0);
    }

    public void dumpTable(String path) {
        final var text = new StringBuilder();
        // table head
        text.append("Status,ACTION").append(",".repeat(terminals.size()))
            // GOTO 占了第一个 nonTerminal 的位置, 所以要 -1
            .append("GOTO").append(",".repeat(nonTerminals.size() - 1))
            .append("\n");

        text.append(",")
            .append(terminals.stream().map(Term::toString).collect(Collectors.joining(",")))
            .append(",")
            .append(nonTerminals.stream().map(Term::toString).collect(Collectors.joining(",")))
            .append("\n");

        for (final var status : statusInIndexOrder) {
            text.append(status)
                .append(",")
                .append(terminals.stream().map(status::getAction).map(Action::toString).collect(Collectors.joining(",")))
                .append(",")
                .append(nonTerminals.stream().map(status::getGoto).map(this::convertToGotoString).collect(Collectors.joining(",")))
                .append("\n");
        }

        FileUtils.writeFile(path, text.toString());
    }

    private String convertToGotoString(Status status) {
        if (status.equals(Status.error())) {
            return "";
        } else {
            return status.toString();
        }
    }

    LRTable(List<Status> statusInIndexOrder, List<TokenKind> terminals, List<NonTerminal> nonTerminals) {
        this.statusInIndexOrder = statusInIndexOrder;
        this.terminals = terminals;
        this.nonTerminals = nonTerminals;
    }

    private final List<Status> statusInIndexOrder;
    private final List<TokenKind> terminals;
    private final List<NonTerminal> nonTerminals;
}
