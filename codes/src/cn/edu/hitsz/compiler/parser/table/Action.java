package cn.edu.hitsz.compiler.parser.table;

import java.util.Objects;

/**
 * 代表 LR 分析表 action 表中的一个动作, 你不应该修改此文件
 */
public class Action {
    public enum ActionKind {Reduce, Shift, Accept, Error}

    /**
     * @return 构造出的接受动作
     */
    public static Action accept() {
        return acceptInstance;
    }

    /**
     * @param status 移入后要加入状态栈的状态
     * @return 构造出的移入动作
     */
    public static Action shift(Status status) {
        return new Action(ActionKind.Shift, null, status);
    }

    /**
     * @param production 要规约的产生式
     * @return 构造出的规约动作
     */
    public static Action reduce(Production production) {
        return new Action(ActionKind.Reduce, production, null);
    }

    /**
     * @return 构造出的错误动作
     */
    public static Action error() {
        return errorInstance;
    }

    public ActionKind getKind() {
        return kind;
    }

    /**
     * @return 获得规约动作的产生式
     * @throws RuntimeException 动作不是规约动作
     */
    public Production getProduction() {
        if (kind != ActionKind.Reduce) {
            throw new RuntimeException("Only reduce action could have a production");
        }

        assert production != null;
        return production;
    }

    /**
     * @return 获得移入动作的状态
     * @throws RuntimeException 动作不是移入动作
     */
    public Status getStatus() {
        if (kind != ActionKind.Shift) {
            throw new RuntimeException("Only shift action could hava a status");
        }

        assert status != null;
        return status;
    }

    @Override
    public String toString() {
        return switch (kind) {
            case Accept -> "accept";
            case Error -> "";
            case Reduce -> "reduce " + production;
            case Shift -> "shift " + status;
        };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Action action
            && action.getKind().equals(kind)
            && switch (kind) {
            case Shift -> action.status.equals(status);
            case Reduce -> action.production.equals(production);
            case Accept, Error -> true;
        };
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, status, production);
    }

    private static final Action acceptInstance = new Action(ActionKind.Accept, null, null);
    private static final Action errorInstance = new Action(ActionKind.Error, null, null);

    private Action(ActionKind kind, Production production, Status status) {
        this.kind = kind;
        this.production = production;
        this.status = status;
    }

    private final ActionKind kind;
    private final Production production;
    private final Status status;
}
