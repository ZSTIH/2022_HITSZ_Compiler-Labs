package cn.edu.hitsz.compiler.ir;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 中间表示的指令.
 * <br>
 * 如果你需要扩展 IR, 你可以修改此文件. 但是你可能需要同步修改 IRGenerator, AssemblyGenerator 以及 IREmulator 中的内容.
 * <br>
 * 本项目的中间表示采用三地址代码/四元组形式, 指令与 IR 变量 (又称虚拟寄存器) 分离. 实现上采用同型 IR + 辅助用 getter 的形式, 其好处在于
 * 既实现了对不同参数数量指令的统一存储结构, 又能提供人类可读的参数访问支持.
 * <br>
 * 鉴于在一个编译器里, IR 的种类是固定的, 我们直接采用枚举来标志 IR 种类并自行在各个 getter 中判断对该种类的操作是否合法,
 * 而不是采用 Instruction 父类, BinaryInstruction, UnaryInstruction, ...子类这种继承实现.
 * 因为编译器中充斥 "对不同种类的指令采取不同操作" 这种行为, 如果要使用继承的话, 那么就只能在下面三种方法中选择:
 * <ul>
 *     <li>采用大量的 if instanceof (模式匹配的 switch 在 Java 17 LTS 还是 preview 特性, 不能用), 而这众所周知是强烈违反 OOP 原则的</li>
 *     <li>将不同的操作作为 IR 的方法, 然后每个子类 Override. 这样的缺点是会在 IR 这种理应独立的数据结构中加入过多的其他部分的代码</li>
 *     <li>实现一个 IRVisitor, 但是对于我们这种项目而言太过小题大做了</li>
 * </ul>
 * <p>
 * 说白了, IR 作为一种 "对象种类确定, 操作不确定" 的东西, 天然不适合用 OOP 处理. (除非你将 "操作" 视为对象, 这就直接是 Visitor 模式了).
 * 那不如直接怎么写死怎么来, 用枚举确定类型, 用 getter 包装不同类型的不同参数的访问, 用 createXXX 方法模拟子类构造函数.
 */
public class Instruction {
    //============================== 不同种类 IR 的构造函数 ==============================
    public static Instruction createAdd(IRVariable result, IRValue lhs, IRValue rhs) {
        return new Instruction(InstructionKind.ADD, result, List.of(lhs, rhs));
    }

    public static Instruction createSub(IRVariable result, IRValue lhs, IRValue rhs) {
        return new Instruction(InstructionKind.SUB, result, List.of(lhs, rhs));
    }

    public static Instruction createMul(IRVariable result, IRValue lhs, IRValue rhs) {
        return new Instruction(InstructionKind.MUL, result, List.of(lhs, rhs));
    }

    public static Instruction createMov(IRVariable result, IRValue from) {
        return new Instruction(InstructionKind.MOV, result, List.of(from));
    }

    public static Instruction createRet(IRValue returnValue) {
        return new Instruction(InstructionKind.RET, null, List.of(returnValue));
    }


    //============================== 不同种类 IR 的参数 getter ==============================
    public InstructionKind getKind() {
        return kind;
    }

    public IRVariable getResult() {
        ensureKindMatch(Set.of(InstructionKind.ADD, InstructionKind.SUB, InstructionKind.MUL, InstructionKind.MOV));
        return result;
    }

    public IRValue getLHS() {
        ensureKindMatch(Set.of(InstructionKind.ADD, InstructionKind.SUB, InstructionKind.MUL));
        return operands.get(0);
    }

    public IRValue getRHS() {
        ensureKindMatch(Set.of(InstructionKind.ADD, InstructionKind.SUB, InstructionKind.MUL));
        return operands.get(1);
    }

    public IRValue getFrom() {
        ensureKindMatch(Set.of(InstructionKind.MOV));
        return operands.get(0);
    }

    public IRValue getReturnValue() {
        ensureKindMatch(Set.of(InstructionKind.RET));
        return operands.get(0);
    }


    //============================== 基础设施 ==============================
    @Override
    public String toString() {
        final var kindString = kind.toString();
        final var resultString = result == null ? "" : result.toString();
        final var operandsString = operands.stream().map(Objects::toString).collect(Collectors.joining(", "));
        return "(%s, %s, %s)".formatted(kindString, resultString, operandsString);
    }

    public List<IRValue> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    private Instruction(InstructionKind kind, IRVariable result, List<IRValue> operands) {
        this.kind = kind;
        this.result = result;
        this.operands = operands;
    }

    private final InstructionKind kind;
    private final IRVariable result;
    private final List<IRValue> operands;

    private void ensureKindMatch(Set<InstructionKind> targetKinds) {
        final var kind = getKind();
        if (!targetKinds.contains(kind)) {
            final var acceptKindsString = targetKinds.stream()
                .map(InstructionKind::toString)
                .collect(Collectors.joining(","));

            throw new RuntimeException(
                "Illegal operand access, except %s, but given %s".formatted(acceptKindsString, kind));
        }
    }
}
