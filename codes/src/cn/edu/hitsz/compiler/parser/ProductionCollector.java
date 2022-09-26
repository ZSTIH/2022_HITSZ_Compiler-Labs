package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 规约出的产生式的收集器, 你不应该改动此文件
 * <br>
 * 该类将自己注册为 LR 驱动程序的动作观察者, 在每次 reduce 将规约的产生式存起来, 待到语法分析结束之后便能按规约顺序输出所有规约到产生式.
 * 该类的输出结果会被作为判断实验二代码正误的根据.
 */
public class ProductionCollector implements ActionObserver {
    public ProductionCollector(Production beginProduction) {
        this.beginProduction = beginProduction;
    }

    private final Production beginProduction;
    private final List<Production> reducedProductions = new ArrayList<>();

    /**
     * 将结果输出到文件
     *
     * @param path 文件路径
     */
    public void dumpToFile(String path) {
        FileUtils.writeLines(path, reducedProductions.stream().map(Production::toString).toList());
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // 当规约时, 记录规约到的产生式
        reducedProductions.add(production);
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // do nothing
    }

    @Override
    public void whenAccept(Status currentStatus) {
        // 当接受时, 记录下对起始产生式的规约
        reducedProductions.add(beginProduction);
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // do nothing
    }
}
