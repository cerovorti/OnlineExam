@echo off
chcp 65001 >nul
title 数据库导入工具

echo ========================================
echo    数据库导入工具--by cerovorti
echo ========================================
echo.

echo [信息] 正在查找MySQL安装位置...
echo.

REM 方法1: 检查PATH环境变量中的mysql
where mysql >nul 2>&1
if not errorlevel 1 (
    echo ✅ 在PATH中找到mysql命令
    set MYSQL_CMD=mysql
    goto found_mysql
)

set MYSQL_PATHS=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe

set FOUND=0
for /L %%i in (0,1,6) do (
    if exist "!MYSQL_PATHS[%%i]!" (
        echo ✅ 找到MySQL: !MYSQL_PATHS[%%i]!
        set MYSQL_CMD="!MYSQL_PATHS[%%i]!"
        set FOUND=1
        goto found_mysql
    )
)

REM 如果都没找到，让用户手动输入
echo ❌ 自动查找未找到MySQL安装位置
echo.
echo 请手动指定MySQL的mysql.exe路径：
echo 例如: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
echo.
set /p MYSQL_CMD="请输入完整路径: "

if "%MYSQL_CMD%"=="" (
    echo ❌ 未输入路径，退出
    pause
    exit /b 1
)

if not exist "%MYSQL_CMD%" (
    echo ❌ 路径不存在: %MYSQL_CMD%
    pause
    exit /b 1
)

:found_mysql
echo.
echo [信息] 使用MySQL命令: %MYSQL_CMD%
echo.

REM 显示当前目录的SQL文件
echo 当前目录下的SQL备份文件：
dir *.sql /b 2>nul

echo.
set /p BACKUP_FILE="请输入要导入的备份文件名: "

if "%BACKUP_FILE%"=="" (
    echo ❌ 文件名不能为空
    pause
    exit /b 1
)

if not exist "%BACKUP_FILE%" (
    echo ❌ 文件不存在: %BACKUP_FILE%
    echo.
    echo 当前目录文件列表:
    dir *.sql /b
    pause
    exit /b 1
)

echo.
echo ========================================
echo 确认导入信息： %BACKUP_FILE%
echo ========================================
echo.
set /p CONFIRM="确认导入？(y/N): "

if /i not "%CONFIRM%"=="y" (
    echo 取消导入
    pause
    exit /b 0
)

echo.
echo 正在导入数据库...
echo 如果提示输入密码，请输入MySQL的root用户密码
echo.

%MYSQL_CMD% -u root -p < "%BACKUP_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo ✅ 数据库导入成功！
    echo.
    echo 现在可以启动系统了
) else (
    echo.
    echo ❌ 数据库导入失败！
    echo.
    echo 可能的原因：
    echo   - MySQL密码错误
    echo   - MySQL服务未启动
    echo   - 备份文件损坏
    echo   - 数据库已存在冲突
)

echo.
pause