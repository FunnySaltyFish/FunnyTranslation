import subprocess

def get_recent_commits(num_commits):
    # 执行 git log 命令以获取最近的提交记录
    command = ['git', 'log', f'-n{num_commits}']
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8')

    if result.returncode == 0:
        # 获取输出结果并按行分割
        output = result.stdout.strip().split('\n')

        # 提取每次提交的信息
        commits = []
        commit = {}
        for line in output:
            if line.startswith('commit'):
                if commit:
                    commits.append(commit)
                commit = {'hash': line.split()[1]}
            elif line.startswith('Author:'):
                commit['author'] = line[8:].strip()
            elif line.startswith('Date:'):
                commit['date'] = line[6:].strip()
            elif line.startswith('    '):
                if 'message' in commit:
                    commit['message'] += line[4:]
                else:
                    commit['message'] = line[4:]

        if commit:
            commits.append(commit)

        return commits
    else:
        print(f"Error: {result.stderr}")
        return []

# 使用示例：获取最近 5 次提交记录
commits = get_recent_commits(int(input("请输入获取最近多少次提交记录：")))
s = ""
for commit in commits:
#     print(f"Commit: {commit['hash']}")
#     print(f"Author: {commit['author']}")
#     print(f"Date: {commit['date']}")
#     print(f"Message: {commit['message']}")
#     print()
    s += f"- {commit['message']}\n"

print(s)
import clipboard
clipboard.copy(s)
