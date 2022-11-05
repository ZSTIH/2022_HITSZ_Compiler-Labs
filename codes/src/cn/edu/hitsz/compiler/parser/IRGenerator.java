package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        // throw new NotImplementedException();
        irGeneratorStack.push(new IRGeneratorStackEntry(currentToken));
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        // throw new NotImplementedException();
        switch (production.index()) {
            case 6 -> {
                // S -> id = E
                var from = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.pop();
                var token = irGeneratorStack.pop().getToken();
                var text = token.getText();
                if (!symbolTable.has(text)) {
                    throw new RuntimeException("No such id in symbolTable!");
                }
                var result = IRVariable.named(text);
                instructions.add(Instruction.createMov(result, from));
                irGeneratorStack.push(new IRGeneratorStackEntry());
            }
            case 7 -> {
                // S -> return E
                var returnValue = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.pop();
                instructions.add(Instruction.createRet(returnValue));
                irGeneratorStack.push(new IRGeneratorStackEntry());
            }
            case 8 -> {
                // E -> E + A
                var rhs = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.pop();
                var lhs = irGeneratorStack.pop().getIrValue();
                var result = IRVariable.temp();
                instructions.add(Instruction.createAdd(result, lhs, rhs));
                irGeneratorStack.push(new IRGeneratorStackEntry(result));
            }
            case 9 -> {
                // E -> E - A
                var rhs = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.pop();
                var lhs = irGeneratorStack.pop().getIrValue();
                var result = IRVariable.temp();
                instructions.add(Instruction.createSub(result, lhs, rhs));
                irGeneratorStack.push(new IRGeneratorStackEntry(result));
            }
            case 10, 12 -> {
                // E -> A, A -> B
                var irValue = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.push(new IRGeneratorStackEntry(irValue));
            }
            case 11 -> {
                // A -> A * B
                var rhs = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.pop();
                var lhs = irGeneratorStack.pop().getIrValue();
                var result = IRVariable.temp();
                instructions.add(Instruction.createMul(result, lhs, rhs));
                irGeneratorStack.push(new IRGeneratorStackEntry(result));
            }
            case 13 -> {
                // B -> ( E )
                irGeneratorStack.pop();
                var irValue = irGeneratorStack.pop().getIrValue();
                irGeneratorStack.pop();
                irGeneratorStack.push(new IRGeneratorStackEntry(irValue));
            }
            case 14 -> {
                // B -> id
                var token = irGeneratorStack.pop().getToken();
                var text = token.getText();
                if (!symbolTable.has(text)) {
                    throw new RuntimeException("No such id in symbolTable!");
                }
                var irVariable = IRVariable.named(text);
                irGeneratorStack.push(new IRGeneratorStackEntry(irVariable));
            }
            case 15 -> {
                // B -> IntConst
                var token = irGeneratorStack.pop().getToken();
                var irImmediate = IRImmediate.of(Integer.parseInt(token.getText()));
                irGeneratorStack.push(new IRGeneratorStackEntry(irImmediate));
            }
            default -> {
                // 其它产生式直接弹栈即可
                int num = production.body().size();
                while (num > 0) {
                    irGeneratorStack.pop();
                    num--;
                }
                // 压入空记录占位
                irGeneratorStack.push(new IRGeneratorStackEntry());
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        // throw new NotImplementedException();
        // 空实现 (遇到 Accept 时语义分析可以结束, 无需进行其它动作)
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        // throw new NotImplementedException();
        this.symbolTable = table;
    }

    public List<Instruction> getIR() {
        // TODO
        // throw new NotImplementedException();
        return instructions;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }

    /**
     * 加载到的符号表
     */
    private SymbolTable symbolTable;

    /**
     * 生成得到的 Instruction 列表
     */
    private final List<Instruction> instructions = new ArrayList<>();

    private final Stack<IRGeneratorStackEntry> irGeneratorStack = new Stack<>();
}

class IRGeneratorStackEntry {
    private final Token token;
    private final IRValue irValue;

    public IRGeneratorStackEntry(Token token) {
        this.token = token;
        this.irValue = null;
    }

    public IRGeneratorStackEntry(IRValue irValue) {
        this.token = null;
        this.irValue = irValue;
    }

    public IRGeneratorStackEntry() {
        this.token = null;
        this.irValue = null;
    }

    public Token getToken() {
        if (this.token == null) {
            throw new RuntimeException("Not a token!");
        }
        return this.token;
    }

    public IRValue getIrValue() {
        if (this.irValue == null) {
            throw new RuntimeException("Not an irValue!");
        }
        return this.irValue;
    }
}