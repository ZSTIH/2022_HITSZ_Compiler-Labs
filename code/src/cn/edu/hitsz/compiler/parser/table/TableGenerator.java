package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 根据语法文件构造 LR 分析表.
 * <br>
 * 此文件为非必需的框架文件, 用于提升整个编译器处理流程的统一性以及为学生提供 SLR(1) 分析表生成程序的参考. 正常情况下你不需要了解该文件.
 */
public class TableGenerator {
    public TableGenerator() {
        this.productions = GrammarInfo.getProductionsInOrder();
        this.terminals = new HashSet<>(TokenKind.allAllowedTokenKinds().values());
        this.nonTerminals = new HashSet<>(GrammarInfo.getNonTerminals().values());

        if (productions.get(0).body().size() != 1) {
            throw new RuntimeException("The first production in grammar file must like S -> S'");
        }
    }

    /**
     * 主体方法
     */
    public void run() {
        calcFirst();
        calcFollow();
        constructDFA();
        dumpItems();
        genTable();
    }

    /**
     * 将该分析表生成的 LR(0) 规范集族打印到某个文件之中, 用于调试
     */
    public void dumpItems() {
        final var lines = new ArrayList<String>();
        for (final var status : allStatusInIndexOrder) {
            lines.add("%d: ".formatted(status.index()));
            for (final var item : including.get(status)) {
                lines.add("    " + item);
            }
        }

        FileUtils.writeLines("data/out/items.txt", lines);
    }

    /**
     * @return 构造出的 LR 表
     */
    public LRTable getTable() {
        return new LRTable(allStatusInIndexOrder, new ArrayList<>(terminals), new ArrayList<>(nonTerminals));
    }

    private final List<Production> productions;
    private final Set<TokenKind> terminals;
    private final Set<NonTerminal> nonTerminals;

    private final Map<Term, Set<TokenKind>> first = new HashMap<>();
    private final Map<Term, Set<TokenKind>> follow = new HashMap<>();

    private final Set<Term> visited = new HashSet<>();

    /**
     * 计算所有符号的 first 集合 <br>
     * 终结符的 first 就是它自身, 非终结符的 first 则递归计算
     */
    private void calcFirst() {
        for (final var terminal : terminals) {
            first.put(terminal, Set.of(terminal));
        }

        for (final var nonTerminal : nonTerminals) {
            visited.clear();
            calcFirst(nonTerminal);
        }

    }

    /**
     * 递归计算特定非终结符的 first
     *
     * @param nonTerminal 非终结符
     * @return 其 first 集合
     */
    private Set<TokenKind> calcFirst(Term nonTerminal) {
        // 如果已经被计算, 则直接返回缓存的结果
        if (first.containsKey(nonTerminal)) {
            return first.get(nonTerminal);
        }

        final var result = new LinkedHashSet<TokenKind>();
        for (final var production : productions) {
            if (nonTerminal == production.head()) {
                // 在文法中寻找以该非终结符为头的产生式
                final var firstSymbol = production.body().get(0);
                // 记录已经在栈中的递归过的非终结符, 防止直接或间接的左递归导致程序死循环
                if (!visited.contains(firstSymbol)) {
                    // 随后递归查找该产生式体的第一个文法符号的 first 集合
                    // 它的 first 集合也是该非终结符的 first 集合
                    visited.add(firstSymbol);
                    result.addAll(calcFirst(firstSymbol));
                }
            }
        }

        first.put(nonTerminal, result);
        return result;
    }


    /**
     * 计算所有非终结符的 follow 集合
     */
    private void calcFollow() {
        for (final var nonTerminal : nonTerminals) {
            visited.clear();
            calcFollow(nonTerminal);
        }

        // 对于找完所有产生式了都还没没有 follow 的非终结符
        // 它必然是一个没有被使用的非终结符, 这意味着它的 follow 就是 EOF
        nonTerminals.stream()
            .map(follow::get).filter(Set::isEmpty)
            .forEach(set -> set.add(TokenKind.eof()));
    }

    /**
     * 递归计算给定非终结符的 follow 集合
     *
     * @param nonTerminal 给定的非终止符
     * @return 其 follow 集合
     */
    private Set<TokenKind> calcFollow(NonTerminal nonTerminal) {
        // 与 first 类似, 先查找缓存
        if (follow.containsKey(nonTerminal)) {
            return follow.get(nonTerminal);
        }

        final var result = new LinkedHashSet<TokenKind>();
        for (final var production : productions) {
            final var body = production.body();

            // 对该非终结符在每条产生式体中的可能出现, 我们都要将紧跟在该出现后面的项的 first 加入其 follow 集合
            // 我们直接枚举每条产生式体的每个项
            for (int i = 0; i < body.size() - 1; i++) {
                // 如果发现目前的 curr 的话, 后面跟着的符号的 first 就是它的 follow
                final var symbol = body.get(i);
                final var next = body.get(i + 1);

                if (symbol.equals(nonTerminal)) {
                    result.addAll(first.get(next));
                }
            }

            // 如果该非终结符出现在产生式的末尾, 那么该产生式的头的 follow 集合也要加入其 follow 集合中
            final var lastInBody = body.get(body.size() - 1);
            if (lastInBody.equals(nonTerminal)) {
                final var head = production.head();
                if (visited.contains(head)) {
                    // 如果该产生式头已经在栈中了, 那么意味着这个非终结符有可能一直展开自己
                    // 这时候 EOF 也可能是它的 Follow
                    result.add(TokenKind.eof());
                } else {
                    // 否则直接递归查找
                    visited.add(head);
                    result.addAll(calcFollow(head));
                }
            }
        }

        follow.put(nonTerminal, result);
        return result;
    }

    /**
     * 表示一个项目
     * <br>
     * 对于 A -> B . C, 其 production 为 A -> B C, dot 为 1 (其下一个项 C 的索引) <br>
     * 对于 A -> B C ., 其 production 为 A -> B C, dot 为 2 (其产生式体的项数量) <br>
     *
     * @param production 产生式
     * @param dot        目前解析到的位置
     */
    private record Item(Production production, int dot) {
        /**
         * @return 点的位置是否在产生式的末尾
         */
        public boolean isDotAtEnd() {
            return production.body().size() == dot;
        }

        /**
         * @return 获得点后面的文法符号; 若点的位置在末尾则返回空
         */
        public Optional<Term> getAfterDot() {
            if (isDotAtEnd()) {
                return Optional.empty();
            } else {
                return Optional.of(production.body().get(dot));
            }
        }

        /**
         * @return 获得当前项目的后继项; 若点的位置在末尾则返回空
         */
        public Optional<Item> getNextItem() {
            if (isDotAtEnd()) {
                return Optional.empty();
            } else {
                return Optional.of(new Item(production, dot + 1));
            }
        }

        @Override
        public String toString() {
            final var builder = new StringBuilder();

            builder.append(production.head());
            builder.append(" -> ");

            final var body = production.body();
            for (int i = 0; i < body.size(); i++) {
                if (i == dot) {
                    builder.append(" .");
                }
                builder.append(" ").append(body.get(i));
            }

            if (dot == body.size()) {
                builder.append(" .");
            }

            return builder.toString();
        }
    }

    private final Map<Set<Item>, Status> belongTo = new HashMap<>();
    private final Map<Status, Set<Item>> including = new HashMap<>();
    private final List<Status> allStatusInIndexOrder = new ArrayList<>();

    /**
     * 构造出所有状态并维护状态与项集之间的对应关系
     */
    private void constructDFA() {
        final var argumentProduction = productions.get(0);
        final var initItem = new Item(argumentProduction, 0);
        final var collections = constructCanonicalLRCollection(initItem);

        for (int idx = 0; idx < collections.size(); idx++) {
            final var status = Status.create(idx);
            allStatusInIndexOrder.add(status);

            final var items = collections.get(idx);
            including.put(status, items);
            belongTo.put(items, status);
        }
    }

    /**
     * @param head 头部符号
     * @return 返回一个流, 其中的元素都是以 head 为头的产生式
     */
    private Stream<Production> getProductionsByHead(Term head) {
        return productions.stream().filter(production -> production.head().equals(head));
    }

    /**
     * 构造项集 {@code sourceItem} 的闭包, 相当于理论课中的 CLOSURE(I) 函数
     *
     * @param sourceItem 项集 I
     * @return 闭包
     */
    private Set<Item> constructClosure(Set<Item> sourceItem) {
        // 采用 BFS 的方法来避免每次都检测集合的更改
        final var result = new LinkedHashSet<>(sourceItem);
        // 已经找到, 但是还未完全探索其能展开的项的项
        // 类似于 BFS 的队列或者是 mark-and-sweep GC 算法里的灰色列表
        final var unexpanded = new ArrayDeque<>(sourceItem);

        while (!unexpanded.isEmpty()) {
            final var top = unexpanded.pollFirst();
            // 获得当前项中点后面的符号
            top.getAfterDot().ifPresent(afterDot ->
                // 随后查找以该符号作为头部的产生式
                getProductionsByHead(afterDot)
                    // 构造点在对应产生式开头的新项
                    .map(production -> new Item(production, 0))
                    // 对于不在 result 中的新项
                    .filter(item -> !result.contains(item))
                    // 加入队列并加入结果中
                    .peek(unexpanded::add)
                    .forEach(result::add));
        }

        return result;
    }

    /**
     * 相当于理论课中的 GO(I, X) 函数 (转移函数)
     *
     * @param items 当前项集 I
     * @param term  文法符号 X
     * @return I 关于 X 的后继项目集
     */
    private Set<Item> constructGoto(Set<Item> items, Term term) {
        final var kernelForGoto = items.stream()
            // 先筛选出 I 中所有点后面是 term 的项
            .filter(item -> item.getAfterDot().map(term::equals).orElse(false))
            // 然后分别求出每一个项的后继项
            // 这个项集就是后继项目集的核
            .map(Item::getNextItem).flatMap(Optional::stream)
            .collect(Collectors.toSet());

        // 随后从后继项目集的核中构造闭包即可得到后继项目集
        return constructClosure(kernelForGoto);
    }

    /**
     * 构造 LR(0) 规范项目集族
     *
     * @param initItem 起始项目 S -> . S'
     * @return 规范项目集族
     */
    private List<Set<Item>> constructCanonicalLRCollection(Item initItem) {
        // 先收集所有的文法符号备用
        final var terms = new LinkedHashSet<Term>();
        terms.addAll(nonTerminals);
        terms.addAll(terminals);

        // 构造初始项目集族
        final var initClosure = constructClosure(Set.of(initItem));

        // 同样以 BFS 形式搜索
        final var result = new LinkedHashSet<>(Set.of(initClosure));
        final var unexpanded = new ArrayDeque<>(Set.of(initClosure));

        while (!unexpanded.isEmpty()) {
            final var items = unexpanded.pollFirst();

            // 对于每个未被探索的集族
            for (final var term : terms) {
                // 尝试对每一个文法符号都求一个后继项目集
                final var to = constructGoto(items, term);
                // 如果还没被加入到 result 中
                if (!to.isEmpty() && !result.contains(to)) {
                    // 就加入 result 和待探索列表中
                    result.add(to);
                    unexpanded.addLast(to);
                }
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * 构造 LR 分析表 (填充各个 status 中的 action 与 goto)
     */
    private void genTable() {
        // 依索引顺序对每个状态的每个项目
        for (final var status : allStatusInIndexOrder) {
            for (final var item : including.get(status)) {
                final var afterDotOpt = item.getAfterDot();

                // 根据项目的点的位置和点后面跟着的文法符号进行分类
                if (afterDotOpt.isEmpty()) {
                    final var argumentProduction = productions.get(0);
                    if (item.production().equals(argumentProduction)) {
                        // S -> S' .
                        // 如果项目代表起始文法的末尾, 那么再遇到 EOF 就 accept 了
                        status.setAction(TokenKind.eof(), Action.accept());
                    } else {
                        // A -> alpha .
                        // 如果项目代表某个产生式的末尾, 那么再遇到对于任何位于 follow(A) 内的文法符号都应该规约 A
                        final var production = item.production();
                        final var head = production.head();
                        for (final var a : follow.get(head)) {
                            status.setAction(a, Action.reduce(production));
                        }
                    }

                } else {
                    final var symbol = afterDotOpt.get();
                    final var next = belongTo.get(constructGoto(including.get(status), symbol));

                    if (symbol instanceof TokenKind tokenKind) {
                        // A -> alpha . a beta
                        // 如果项目代表某个产生式的中间, 并且接着一个终结符的情况, 我们就移入该终结符
                        status.setAction(tokenKind, Action.shift(next));
                    } else if (symbol instanceof NonTerminal nonTerminal) {
                        // A -> alpha . B beta
                        // 如果项目代表某个产生式的中间, 并且接着一个非终结符的情况,
                        // 我们就转移到 B 解析之后的状态中去 (即 GO(I, B) 对应的状态)
                        status.setGoto(nonTerminal, next);
                    } else {
                        throw new RuntimeException("Unknown type of Term");
                    }
                }
            }
        }
    }

    // 我们在 Status.setAction/setGoto 中检查规约-规约冲突与移入-规约冲突
    // 如果有冲突, 它们会抛出 RuntimeException
}
