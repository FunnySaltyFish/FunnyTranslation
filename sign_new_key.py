import os
from typing import List
import subprocess
import dotenv
import sys

# apksigner sign --ks "D:\projects\mykey.keystore" --next-signer --ks "D:\projects\funny_key_new.jks" --lineage "D:\projects\key_lineage" "D:\projects\AppProjects\Mine\FunnyTranslation\translate\build\outputs\apk\common\debug\translate-common-debug.apk"
OLD_KET_PATH = r"D:\projects\mykey.keystore"
NEW_KEY_PATH = r"D:\projects\funny_key_new.jks"
LINEAGE_PATH = r"D:\projects\key_lineage"

if not (
    os.path.exists(OLD_KET_PATH) and os.path.exists(NEW_KEY_PATH) and os.path.exists(LINEAGE_PATH)
):
    print("key does not exists, exit!")
    exit(1)

dotenv.load_dotenv()

def get_recent_modified_apk(path_list: List[str]):
    recent_file = None
    recent_time = 0

    for path in path_list:
        for root, dirs, files in os.walk(path):
            for file in files:
                if file.endswith(".apk"):
                    file_path = os.path.join(root, file)
                    modified_time = os.path.getmtime(file_path)
                    if modified_time > recent_time:
                        recent_file = file_path
                        recent_time = modified_time

    return recent_file

import subprocess

def execute_command(command):
    try:
        # 执行命令并捕获输出结果
        result = subprocess.check_output(command, shell=True, encoding='utf-8')
        return result.strip()
    except subprocess.CalledProcessError as e:
        print(f"Command execution failed: {e}")
        return None


if __name__ == '__main__':
    path_list = []
    translate_dir = os.path.join(sys.argv[1], "translate")
    subdirs = ["common", "google", "build/outputs/apk", "build/intermediates/apk"]
    for each in subdirs:
        path_list.append(translate_dir + "/" + each)
    print("path_list:", path_list)
    apk_path = get_recent_modified_apk(path_list)
    print("find apk_path: ", apk_path)
    if apk_path:
        password = os.environ.get("KS_PASS")
        result = execute_command("apksigner.bat sign --ks {} --ks-pass pass:{} --next-signer --ks {} --ks-pass pass:{} --lineage {}  {}".format(OLD_KET_PATH, password, NEW_KEY_PATH, password, LINEAGE_PATH, apk_path))
        print("sign apk success!\n", result)
