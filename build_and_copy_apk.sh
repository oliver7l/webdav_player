#!/bin/bash

# 增加版本号功能
echo "开始更新版本号..."

# 读取当前版本号
VERSION_CODE=$(grep 'versionCode' ./app/build.gradle.kts | grep -o '[0-9]\+')
VERSION_NAME=$(grep 'versionName' ./app/build.gradle.kts | grep -o '[0-9]\+\.[0-9]\+\.[0-9]\+')

# 计算新版本号
NEW_VERSION_CODE=$((VERSION_CODE + 1))

# 分解版本号并增加最后一位
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION_NAME"
NEW_PATCH=$((PATCH + 1))
NEW_VERSION_NAME="${MAJOR}.${MINOR}.${NEW_PATCH}"

# 更新build.gradle.kts文件
echo "更新版本号：${VERSION_NAME} -> ${NEW_VERSION_NAME}, ${VERSION_CODE} -> ${NEW_VERSION_CODE}"
sed -i '' "s/versionCode = ${VERSION_CODE}/versionCode = ${NEW_VERSION_CODE}/" ./app/build.gradle.kts
sed -i '' "s/versionName = \"${VERSION_NAME}\"/versionName = \"${NEW_VERSION_NAME}\"/" ./app/build.gradle.kts

# 构建debug版本的APK
echo "开始构建APK..."
./gradlew assembleDebug

# 检查构建是否成功
if [ $? -eq 0 ]; then
    echo "构建成功！"
    
    # 找到生成的APK文件
    SOURCE_APK=$(find ./app/build -name "*.apk" | head -1)
    
    # 检查源APK文件是否存在
    if [ -f "$SOURCE_APK" ]; then
        # 清理历史APK文件
        echo "清理历史APK文件..."
        rm -f ./WebDAVViewer-*.apk
        
        # 生成带有时间戳的目标文件名
        TIMESTAMP=$(date +%Y%m%d%H%M%S)
        TARGET_APK="./WebDAVViewer-${NEW_VERSION_NAME}-${TIMESTAMP}.apk"
        
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
