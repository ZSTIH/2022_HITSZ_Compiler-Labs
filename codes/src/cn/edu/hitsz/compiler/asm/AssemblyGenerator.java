package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.*;

import java.util.*;

import static cn.edu.hitsz.compiler.utils.FileUtils.writeLines;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        // throw new NotImplementedException();
        for (Instruction instruction : originInstructions) {
            if (instruction.getKind().isBinary()) {
                var lhs = instruction.getLHS();
                var rhs = instruction.getRHS();
                var result = instruction.getResult();
                if (lhs.isImmediate() && rhs.isImmediate()) {
                    var leftValue = ((IRImmediate) lhs).getValue();
                    var rightValue = ((IRImmediate) rhs).getValue();
                    int immediate;
                    switch (instruction.getKind()) {
                        case SUB -> immediate = leftValue - rightValue;
                        case ADD -> immediate = leftValue + rightValue;
                        default -> immediate = leftValue * rightValue;
                    }
                    var from = IRImmediate.of(immediate);
                    Instruction newInstruction = Instruction.createMov(result, from);
                    instructions.add(newInstruction);
                } else if (lhs.isImmediate() && rhs.isIRVariable() && (instruction.getKind() == InstructionKind.MUL || instruction.getKind() == InstructionKind.SUB)) {
                    var immediate = (IRImmediate) lhs;
                    var tempResult = IRVariable.temp();
                    Instruction instruction1 = Instruction.createMov(tempResult, immediate);
                    instructions.add(instruction1);
                    Instruction instruction2;
                    if (instruction.getKind() == InstructionKind.SUB) {
                        instruction2 = Instruction.createSub(result, tempResult, rhs);
                    } else {
                        // MUL
                        instruction2 = Instruction.createMul(result, tempResult, rhs);
                    }
                    instructions.add(instruction2);
                } else if (lhs.isIRVariable() && rhs.isImmediate() && (instruction.getKind() == InstructionKind.MUL)) {
                    var immediate = (IRImmediate) rhs;
                    var tempResult = IRVariable.temp();
                    Instruction instruction1 = Instruction.createMov(tempResult, immediate);
                    instructions.add(instruction1);
                    Instruction instruction2 = Instruction.createMul(result, lhs, tempResult);
                    instructions.add(instruction2);
                } else {
                    instructions.add(instruction);
                }
            } else {
                instructions.add(instruction);
            }
        }

        for (Instruction instruction : instructions) {
            // 加载并保存全部中间指令的 IRVariable
            variablesInUse.addAll(getIRVariablesFromInstruction(instruction));
        }

    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        // throw new NotImplementedException();
        for (Instruction instruction : instructions) {

            if (instruction.getKind().isBinary()) {

                var lhs = instruction.getLHS();
                var rhs = instruction.getRHS();
                Operand firstOperand = new Operand(allocRegister(instruction));

                switch (instruction.getKind()) {
                    case ADD -> {
                        if (lhs.isIRVariable() && rhs.isIRVariable()) {
                            Operand secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) lhs));
                            Operand thirdOperand = new Operand(getRegisterWithIRVariable((IRVariable) rhs));
                            AsmInstruction asmInstruction = new AsmInstruction(Opcode.add, firstOperand, secondOperand, thirdOperand);
                            asmInstructions.add(asmInstruction);
                        } else {
                            Operand secondOperand;
                            Operand thirdOperand;
                            if (lhs.isImmediate()) {
                                secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) rhs));
                                thirdOperand = new Operand(((IRImmediate) lhs).getValue());
                            } else {
                                secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) lhs));
                                thirdOperand = new Operand(((IRImmediate) rhs).getValue());
                            }
                            AsmInstruction asmInstruction = new AsmInstruction(Opcode.addi, firstOperand, secondOperand, thirdOperand);
                            asmInstructions.add(asmInstruction);
                        }
                    }
                    case SUB -> {
                        if (lhs.isIRVariable() && rhs.isIRVariable()) {
                            Operand secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) lhs));
                            Operand thirdOperand = new Operand(getRegisterWithIRVariable((IRVariable) rhs));
                            AsmInstruction asmInstruction = new AsmInstruction(Opcode.sub, firstOperand, secondOperand, thirdOperand);
                            asmInstructions.add(asmInstruction);
                        } else {
                            // subi rd, rs1, imm
                            Operand secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) lhs));
                            Operand thirdOperand = new Operand(((IRImmediate) rhs).getValue());
                            AsmInstruction asmInstruction = new AsmInstruction(Opcode.subi, firstOperand, secondOperand, thirdOperand);
                            asmInstructions.add(asmInstruction);
                        }
                    }
                    case MUL -> {
                        Operand secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) lhs));
                        Operand thirdOperand = new Operand(getRegisterWithIRVariable((IRVariable) rhs));
                        AsmInstruction asmInstruction = new AsmInstruction(Opcode.mul, firstOperand, secondOperand, thirdOperand);
                        asmInstructions.add(asmInstruction);
                    }
                    default -> {
                        throw new RuntimeException("Unknown instruction type!");
                    }
                }
            } else if (instruction.getKind().isReturn()) {
                // RET
                Operand firstOperand = new Operand(Register.a0);
                var returnValue = instruction.getReturnValue();
                generateBinaryAsm(firstOperand, returnValue);
            } else {
                // MOV
                Operand firstOperand = new Operand(allocRegister(instruction));
                var from = instruction.getFrom();
                generateBinaryAsm(firstOperand, from);
            }

            removeVariablesInUse(instruction);
        }
    }

    /**
     * 生成汇编指令 li 或 mv 的函数
     *
     * @param firstOperand 汇编指令的第一个操作数
     * @param from         汇编指令第二个操作数的来源
     */
    private void generateBinaryAsm(Operand firstOperand, IRValue from) {
        if (from.isImmediate()) {
            // li rd, imm
            Operand secondOperand = new Operand(((IRImmediate) from).getValue());
            asmInstructions.add(new AsmInstruction(Opcode.li, firstOperand, secondOperand));
        } else {
            // mv rd, rs1
            Operand secondOperand = new Operand(getRegisterWithIRVariable((IRVariable) from));
            asmInstructions.add(new AsmInstruction(Opcode.mv, firstOperand, secondOperand));
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        // throw new NotImplementedException();
        List<String> toFIle = new ArrayList<>();
        toFIle.add(".text");
        for (AsmInstruction asmInstruction : asmInstructions) {
            toFIle.add(asmInstruction.toString());
        }
        writeLines(path, toFIle);
    }

    /**
     * 根据 currentInstruction 分配寄存器. <br>
     * 分配方法为释放掉被不再使用的变量占用的寄存器. <br>
     * 若无法分配则报错, 因此是不完备的寄存器分配. <br>
     *
     * @param currentInstruction 需要被分配寄存器的中间代码指令
     * @return 寄存器名
     */
    private Register allocRegister(Instruction currentInstruction) {

        var result = currentInstruction.getResult();

        // 若当前变量已经在之前被分配寄存器, 直接返回
        if (registerAllocTable.containsValue(result)) {
            return getRegisterWithIRVariable(result);
        }

        // 其它指令不能分配寄存器 a0 , 也不能分配 registerAllocTable 中已有的寄存器
        for (var registerName : Register.values()) {
            if (!registerAllocTable.containsKey(registerName) && (registerName != Register.a0)) {
                registerAllocTable.put(registerName, result);
                return registerName;
            }
        }

        // 若当前已无空闲的寄存器, 检测是否有不再被使用的变量(若有则分配存放该变量的寄存器）
        for (var registerName : registerAllocTable.keySet()) {
            if (!variablesInUse.contains(registerAllocTable.get(registerName))) {
                registerAllocTable.put(registerName, result);
                return registerName;
            }
        }

        // 否则将无法分配寄存器并报错 (实现的是不完备的寄存器分配)
        throw new RuntimeException("No enough registers!");
    }

    /**
     * 用于根据 IRVariable 从 registerAllocTable 查找出分配的寄存器
     */
    private Register getRegisterWithIRVariable(IRVariable irVariable) {
        for (var key : registerAllocTable.keySet()) {
            if (Objects.equals(registerAllocTable.get(key).getName(), irVariable.getName())) {
                return key;
            }
        }
        // 若没找到, 说明当前变量还没有被分配寄存器, 报错
        throw new RuntimeException("The variable hasn't been allocated register yet!");
    }

    /**
     * 获得当前中间代码指令的全部 IRVariable
     *
     * @param instruction 某条中间代码指令
     * @return 解析得到的 IRVariable 列表
     */
    private List<IRVariable> getIRVariablesFromInstruction(Instruction instruction) {
        List<IRVariable> variables = new ArrayList<>();
        List<IRValue> values = new ArrayList<>();
        if (instruction.getKind().isBinary()) {
            // ADD, SUB, MUL
            values.add(instruction.getResult());
            values.add(instruction.getLHS());
            values.add(instruction.getRHS());
        } else if (instruction.getKind().isReturn()) {
            // RET
            values.add(instruction.getReturnValue());
        } else {
            // MOV
            values.add(instruction.getFrom());
            values.add(instruction.getResult());
        }
        for (IRValue irValue : values) {
            if (irValue.isIRVariable()) {
                variables.add((IRVariable) irValue);
            }
        }
        return variables;
    }

    /**
     * 从 variablesInUse 中去除掉当前指令包含的 IRVariable
     *
     * @param instruction 当前中间代码指令
     */
    private void removeVariablesInUse(Instruction instruction) {
        int num = getIRVariablesFromInstruction(instruction).size();
        while (num > 0) {
            variablesInUse.remove(0);
            num--;
        }
    }

    /**
     * 生成的汇编指令列表
     */
    private final List<AsmInstruction> asmInstructions = new ArrayList<>();

    /**
     * 存放经过预处理后的中间指令
     */
    private final List<Instruction> instructions = new ArrayList<>();

    /**
     * 寄存器分配表
     */
    private final Map<Register, IRVariable> registerAllocTable = new HashMap<>();

    /**
     * 保存仍要被使用的 IRVariable , 便于寄存器分配时进行判断
     */
    private final List<IRVariable> variablesInUse = new ArrayList<>();
}

/**
 * 汇编指令
 */
class AsmInstruction {
    private final Opcode opcode;
    private final List<Operand> operands = new ArrayList<>();

    public AsmInstruction(Opcode opcode, Operand operand1, Operand operand2) {
        this.opcode = opcode;
        this.operands.add(operand1);
        this.operands.add(operand2);
    }

    public AsmInstruction(Opcode opcode, Operand operand1, Operand operand2, Operand operand3) {
        this.opcode = opcode;
        this.operands.add(operand1);
        this.operands.add(operand2);
        this.operands.add(operand3);
    }

    @Override
    public String toString() {
        String res = "\t" + this.opcode.toString() + ' ';
        List<String> operandStrings = new ArrayList<>();
        for (Operand operand : operands) {
            operandStrings.add(operand.toString());
        }
        res = res + String.join(", ", operandStrings);
        return res;
    }
}

/**
 * 寄存器
 */
enum Register {
    t0, t1, t2, t3, t4, t5, t6, a0
}

/**
 * 汇编指令的操作码
 */
enum Opcode {
    add, sub, mul, addi, subi, mv, li
}

/**
 * 汇编指令的操作数
 */
class Operand {
    Register register;
    Integer immediate;

    public Operand(Integer immediate) {
        this.immediate = immediate;
    }

    public Operand(Register register) {
        this.register = register;
    }

    @Override
    public String toString() {
        if (register != null) {
            return register.toString();
        } else {
            return immediate.toString();
        }
    }

}