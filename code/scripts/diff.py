#!/usr/bin/python
from itertools import zip_longest
import sys


def diff(std, src):
    std_lines = map(str.strip, std.strip().splitlines())
    src_lines = map(str.strip, src.strip().splitlines())
    for idx, (std_line, src_line) in enumerate(zip_longest(std_lines, src_lines)):
        if std_line != src_line:
            return idx+1, std_line, src_line
    return 0, None, None


def do_diff(std_path, src_path):
    with open(std_path) as in_std:
        std_content = in_std.read()

    with open(src_path) as in_src:
        src_content = in_src.read()

    line_no, std_beginline, src_beginline = diff(std_content, src_content)

    if line_no == 0:
        print("The src file is the same as std file.")
    else:
        print(f"Different begin at line {line_no}:")
        print("std: " + std_beginline)
        print("src: " + src_beginline)


if __name__ == '__main__':
    _, std_path, src_path = sys.argv
    do_diff(std_path, src_path)
