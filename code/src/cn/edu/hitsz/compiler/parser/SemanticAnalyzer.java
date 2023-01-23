package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
        // throw new NotImplementedException();
        // 空实现 (遇到 Accept 时语义分析可以结束, 无需进行其它动作)
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        // throw new NotImplementedException();
        switch (production.index()) {
            case 4 -> {
                // S -> D id
                // 获得 id 对应的 token
                var token = semanticStack.pop().getToken();
                // 获得 D 对应的 type
                var type = semanticStack.pop().getType();
                // 获得 id 对应的 Text
                var idText = token.getText();
                symbolTable.get(idText).setType(type);
                // 压入空记录占位
                semanticStack.push(new SemanticStackEntry());
            }
            case 5 -> {
                // D -> int
                var token = semanticStack.pop().getToken();
                var tokenKindID = token.getKindId();
                if ("int".equals(tokenKindID)) {
                    // 规约的产生式确实为 D -> int , 将 D 符号的 type 属性(综合属性)压入栈
                    semanticStack.push(new SemanticStackEntry(SourceCodeType.Int));
                } else {
                    throw new RuntimeException("Semantic analysis error!");
                }
            }
            default -> {
                // 其它产生式直接弹栈即可
                int num = production.body().size();
                while (num > 0) {
                    semanticStack.pop();
                    num--;
                }
                // 压入空记录占位
                semanticStack.push(new SemanticStackEntry());
            }
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        // throw new NotImplementedException();
        semanticStack.push(new SemanticStackEntry(currentToken));
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        // throw new NotImplementedException();
        this.symbolTable = table;
    }

    /**
     * 加载到的符号表
     */
    private SymbolTable symbolTable;

    /**
     * 语义分析栈
     */
    private final Stack<SemanticStackEntry> semanticStack = new Stack<>();
}

class SemanticStackEntry {
    private final Token token;
    private final SourceCodeType type;

    public SemanticStackEntry(Token token) {
        this.token = token;
        this.type = null;
    }

    public SemanticStackEntry(SourceCodeType type) {
        this.token = null;
        this.type = type;
    }

    public SemanticStackEntry() {
        this.token = null;
        this.type = null;
    }

    public Token getToken() {
        if (this.token == null) {
            throw new RuntimeException("Not a token!");
        }
        return this.token;
    }

    public SourceCodeType getType() {
        if (this.type == null) {
            throw new RuntimeException("Not a type!");
        }
        return this.type;
    }

}