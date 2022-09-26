#!/usr/bin/python
from typing import Callable, Iterable, Iterator
import os
import shutil


def all_path(root: str) -> Iterator[list[str]]:
    for sub in os.listdir(root):
        full = os.path.join(root, sub)
        if os.path.isdir(full):
            yield from map(lambda sub_list: [sub] + sub_list, all_path(full))
        else:
            yield [sub]


def transform_for_dir(from_dir: str, to_dir: str, transfromer: Callable[[list[str]], list[str]]):
    for paths in all_path(from_dir):
        from_path = os.path.join(from_dir, *paths)
        to_path = os.path.join(to_dir, *paths)

        to_dir_path = os.path.join(to_dir, *paths[:-1])
        os.makedirs(to_dir_path, exist_ok=True)

        with open(from_path, 'r') as fin:
            old_lines = fin.readlines()
            new_lines = transfromer(old_lines)

        with open(to_path, 'w') as fout:
            fout.writelines(new_lines)


def get_indent(line: str):
    space_count = 0
    while line[space_count] == ' ':
        space_count += 1
    return ' ' * space_count


def replace_begin(lines: list[str]) -> list[str]:
    new_lines: list[str] = []

    need_import = False

    in_delete = False
    in_replace = False

    for line in lines:

        content = line.strip()
        if content == "//@begin":
            in_replace = True
            need_import = True

        elif content == "//@begin-del":
            in_delete = True

        elif content == "//@end":
            if in_replace:
                new_lines.append(get_indent(line) +
                                 "throw new NotImplementedException();\n")

            in_replace = False
            in_delete = False

        elif not in_delete and not in_replace:
            new_lines.append(line)

    if need_import:
        new_lines.insert(
            2, "import cn.edu.hitsz.compiler.NotImplementedException;\n")

    return new_lines


if __name__ == '__main__':
    transform_for_dir("src", "template/src", replace_begin)
    shutil.copytree("./data", "./template/data")
    shutil.copytree("./scripts", "./template/scripts")
    os.system("cp .gitignore template")
    os.system("rm ./template/data/out/*")
