# -*-coding:UTF-8-*-
import sys
import shutil
import StringIO
import os
import os.path
import time
import datetime

reload(sys)
sys.setdefaultencoding('UTF-8')

root_dir = os.getcwd()

proj_name = os.path.basename(os.getcwd())

def change_string(file, old_str, new_str):
    input_file = open(file)
    lines = input_file.readlines()
    input_file.close()
    output = open(file, u'w')
    for line in lines:
        if not line:
            break
        if old_str in line:
            temp = line.split(old_str)
            temp1 = temp[0] + new_str + temp[1]
            output.write(temp1)
        else:
            output.write(line)
    output.close()
    return 0

if __name__ == "__main__":
    t = time.time()
    #const_file = os.path.join(root_dir, "")
    #change_string(const_file,"",str(0))
    	
    release_aar = os.path.join(root_dir, 'build/outputs/aar/doadlib-debug.aar')

    if os.path.exists(release_aar):
        os.remove(release_aar)
    print("********************begin build debug*********************")
    os.system('gradle --console rich assembleDebug')
    print("********************end build debug*********************")
    if os.path.exists(release_aar):
        shutil.copyfile(release_aar, os.path.join(root_dir, 'aar/debug/alx.aar'))
    raw_input()
