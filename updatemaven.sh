#!/bin/bash
# 
# FrameworkのMavenリポジトリ作成、Commit＆Push
# 

# Mavenリポジトリ作成＆更新
./gradlew floating-window-framework:clean floating-window-framework:assembleRelease floating-window-framework:uploadArchives

# Commit＆Push
cd ../maven-repositories
git add *
git commit -m '$1'
git push origin master
# git pull origin master
