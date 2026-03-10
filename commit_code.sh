#!/bin/bash

# 提交代码脚本

echo "=== 代码提交脚本 ==="

# 检查git状态
echo "检查git状态..."
git status

echo "\n=== 准备提交 ==="

# 添加所有更改的文件
echo "添加所有更改的文件..."
git add .

# 提示用户输入提交信息
echo "\n请输入提交信息："
read commit_message

# 执行git commit
echo "\n执行git commit..."
git commit -m "$commit_message"

# 询问是否推送到远程仓库
echo "\n是否推送到远程仓库？(y/n)"
read push_choice

if [ "$push_choice" = "y" ] || [ "$push_choice" = "Y" ]; then
    echo "推送到远程仓库..."
    git push
else
    echo "跳过推送操作"
fi

echo "\n=== 提交完成 ==="
