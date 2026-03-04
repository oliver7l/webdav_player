#!/bin/bash

# 构建debug版本的APK
echo "开始构建APK..."
./gradlew assembleDebug

# 检查构建是否成功
if [ $? -eq 0 ]; then
    echo "构建成功！"
    
    # 定义源APK路径
    SOURCE_APK="./app/build/outputs/apk/debug/WebDAVViewer-1.0-debug-debug.apk"
    
    # 检查源APK文件是否存在
    if [ -f "$SOURCE_APK" ]; then
        # 清理历史APK文件
        echo "清理历史APK文件..."
        rm -f ./WebDAVViewer-1.0-*.apk
        
        # 生成带有时间戳的目标文件名
        TIMESTAMP=$(date +%Y%m%d%H%M%S)
        TARGET_APK="./WebDAVViewer-1.0-${TIMESTAMP}.apk"
        
        # 复制APK文件到项目根目录
        echo "复制APK到项目根目录..."
        cp "$SOURCE_APK" "$TARGET_APK"
        
        # 检查复制是否成功
        if [ $? -eq 0 ]; then
            echo "APK已成功复制到项目根目录：$TARGET_APK"
            
            # 确保/tmp/feiniu_sync目录存在
            echo "创建/tmp/feiniu_sync目录..."
            mkdir -p /tmp/feiniu_sync
            
            # 复制APK文件到/tmp/feiniu_sync目录
            echo "复制APK到/tmp/feiniu_sync目录..."
            cp "$TARGET_APK" "/tmp/feiniu_sync/"
            
            # 检查复制是否成功
            if [ $? -eq 0 ]; then
                echo "APK已成功复制到/tmp/feiniu_sync目录"
            else
                echo "复制APK到/tmp/feiniu_sync目录失败！"
            fi
        else
            echo "复制APK失败！"
        fi
    else
        echo "源APK文件不存在！"
    fi
else
    echo "构建失败！"
    exit 1
fi
