package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取 "编译工作台" 生成的语法分析表并将其转换为 LRTable 结构, 你不应该修改此文件
 */
public class TableLoader {
    /**
     * 读取分析表
     *
     * @param path CSV 格式的分析表路径
     * @return LRTable
     */
    public LRTable load(String path) {
        final var csv = FileUtils.readCSV(path);
        // 表头是 状态, ACTION, ..., GOTO, ... 那一行
        final var tableHeader = csv.get(0);

        // 根据该行确定各个部分的列号
        final var statusColumnIndex = 0;
        final var actionColumnBegin = 1;
        final var actionColumnEnd = tableHeader.indexOf("GOTO");
        final var gotoColumnBegin = actionColumnEnd;
        final var gotoColumnEnd = tableHeader.size();

        // 符号行是存放终结符与非终结符的部分
        final var symbolHeader = csv.get(1);
        final var terminals = symbolHeader
            .subList(actionColumnBegin, actionColumnEnd).stream()
            .map(TokenKind::fromString).toList();
        final var nonTerminals = symbolHeader
            .subList(gotoColumnBegin, gotoColumnEnd).stream()
            .map(NonTerminal::new).toList();

        // 再往下便是表的主体部分
        final var statusRows = csv.subList(2, csv.size());

        // 首先读取所有状态编号, 构造出对应的 Status 对象
        for (final var row : statusRows) {
            final var statusIndexString = row.get(statusColumnIndex);
            final var statusIndex = Integer.parseInt(statusIndexString);
            final var status = Status.create(statusIndex);

            statusInIndexOrder.add(status);
            statuses.put(statusIndex, status);
        }

        // 然后再读取 ACTION 和 GOTO 表, 为构造出的 Status 对象填充 action 和 goto 信息
        for (final var row : statusRows) {
            final var status = statuses.get(Integer.valueOf(row.get(statusColumnIndex)));

            // 处理 ACTION 表
            for (int idx = actionColumnBegin; idx < actionColumnEnd; idx++) {
                final var relativeIndex = idx - actionColumnBegin;
                final var terminal = terminals.get(relativeIndex);

                final var action = parseAction(row.get(idx));
                status.setAction(terminal, action);
            }

            // 处理 GOTO 表
            for (int idx = gotoColumnBegin; idx < gotoColumnEnd; idx++) {
                final var relativeIndex = idx - gotoColumnBegin;
                final var nonTerminal = nonTerminals.get(relativeIndex);

                final var goto_ = parseGoto(row.get(idx));
                status.setGoto(nonTerminal, goto_);
            }
        }

        // 返回构造出的 LR 表
        return new LRTable(statusInIndexOrder, terminals, nonTerminals);
    }

    private final List<Status> statusInIndexOrder = new ArrayList<>();
    private final Map<Integer, Status> statuses = new HashMap<>();

    /**
     * 解析 ACTION 表的字符串
     *
     * @param text 对应单元格的文本
     * @return 解析出的动作
     */
    private Action parseAction(String text) {
        // 空字符串代表错误动作
        if (text.isEmpty()) {
            return Action.error();
        }

        // 动作总是分为 "<命令> <载荷>" 两部分, 以空格分割
        // limit 是 < 的, limit = 2 意味着切割次数只能 < 2, 也就是切割一次.
        // 有可能 words 的长度只有 1, 这时候 text 就是 accept
        final var words = text.split(" ", 2);
        final var command = words[0];

        return switch (command) {
            case "shift" -> {
                final var statusIndex = Integer.valueOf(words[1]);
                final var status = statuses.get(statusIndex);
                yield Action.shift(status);
            }

            case "reduce" -> {
                final var production = GrammarInfo.getProductionByText(words[1]);
                yield Action.reduce(production);
            }

            case "accept" -> Action.accept();

            default -> throw new RuntimeException("Illegal action in table: " + text);
        };
    }

    /**
     * 解析 GOTO 表中的字符串
     *
     * @param text 对应单元格的文本
     * @return 解析出的状态
     */
    private Status parseGoto(String text) {
        if (text.isEmpty()) {
            return Status.error();
        } else {
            return statuses.get(Integer.valueOf(text));
        }
    }
}
