@echo off
chcp 65001 >nul
title 数据库导入工具

echo ========================================
echo    数据库导入工具
echo ========================================
echo.

echo [信息] 正在查找MySQL安装位置...
echo.

REM 方法1: 检查PATH环境变量中的mysql
where mysql >nul 2>&1
if not errorlevel 1 (
    echo [OK] 在PATH中找到mysql命令
    set MYSQL_CMD=mysql
    goto found_mysql
)

REM 方法2: 常见安装路径
for %%d in (
    "C:\Program Files\MySQL\MySQL Server 8.0\bin"
    "C:\Program Files\MySQL\MySQL Server 8.4\bin"
    "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin"
    "D:\Program Files\MySQL\MySQL Server 8.0\bin"
) do (
    if exist %%d\mysql.exe (
        echo [OK] 找到MySQL: %%d
        set MYSQL_CMD=%%d\mysql
        goto found_mysql
    )
)

echo [错误] 未找到mysql命令，请确保MySQL已安装并添加到PATH环境变量
pause
exit /b 1

:found_mysql
echo.

REM 输入数据库连接信息
set /p MYSQL_HOST="请输入MySQL主机地址 (默认 localhost): "
if "%MYSQL_HOST%"=="" set MYSQL_HOST=localhost

set /p MYSQL_PORT="请输入MySQL端口 (默认 3306): "
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

set /p MYSQL_USER="请输入MySQL用户名 (默认 root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="请输入MySQL密码: "

echo.
echo ========================================
echo  即将导入数据库: online_exam_system
echo  主机: %MYSQL_HOST%:%MYSQL_PORT%
echo  用户: %MYSQL_USER%
echo ========================================
echo.
set /p CONFIRM="确认导入? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo 已取消
    pause
    exit /b 0
)

echo.
echo [信息] 正在导入数据库脚本...
echo.

%MYSQL_CMD% -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% < "%~dp0online_exam_system.sql"

if errorlevel 1 (
    echo.
    echo [失败] 数据库导入失败，请检查连接信息和MySQL服务状态
) else (
    echo.
    echo [成功] 数据库导入完成！
    echo   已创建数据库 online_exam_system
    echo   已创建全部16张数据表
    echo   已导入初始测试数据（院系、班级、科目、题库）
    echo.
    echo   首次启动后端时，DataInitializer会自动创建内置账号：
    echo   管理员:   admin      密码: 123456
    echo   教师:     teacher1   密码: 123456
    echo   学生:     student001 密码: 123456
)

echo.
pause
