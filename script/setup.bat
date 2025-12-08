@echo off
setlocal EnableDelayedExpansion

REM 获取脚本所在目录
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "SQL_DIR=%PROJECT_ROOT%\sql"

echo ========================================
echo       Novi Project DB Setup (Batch)
echo ========================================

REM 检查 mysql 命令
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] 'mysql' command not found.
    echo Please install MySQL Client or add it to PATH.
    pause
    exit /b 1
)

:DB_CONFIG_LOOP
echo.
echo --- Please enter database connection info ---

if not defined DB_HOST set "DB_HOST=127.0.0.1"
set /p "INPUT_HOST=Database Host [%DB_HOST%]: "
if not "%INPUT_HOST%"=="" set "DB_HOST=%INPUT_HOST%"

:PORT_LOOP
if not defined DB_PORT set "DB_PORT=3306"
set /p "INPUT_PORT=Database Port [%DB_PORT%]: "
if not "%INPUT_PORT%"=="" set "DB_PORT=%INPUT_PORT%"

if not defined DB_NAME set "DB_NAME=novi"
set /p "INPUT_NAME=Database Name [%DB_NAME%]: "
if not "%INPUT_NAME%"=="" set "DB_NAME=%INPUT_NAME%"

if not defined DB_USER set "DB_USER=root"
set /p "INPUT_USER=Database User [%DB_USER%]: "
if not "%INPUT_USER%"=="" set "DB_USER=%INPUT_USER%"

echo Database Password:
for /f "delims=" %%i in ('powershell -Command "$p = Read-Host -AsSecureString; $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($p); [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)"') do set "DB_PASS=%%i"

REM 生成 JWT
if not defined JWT_SECRET (
    for /f %%i in ('powershell -Command "[guid]::NewGuid().ToString()"') do set "DEFAULT_JWT=%%i"
) else (
    set "DEFAULT_JWT=%JWT_SECRET%"
)

echo JWT Token Secret (Press Enter to generate):
echo Default: %DEFAULT_JWT%
set "JWT_SECRET="
set /p "JWT_SECRET="
if "%JWT_SECRET%"=="" set "JWT_SECRET=%DEFAULT_JWT%"

echo.
echo Connecting to %DB_HOST%:%DB_PORT%...

REM 测试连接
mysql -h"%DB_HOST%" -P"%DB_PORT%" -u"%DB_USER%" -p"%DB_PASS%" -e "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Connected!
    goto DB_INIT
) else (
    echo [ERROR] Connection failed.
    set /p "RETRY=Retry? (y/n) [y]: "
    if /i "!RETRY!"=="n" exit /b 1
    goto DB_CONFIG_LOOP
)

:DB_INIT
echo.
echo Checking database '%DB_NAME%'...

mysql -h"%DB_HOST%" -P"%DB_PORT%" -u"%DB_USER%" -p"%DB_PASS%" -N -s -e "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '%DB_NAME%';" > temp_check.txt
set /p DB_EXISTS=<temp_check.txt
del temp_check.txt

if "!DB_EXISTS!" GTR "0" (
    echo [WARNING] Database '%DB_NAME%' already exists.
    set /p "CONFIRM=Re-initialize (Wipe Data)? (y/n) [n]: "
    if /i "!CONFIRM!"=="y" (
        echo Rebuilding database...
        mysql -h"%DB_HOST%" -P"%DB_PORT%" -u"%DB_USER%" -p"%DB_PASS%" -e "DROP DATABASE IF EXISTS %DB_NAME%; CREATE DATABASE %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
        set "SHOULD_INIT_SQL=true"
    ) else (
        echo Skipping SQL initialization.
        set "SHOULD_INIT_SQL=false"
        
        set /p "ADD_AI=Add new AI config? (y/n) [n]: "
        if /i "!ADD_AI!"=="y" call :CONFIGURE_AI
    )
) else (
    echo Creating database...
    mysql -h"%DB_HOST%" -P"%DB_PORT%" -u"%DB_USER%" -p"%DB_PASS%" -e "CREATE DATABASE %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    set "SHOULD_INIT_SQL=true"
)

if "%SHOULD_INIT_SQL%"=="true" (
    echo.
    echo Executing SQL files...
    
    set FILES=user_account.sql chat_session.sql chat_message.sql user_memory.sql ai_model_config.sql ai_prompt_config.sql question_category.sql question_example.sql paper_generation_record.sql paper_question_detail.sql question_generation_record.sql
    
    for %%f in (%FILES%) do (
        set "FULL_PATH=%SQL_DIR%\%%f"
        if exist "!FULL_PATH!" (
            echo Executing: %%f...
            mysql -h"%DB_HOST%" -P"%DB_PORT%" -u"%DB_USER%" -p"%DB_PASS%" -D"%DB_NAME%" --default-character-set=utf8mb4 < "!FULL_PATH!"
            if !errorlevel! neq 0 (
                echo [ERROR] Failed to execute %%f
                set /p "CONT=Continue? (y/n) [n]: "
                if /i "!CONT!" neq "y" exit /b 1
            )
        ) else (
            echo [WARNING] File not found: %%f
        )
    )
    echo [SUCCESS] SQL initialization complete.
    
    call :CONFIGURE_AI
)

REM 生成 .env
echo.
echo Generating .env file...
set "ENV_FILE=%PROJECT_ROOT%\.env"

(
echo SPRING_DATASOURCE_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%?useUnicode=true^&characterEncoding=utf-8^&useSSL=false^&serverTimezone=Asia/Shanghai
echo SPRING_DATASOURCE_USERNAME=%DB_USER%
echo SPRING_DATASOURCE_PASSWORD=%DB_PASS%
echo JWT_SECRET_KEY=%JWT_SECRET%
) > "%ENV_FILE%"

echo [SUCCESS] .env generated at %ENV_FILE%

echo.
echo ========================================
echo       Setup Complete! Enjoy Novi.
echo ========================================
pause
exit /b 0

:CONFIGURE_AI
echo.
echo --- Configure AI Model ---
echo The project needs an AI model to work properly.

set /p "AI_BASE_URL=Base URL: "
set /p "AI_MODEL_NAME=Model Name: "
echo API Key:
for /f "delims=" %%i in ('powershell -Command "$p = Read-Host -AsSecureString; $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($p); [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)"') do set "AI_API_KEY=%%i"
set /p "AI_DESC=Description [My Custom Model]: "
if "%AI_DESC%"=="" set "AI_DESC=My Custom Model"

echo Saving AI configuration...

REM 注意：批处理转义较复杂，且不支持参数化查询，存在注入风险，但在 setup 脚本中可接受
REM 引号转义可能需要 careful handling

mysql -h"%DB_HOST%" -P"%DB_PORT%" -u"%DB_USER%" -p"%DB_PASS%" -D"%DB_NAME%" -e "INSERT INTO ai_model_config (model_name, base_url, api_key, description, completions_path) VALUES ('%AI_MODEL_NAME%', '%AI_BASE_URL%', '%AI_API_KEY%', '%AI_DESC%', '/chat/completions');"

if %errorlevel% equ 0 (
    echo [SUCCESS] AI Config saved.
) else (
    echo [ERROR] Failed to save AI Config.
)
exit /b 0
