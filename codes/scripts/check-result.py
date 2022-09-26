#!/usr/bin/python
import diff
import sys
import os

id_dict = {1: ["token.txt", "old_symbol_table.txt"], 2: ["parser_list.txt"], 3: [
    "ir_emulate_result.txt", "new_symbol_table.txt"], 4: ["assembly_language.asm"]}

# 需要改为你自己的 rars.jar 路径
rars_path = "/home/test/rars.jar"


if __name__ == '__main__':
    _, _lab_id, std_dir, out_dir = sys.argv
    lab_id = int(_lab_id)

    if lab_id <= 3:
        diff_range = lab_id
    if lab_id == 4:
        diff_range = 3

    for i in range(1, diff_range + 1):
        print(f"Diffing lab{i} output:")
        for filename in id_dict[i]:
            out_path = os.path.join(out_dir, filename)
            std_path = os.path.join(std_dir, filename)
            print(f"Diffing file {filename}:")
            diff.do_diff(std_path, out_path)
        print()

    if lab_id == 4:
        os.system(f"java -jar {rars_path} mc CompactDataAtZero a0 nc dec ae255 " +
                  os.path.join(out_dir, "assembly_language.asm"))
