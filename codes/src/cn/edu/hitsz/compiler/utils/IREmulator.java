package cn.edu.hitsz.compiler.utils;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用来模拟执行 IR 的类
 */
public class IREmulator {
    public static IREmulator load(List<Instruction> instructions) {
        return new IREmulator(instructions);
    }

    public Optional<Integer> execute() {
        for (final var instruction : instructions) {
            switch (instruction.getKind()) {
                case MOV -> {
                    final var from = eval(instruction.getFrom());
                    environment.put(instruction.getResult(), from);
                }

                case ADD -> {
                    final var lhs = eval(instruction.getLHS());
                    final var rhs = eval(instruction.getRHS());
                    environment.put(instruction.getResult(), lhs + rhs);
                }

                case SUB -> {
                    final var lhs = eval(instruction.getLHS());
                    final var rhs = eval(instruction.getRHS());
                    environment.put(instruction.getResult(), lhs - rhs);
                }

                case MUL -> {
                    final var lhs = eval(instruction.getLHS());
                    final var rhs = eval(instruction.getRHS());
                    environment.put(instruction.getResult(), lhs * rhs);
                }

                case RET -> this.returnValue = eval(instruction.getReturnValue());

                default -> throw new RuntimeException("Unknown instruction kind: " + instruction.getKind());
            }
        }

        return Optional.ofNullable(this.returnValue);
    }

    public Integer eval(IRValue value) {
        if (value instanceof IRImmediate immediate) {
            return immediate.getValue();
        } else if (value instanceof IRVariable variable) {
            return environment.get(variable);
        } else {
            throw new RuntimeException("Unknown IR value type");
        }
    }

    private IREmulator(List<Instruction> instructions) {
        this.instructions = instructions;
        this.environment = new HashMap<>();
        this.returnValue = null;
    }

    private final List<Instruction> instructions;
    private final Map<IRVariable, Integer> environment;
    private Integer returnValue;
}
