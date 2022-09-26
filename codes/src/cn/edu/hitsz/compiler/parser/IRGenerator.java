package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.List;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        throw new NotImplementedException();
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

