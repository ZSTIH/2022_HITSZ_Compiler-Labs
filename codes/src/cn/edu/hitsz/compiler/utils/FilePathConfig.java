package cn.edu.hitsz.compiler.utils;

/**
 * 程序中可能用到的各种路径
 */
public final class FilePathConfig {
    //==================================== 输入文件 ========================================//
    /**
     * 输入的待编译的源代码
     */
    public static final String SRC_CODE_PATH = "data/in/input_code.txt";

    /**
     * 编码表
     */
    public static final String CODING_MAP_PATH = "data/in/coding_map.csv";

    /**
     * 语法文件
     */
    public final static String GRAMMAR_PATH = "data/in/grammar.txt";

    /**
     * 第三方工具构造的 LR 分析表
     */
    public final static String LR1_TABLE_PATH = "data/in/LR1_table.csv";


    //==================================== 输出文件 ========================================//
    /**
     * 词法单元流
     */
    public static final String TOKEN_PATH = "data/out/token.txt";

    /**
     * 语义分析前的符号表
     */
    public static final String OLD_SYMBOL_TABLE = "data/out/old_symbol_table.txt";

    /**
     * 规约出的产生式列表
     */
    public static final String PARSER_PATH = "data/out/parser_list.txt";

    /**
     * 语义分析后的符号表
     */
    public static final String NEW_SYMBOL_TABLE = "data/out/new_symbol_table.txt";

    /**
     * 中间代码
     */
    public static final String INTERMEDIATE_CODE_PATH = "data/out/intermediate_code.txt";

    /**
     * IR 模拟执行的结果
     */
    public static final String EMULATE_RESULT = "data/out/ir_emulate_result.txt";

    /**
     * 汇编代码
     */
    public static final String ASSEMBLY_LANGUAGE_PATH = "data/out/assembly_language.asm";

    private FilePathConfig() {
    }
}
