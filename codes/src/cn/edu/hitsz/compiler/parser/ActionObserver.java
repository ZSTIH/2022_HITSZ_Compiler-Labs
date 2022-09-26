package cn.edu.hitsz.compiler.parser;


import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

/**
 * LR 驱动程序动作观察者, 你不应该修改此文件
 * <br>
 * 此接口抽象出动作观察者的概念, 将 LR 驱动程序从具体的语义动作中解耦, 使其得以处理任意的, 抽象的文法, 而把 "遇到某某产生式就执行什么东西"
 * 的具体操作从驱动程序中分离.
 * <br>
 * 在传统解析器生成器, 如 yacc 中, 与每条文法的规约对应的动作一般是直接写在 .y 文件中; 而由于我们的解析器不能直接把代码动作写在 grammar.txt
 * 中, 我们只能在自己的代码实现中根据传入的 production 不同再进行动作的分派, 以此来实现 SDT. 关于此接口的一个使用的例子可以参考
 * ProductionCollector 类
 * <br>
 * 注意观察者并不能访问到 LR 驱动程序维护着的状态栈, 观察者之间维护的栈信息也不应该互相访问. 每一个实现该接口的观察者需要自己定义自己需要的
 * 状态信息并维护自己的状态栈.
 *
 * @see ProductionCollector
 * @see SyntaxAnalyzer
 */
public interface ActionObserver {
    /**
     * 当驱动程序执行 Shift 动作时会调用此函数. Shift 会转移到的状态可以直接从参数中获取:
     * {@code currentStatus.getAction(currentToken).getStatus() }
     *
     * @param currentStatus 当前的状态
     * @param currentToken  当前的词法单元
     */
    void whenShift(Status currentStatus, Token currentToken);

    /**
     * 当驱动程序执行 Reduce 动作时会调用此函数. Goto 到的新状态可以直接从参数中获取:
     * {@code currentStatus.getGoto(production.head()) }
     *
     * @param currentStatus 当前状态
     * @param production    待规约的产生式
     */
    void whenReduce(Status currentStatus, Production production);

    /**
     * 当驱动程序执行 Accept 动作时会调用此函数.
     *
     * @param currentStatus 当前状态
     */
    void whenAccept(Status currentStatus);

    /**
     * 当驱动程序接受符号表时会调用此函数, 实现此接口的类可以自行决定是否存储这个符号表
     *
     * @param table 符号表
     */
    void setSymbolTable(SymbolTable table);
}
