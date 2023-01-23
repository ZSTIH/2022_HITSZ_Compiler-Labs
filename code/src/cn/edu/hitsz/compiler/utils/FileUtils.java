package cn.edu.hitsz.compiler.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 用于方便地做文件读写的工具
 */
public final class FileUtils {
    /**
     * 读取文本文件并以String形式返回文件内容
     *
     * @param path 文本文件路径
     * @return 文本内容
     */
    public static String readFile(String path) {
        return String.join("\n", readLines(path));
    }

    /**
     * 读取文本文件并按行以 {@code ArrayList<String>} 形式返回文件内容
     *
     * @param path 文本文件路径
     * @return 文本内容
     */
    public static List<String> readLines(String path) {
        try (final var lines = Files.lines(Paths.get(path))) {
            return lines.toList();
        } catch (IOException e) {
            throw new RuntimeException("IO Exception on " + path, e);
        }
    }

    /**
     * 将内容写入指定文件
     *
     * @param path    要写入的文件路径
     * @param content 要写入的内容
     */
    public static void writeFile(String path, String content) {
        writeLines(path, List.of(content));
    }

    public static void writeLines(String path, List<String> lines) {
        try {
            Files.write(Paths.get(path), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("IO Exception for " + path);
        }
    }

    /**
     * 创建空文件
     *
     * @param path 文件路径
     */
    public static void tryCreateEmptyFile(String path) {
        try {
            Files.createFile(Paths.get(path));
        } catch (FileAlreadyExistsException e) {
            throw new RuntimeException("File already exist for " + path, e);
        } catch (IOException e) {
            throw new RuntimeException("IO Exception for " + path, e);
        }
    }

    public static List<List<String>> readCSV(String path) {
        return readLines(path).stream()
            // 当 limit 是 0 (调用无 limit 参数版本的 split 时就是这种情况) 时
            // split 会忽略尾部的空白字符串, 而当 limit=-1 时不会忽略
            // 这对 csv 是关键的, 因为 csv 里每行经常会有空白的末尾单元格
            .map(line -> line.split(",", -1))
            .map(Arrays::asList)
            .toList();
    }

    private FileUtils() {
    }
}
