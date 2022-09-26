package cn.edu.hitsz.compiler.lexer;

/**
 * 词法单元的实现, 你不应该修改该文件
 * <br>
 * 词法单元 (Token) 是词法分析的结果. 词法分析从源程序文件的文本流中识别出结构, 将一个或多个合并起来表示特定含义的字符合并, 组成词法单元.
 * <br>
 * 词法单元的结构非常简单, 其具有类型与可能的描述文本, 后者在一些复杂的词法单元如标识符,
 * 数字字面量中表示该词法单元的内容, 其又称为词素 (lexeme)
 * <br>
 * 为了方便与统一词法单元的构造, 我们将词法单元的构造函数设为了私有的, 通过公有静态函数进行构造,
 * 这将提升代码可读性并便于我们在构造时执行一定的检查
 *
 * @see TokenKind 词法单元的类型, 其具有一定的复杂结构
 */
public class Token {
    /**
     * @return 代表 EOF 的 token
     */
    public static Token eof() {
        return new Token(TokenKind.eof(), "");
    }

    /**
     * @param tokenKindId token 类型的字符串表示
     * @return 具有该 token 类型的一简单 token (不带其它文本表示, 比如标点/关键字)
     */
    public static Token simple(String tokenKindId) {
        return simple(TokenKind.fromString(tokenKindId));
    }

    /**
     * @param kind token 类型
     * @return 具有该 token 类型的一简单 token (不带其它文本表示, 比如标点/关键字)
     */
    public static Token simple(TokenKind kind) {
        return normal(kind, "");
    }

    /**
     * @param tokenKindId token 类型的字符串表示
     * @return 具有该 token 类型的一正常 token (带其它文本表示, 比如标识符/数字文本)
     */
    public static Token normal(String tokenKindId, String text) {
        return normal(TokenKind.fromString(tokenKindId), text);
    }

    /**
     * @param kind token 类型
     * @param text 源文本
     * @return 具有该 token 类型的一正常 token (带其它文本, 比如标识符/数字文本)
     */
    public static Token normal(TokenKind kind, String text) {
        return new Token(kind, text);
    }

    /**
     * @return 该 token 的类型的文本表示
     */
    public String getKindId() {
        return kind.getIdentifier();
    }

    /**
     * @return 该 token 的类型
     */
    public TokenKind getKind() {
        return kind;
    }

    /**
     * @return 该 token 的文本, 有可能为空字符串 (但恒不为 null)
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "(%s,%s)".formatted(kind, text);
    }

    private Token(TokenKind kind, String text) {
        this.kind = kind;
        this.text = text;
    }

    private final TokenKind kind;
    private final String text;
}
