package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.utils.FilePathConfig;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.*;

/**
 * 读取语法文件 (grammar.txt), 获得产生式的原始字符串和非终结符
 * <br>
 * 你不应该修改此文件
 */
public class GrammarInfo {
    private final Map<String, NonTerminal> nonTerminals = new HashMap<>();
    private final Map<String, Production> productions = new HashMap<>();
    private final List<Production> productionsInOrder = new ArrayList<>();

    private NonTerminal getOrCreateNonTerminal(String name) {
        nonTerminals.computeIfAbsent(name, NonTerminal::new);
        return nonTerminals.get(name);
    }

    private GrammarInfo() {
        final var lines = FileUtils.readLines(FilePathConfig.GRAMMAR_PATH);
        for (int idx = 0; idx < lines.size(); idx++) {
            final var line = lines.get(idx);
            // 形如 `A -> B ( id intConst ) C;` 的产生式
            // 先删除分号, 按 -> 切, 再按空格切 body
            final var withoutComma = line.replace(";", "");
            final var words = withoutComma.split(" -> ");
            final var headString = words[0];
            final var bodyStrings = words[1].split(" ");

            final var head = getOrCreateNonTerminal(headString);

            final var body = new ArrayList<Term>();
            for (final var termName : bodyStrings) {
                if (TokenKind.isAllowed(termName)) {
                    body.add(TokenKind.fromString(termName));
                } else {
                    body.add(getOrCreateNonTerminal(termName));
                }
            }

            // idx + 1 是为了让 production 的标号与行号相同, 方便查看
            final var production = new Production(idx + 1, head, body);
            productionsInOrder.add(production);
            productions.put(withoutComma, production);
        }
    }

    // 为了防止有人看不懂, 就不用枚举定义单例了
    // 顺手写个懒加载
    private static GrammarInfo instance = null;

    private static GrammarInfo getInstance() {
        if (instance == null) {
            instance = new GrammarInfo();
        }

        return instance;
    }

    public static Map<String, NonTerminal> getNonTerminals() {
        return Collections.unmodifiableMap(getInstance().nonTerminals);
    }

    public static Map<String, Production> getProductions() {
        return Collections.unmodifiableMap(getInstance().productions);
    }

    public static NonTerminal getNonTerminal(String name) {
        final var nonTerminals = getNonTerminals();
        if (!nonTerminals.containsKey(name)) {
            throw new RuntimeException("Unknown non-terminal: " + name);
        }

        return nonTerminals.get(name);
    }

    public static Production getProductionByText(String text) {
        final var productions = getProductions();
        if (!productions.containsKey(text)) {
            throw new RuntimeException("Unknown text of production: " + text);
        }

        return productions.get(text);
    }

    public static Production getBeginProduction() {
        return getInstance().productionsInOrder.get(0);
    }

    public static List<Production> getProductionsInOrder() {
        return Collections.unmodifiableList(getInstance().productionsInOrder);
    }
}
