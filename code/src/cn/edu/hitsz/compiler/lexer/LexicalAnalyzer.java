package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        // throw new NotImplementedException();
        source = FileUtils.readFile(path);
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        // throw new NotImplementedException();
        int sourceLength = source.length();
        while (scanningPointer < sourceLength) {

            while (BLANK_CHARACTERS.contains(currentCharacter())) {
                startPointer += 1;
                scanningPointer += 1;
            }

            if (isAlphabetOrUnderline(currentCharacter())) {
                scanningPointer += 1;
                while (isAlphabetOrUnderlineOrDigit(currentCharacter())) {
                    scanningPointer += 1;
                }
                if ("int".equals(currentWord())) {
                    tokens.add(Token.simple("int"));
                } else if ("return".equals(currentWord())) {
                    tokens.add(Token.simple("return"));
                } else {
                    tokens.add(Token.normal("id", currentWord()));
                    if (!symbolTable.has(currentWord())) {
                        symbolTable.add(currentWord());
                    }
                }
                retract();
            } else if (isDigit(currentCharacter())) {
                while (isDigit(currentCharacter())) {
                    scanningPointer += 1;
                }
                tokens.add(Token.normal("IntConst", currentWord()));
                retract();
            } else {
                switch (currentCharacter()) {
                    case '=' -> tokens.add(Token.simple("="));
                    case ',' -> tokens.add(Token.simple(","));
                    case ';' -> tokens.add(Token.simple("Semicolon"));
                    case '+' -> tokens.add(Token.simple("+"));
                    case '-' -> tokens.add(Token.simple("-"));
                    case '*' -> tokens.add(Token.simple("*"));
                    case '/' -> tokens.add(Token.simple("/"));
                    case '(' -> tokens.add(Token.simple("("));
                    case ')' -> tokens.add(Token.simple(")"));
                    default -> {
                    }
                }
            }

            scanningPointer += 1;
            startPointer = scanningPointer;

        }
        tokens.add(Token.eof());
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        // throw new NotImplementedException();
        return tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }

    /**
     * 获得单词扫描指针当前所指向的字符
     * @return scanningPointer指向的字符
     */
    private Character currentCharacter() {
        return source.charAt(scanningPointer);
    }

    /**
     * 获得startPointer与scanningPointer之间的字符串
     * @return 当前所扫描出的单词
     */
    private String currentWord() {
        return source.substring(startPointer, scanningPointer);
    }

    /**
     * 判断当前字符是否为26个英文字母或下划线
     * @return 判断结果
     */
    private boolean isAlphabetOrUnderline(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch == '_');
    }

    /**
     * 判断当前字符是否为数字0-9
     * @return 判断结果
     */
    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 判断当前字符是否为26个英文字母或下划线或数字0-9
     * @return 判断结果
     */
    private boolean isAlphabetOrUnderlineOrDigit(char ch) {
        return isAlphabetOrUnderline(ch) || isDigit(ch);
    }

    /**
     * 读入无关字符并进入终止状态时，需要回退
     */
    private void retract() {
        scanningPointer -= 1;
    }

    /**
     * 待编译的源程序
     */
    private static String source;

    /**
     * 词法分析所获得的 token 列表
     */
    private static ArrayList<Token> tokens = new ArrayList<>();

    /**
     * 单词开始指针
     */
    private static int startPointer = 0;

    /**
     * 单词扫描指针
     */
    private static int scanningPointer = 0;

    /**
     * 存放空白字符的列表
     */
    private static final List<Character> BLANK_CHARACTERS = Arrays.asList(' ', '\t', '\r', '\n');

}
