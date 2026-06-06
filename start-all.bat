@echo off
chcp 65001 >nul
echo ========================================
echo   Social Platform - 微服务启动脚本
echo ========================================
echo.

echo [1/10] 检查中间件...
echo.

REM 检查 MySQL
netstat -ano | findstr ":3306" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] MySQL 已在 3306 运行
) else (
    echo   [!] MySQL 未运行，请先启动 MySQL
)

REM 检查 Redis
netstat -ano | findstr ":6379" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Redis 已在 6379 运行
) else (
    echo   [!] Redis 未运行，请先启动 Redis
)

REM 检查 Nacos
netstat -ano | findstr ":8848" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Nacos 已在 8848 运行
) else (
    echo   [!] Nacos 未运行，请先启动 Nacos
    echo       启动命令: cd D:\development_tool\nacos\bin && startup.cmd -m standalone
)

REM 检查 RocketMQ NameServer
netstat -ano | findstr ":9876" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] RocketMQ NameServer 已在 9876 运行
) else (
    echo   [!] RocketMQ NameServer 未运行
)

REM 检查 Sentinel Dashboard
netstat -ano | findstr ":8858" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Sentinel Dashboard 已在 8858 运行
) else (
    echo   [!] Sentinel Dashboard 未运行
    echo       启动命令: java -Dserver.port=8858 -jar C:\Users\lenovo\sentinel\sentinel-dashboard-1.8.8.jar
)

echo.
echo [2/10] 启动后端服务...
echo.

cd /d "%~dp0backend"

echo 启动 social-user (8081)...
start "social-user" cmd /c "mvn spring-boot:run -pl social-user -DskipTests"
timeout /t 5 /nobreak >nul

echo 启动 social-post (8082)...
start "social-post" cmd /c "mvn spring-boot:run -pl social-post -DskipTests"
timeout /t 3 /nobreak >nul

echo 启动 social-social (8083)...
start "social-social" cmd /c "mvn spring-boot:run -pl social-social -DskipTests"
timeout /t 3 /nobreak >nul

echo 启动 social-message (8084)...
start "social-message" cmd /c "mvn spring-boot:run -pl social-message -DskipTests"
timeout /t 3 /nobreak >nul

echo 启动 social-search (8085)...
start "social-search" cmd /c "mvn spring-boot:run -pl social-search -DskipTests"
timeout /t 3 /nobreak >nul

echo 启动 social-statistics (8086)...
start "social-statistics" cmd /c "mvn spring-boot:run -pl social-statistics -DskipTests"
timeout /t 3 /nobreak >nul

echo 启动 social-ai (8087)...
start "social-ai" cmd /c "mvn spring-boot:run -pl social-ai -DskipTests"
timeout /t 3 /nobreak >nul

echo 启动 social-gateway (8080)...
start "social-gateway" cmd /c "mvn spring-boot:run -pl social-gateway -DskipTests"

echo.
echo [3/10] 等待服务启动...
echo.
timeout /t 30 /nobreak >nul

echo ========================================
echo   服务启动完成！
echo ========================================
echo.
echo   Nacos 控制台:    http://localhost:8848/nacos
echo   Gateway 网关:    http://localhost:8080
echo   Sentinel 面板:   http://localhost:8858
echo   前端开发服务:    http://localhost:3000
echo.
echo   Nacos 账号: nacos / nacos
echo   Sentinel 账号: sentinel / sentinel
echo.
pause
