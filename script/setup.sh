#!/bin/bash

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# 项目根目录 (假设脚本在 script/ 下)
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SQL_DIR="$PROJECT_ROOT/sql"

# 定义颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "      Novi 项目数据库初始化 (Shell版)"
echo "========================================"

# 检查 mysql 客户端命令是否存在
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: 未找到 'mysql' 命令。${NC}"
    echo "请安装 MySQL 客户端 (如 mysql-client) 后重试。"
    exit 1
fi

# 获取输入函数
get_input() {
    local prompt="$1"
    local default="$2"
    local var_name="$3"
    local is_pass="$4"
    
    local input_str
    local prompt_text="$prompt"
    
    if [ -n "$default" ]; then
        prompt_text="$prompt [$default]"
    fi
    
    if [ "$is_pass" == "true" ]; then
        read -s -p "$prompt_text: " input_str
        echo "" # 换行
    else
        read -p "$prompt_text: " input_str
    fi
    
    if [ -z "$input_str" ] && [ -n "$default" ]; then
        eval $var_name='$default'
    else
        eval $var_name='$input_str'
    fi
}

# 循环配置直到连接成功
while true; do
    echo -e "\n--- 请输入数据库连接信息 ---"
    
    get_input "数据库主机" "${DB_HOST:-127.0.0.1}" DB_HOST
    
    while true; do
        get_input "数据库端口" "${DB_PORT:-3306}" DB_PORT
        if [[ "$DB_PORT" =~ ^[0-9]+$ ]] && [ "$DB_PORT" -ge 1 ] && [ "$DB_PORT" -le 65535 ]; then
            break
        else
            echo -e "${RED}错误: 端口必须是 1-65535 之间的数字${NC}"
        fi
    done
    
    get_input "数据库名称" "${DB_NAME:-novi}" DB_NAME
    get_input "数据库用户名" "${DB_USER:-root}" DB_USER
    get_input "数据库密码" "" DB_PASS "true"
    
    # 生成默认UUID (简单模拟)
    # 尝试多种方式获取 UUID
    if command -v uuidgen &> /dev/null; then
        DEFAULT_JWT=$(uuidgen)
    else
        # Fallback to date + random if uuidgen is missing
        DEFAULT_JWT="novi-default-$(date +%s)-$RANDOM"
    fi

    get_input "JWT Token 密钥 (直接回车生成)" "$DEFAULT_JWT" JWT_SECRET
    
    echo -e "\n正在尝试连接至 $DB_HOST:$DB_PORT..."
    
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &> /dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}连接成功!${NC}"
        break
    else
        echo -e "${RED}连接失败，请检查配置。${NC}"
        get_input "是否重试? (y/n)" "y" RETRY
        if [ "$RETRY" != "y" ]; then
            echo "程序退出。"
            exit 1
        fi
    fi
done

# AI配置函数
configure_ai() {
    echo -e "\n--- 配置 AI 模型 ---"
    echo "项目需要配置 AI 模型才能正常使用。请提供以下信息:"
    
    get_input "Base URL" "" AI_BASE_URL
    get_input "Model Name" "" AI_MODEL_NAME
    get_input "API Key" "" AI_API_KEY "true"
    get_input "模型描述" "我的自定义模型" AI_DESC
    
    echo -e "\n正在保存 AI 模型配置..."
    
    # 构建 SQL (注意转义)
    # 使用 Python 脚本可以避免注入更安全，但在纯 bash 下，我们尽力而为
    # 这里假设输入没有极其恶意的单引号
    
    SQL_INSERT="INSERT INTO ai_model_config (model_name, base_url, api_key, description, completions_path) VALUES ('$AI_MODEL_NAME', '$AI_BASE_URL', '$AI_API_KEY', '$AI_DESC', '/chat/completions');"
    
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -e "$SQL_INSERT"
    
    if [ $? -eq 0 ]; then
         echo -e "${GREEN}✅ AI 模型配置已保存。${NC}"
    else
         echo -e "${RED}❌ 保存 AI 模型配置失败。${NC}"
         echo "您可以稍后手动在 ai_model_config 表中添加。"
    fi
}

# 数据库初始化逻辑
echo -e "\n正在检查数据库 '$DB_NAME'..."

DB_EXISTS_CMD="SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '$DB_NAME';"
DB_EXISTS=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -N -s -e "$DB_EXISTS_CMD")

if [ "$DB_EXISTS" -gt 0 ]; then
    echo -e "${YELLOW}检测到数据库 '$DB_NAME' 已存在。${NC}"
    get_input "⚠️  是否重新初始化 (这将清空旧数据!)? (y/n)" "n" CONFIRM
    
    if [ "$CONFIRM" != "y" ]; then
        echo "已取消结构初始化，仅生成 .env 文件。"
        SHOULD_INIT_SQL="false"
        
        get_input "是否需要添加新的 AI 模型配置? (y/n)" "n" ADD_AI
        if [ "$ADD_AI" == "y" ]; then
            configure_ai
        fi
    else
        echo "正在重建数据库..."
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "DROP DATABASE IF EXISTS $DB_NAME; CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
        SHOULD_INIT_SQL="true"
    fi
else
    echo "数据库不存在，正在创建..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    SHOULD_INIT_SQL="true"
fi

# 执行 SQL 文件
if [ "$SHOULD_INIT_SQL" == "true" ]; then
    echo -e "\n准备执行初始化 SQL..."
    
    # 定义必须按顺序执行的文件列表
    FILES=(
        "user_account.sql"
        "chat_session.sql"
        "chat_message.sql"
        "user_memory.sql"
        "ai_model_config.sql"
        "ai_prompt_config.sql"
        "question_category.sql"
        "question_example.sql"
        "paper_generation_record.sql"
        "paper_question_detail.sql"
        "question_generation_record.sql"
    )
    
    for FILE in "${FILES[@]}"; do
        FULL_PATH="$SQL_DIR/$FILE"
        if [ -f "$FULL_PATH" ]; then
            echo "正在执行: $FILE..."
            
            mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" --default-character-set=utf8mb4 < "$FULL_PATH"
            
            if [ $? -ne 0 ]; then
                echo -e "${RED}❌ 执行 $FILE 失败${NC}"
                get_input "是否继续执行后续文件? (y/n)" "n" CONT
                if [ "$CONT" != "y" ]; then
                     echo "用户终止执行。"
                     exit 1
                fi
            fi
        else
            echo -e "${YELLOW}警告: 找不到文件 $FILE${NC}"
        fi
    done
    echo -e "\n${GREEN}SQL 初始化完成。${NC}"
    
    # 在完全初始化后，强制要求配置 AI
    configure_ai
fi

# 生成 .env 文件
echo -e "\n正在生成 .env 配置文件..."
ENV_FILE="$PROJECT_ROOT/.env"

cat > "$ENV_FILE" <<EOL
SPRING_DATASOURCE_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASS}
JWT_SECRET_KEY=${JWT_SECRET}
EOL

if [ -f "$ENV_FILE" ]; then
    echo -e "${GREEN}✅ 配置文件已生成: $ENV_FILE${NC}"
else
    echo -e "${RED}写入 .env 失败${NC}"
fi

echo "========================================"
echo "      初始化完成! Enjoy Novi."
echo "========================================"
