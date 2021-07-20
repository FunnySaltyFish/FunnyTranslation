import sys
import os
import re
update_log = """v2.0.0 beta-4：
◆你关注的更新
—优化 大幅度提升翻译项较多时的翻译速度
—修复 插件调试页面内容显示不完整问题
—修复 因混淆导致的OkHttpUtil不可用问题

◆其他
—这两个版本相隔很久，原因很复杂。我本意用kotlin重写部分内容，写着写着感觉不搭；于是萌生全部用kotlin+MVVM重写的念头；后来又想用Jetpack Compose写页面，然后到最后又放弃了。
—无语子
"""
def get_apk_detail(apkpath):
    output = os.popen("aapt d badging %s" % apkpath).read()
    print(output)
    match = re.compile(r"package: name='(\S+)' versionCode='(\d+)' versionName='(.+?)'").match(output)
    if not match:
        raise Exception("can't get packageinfo")
 
    packagename = match.group(1)
    versionCode = match.group(2)
    versionName = match.group(3)
 
    print('packagename:' + packagename)
    print('versionCode:' + versionCode)
    print('versionName:' + versionName)

    global update_log
    with open("D:\\projects\\AppProjects\\Mine\\FunnyTranslationDownload\\updateLog.txt","a+",encoding="utf-8") as f:
        f.write(f"\n{update_log}")

    update_log = update_log.replace("\n","\\n")

    json_text = f"""{{
	"versionCode":{versionCode},
	"versionName":"{versionName}",
	"apkUrl":"https://www.coolapk.com/apk/254263",
	"isUpdate":true,
	"updateLog":"{update_log}"
}}"""

    print(json_text)
    with open("D:\\projects\\AppProjects\\Mine\\FunnyTranslationDownload\\description.json","w+",encoding="utf-8") as f:
        f.write(json_text)

    os.popen(f'copy {apkpath} D:\\projects\\AppProjects\\Mine\\FunnyTranslationDownload\\funnytranslation_{versionName}.apk')

    
    

if __name__ == "__main__":
    get_apk_detail("D:\\projects\\AppProjects\\Mine\\FunnyTranslation\\app\\release\\app-release.apk")
