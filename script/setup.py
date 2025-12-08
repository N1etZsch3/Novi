import os
import sys
import getpass
import uuid

# 尝试导入 mysql 连接库
try:
    import pymysql
except ImportError:
    print("错误: 未检测到 'pymysql' 库。")
    print("请运行以下命令进行安装:")
    print("pip install pymysql")
    sys.exit(1)

def get_input(prompt, default=None, is_password=False):
    """获取用户输入，支持默认值和密码隐藏"""
    if default:
        prompt_text = f"{prompt} [{default}]: "
    else:
        prompt_text = f"{prompt}: "
    
    if is_password:
        value = getpass.getpass(prompt_text)
    else:
        value = input(prompt_text)
    
    if not value and default:
        return default
    return value

def get_db_config(last_config=None):
    """获取数据库配置信息，支持传入上次的配置作为默认值"""
    config = last_config or {}
    
    print("\n--- 请输入数据库连接信息 ---")
    host = get_input("数据库主机", config.get('host', '127.0.0.1'))
    
    # 端口验证循环
    while True:
        try:
            port_str = get_input("数据库端口", str(config.get('port', 3306)))
            port = int(port_str)
            if 1 <= port <= 65535:
                break
            print("错误: 端口必须在 1-65535 之间")
        except ValueError:
            print("错误: 请输入有效的数字端口")

    name = get_input("数据库名称", config.get('db_name', 'novi'))
    user = get_input("数据库用户名", config.get('user', 'root'))
    password = get_input("数据库密码", is_password=True)
    
    # JWT Secret
    default_jwt = config.get('jwt_secret', str(uuid.uuid4()))
    jwt_secret = get_input("JWT Token 密钥 (直接回车生成随机密钥)", default_jwt)

    return {
        'host': host,
        'port': port,
        'db_name': name,
        'user': user,
        'password': password,
        'jwt_secret': jwt_secret
    }

def connect_to_db(config):
    """尝试连接数据库，返回 connection 对象"""
    print(f"\n正在尝试连接至 {config['host']}:{config['port']}...")
    try:
        conn = pymysql.connect(
            host=config['host'],
            port=config['port'],
            user=config['user'],
            password=config['password'],
            charset='utf8mb4'
        )
        return conn
    except pymysql.MySQLError as e:
        print(f"连接失败: {e}")
        return None

def run_sql_file(cursor, file_path):
    """读取并执行 SQL 文件"""
    file_name = os.path.basename(file_path)
    print(f"正在执行: {file_name}...")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            sql_content = f.read()
    except IOError as e:
        print(f"错误: 无法读取文件 {file_name}: {e}")
        raise

    statements = sql_content.split(';')
    
    for statement in statements:
        stmt_clean = statement.strip()
        if not stmt_clean:
            continue
            
        try:
            cursor.execute(stmt_clean)
        except Exception as e:
            print(f"❌ 执行语句出错 (文件: {file_name})")
            print(f"语句片段: {stmt_clean[:100]}...")
            print(f"错误信息: {e}")
            
            choice = get_input("是否继续执行后续语句? (y/n)", "n")
            if choice.lower() != 'y':
                raise Exception("用户终止执行")

def execute_sql_scripts(conn, cursor):
    """执行 SQL 脚本的核心流程"""
    print("\n准备执行初始化 SQL...")
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    sql_dir = os.path.join(script_dir, '..', 'sql')
    
    sql_files_order = [
         'user_account.sql',
        'chat_session.sql',
        'chat_message.sql',
        'user_memory.sql',
        'ai_model_config.sql',
        'ai_prompt_config.sql',
        'question_category.sql',
        'question_example.sql',
        'paper_generation_record.sql',
        'paper_question_detail.sql',
        'question_generation_record.sql'
    ]
    
    for sql_file in sql_files_order:
        file_path = os.path.join(sql_dir, sql_file)
        if os.path.exists(file_path):
            run_sql_file(cursor, file_path)
            conn.commit()
        else:
            print(f"⚠️  警告: 找不到文件 {sql_file}, 跳过。")

def configure_ai_model(conn, cursor):
    """配置 AI 模型"""
    print("\n--- 配置 AI 模型 ---")
    print("项目需要配置 AI 模型才能正常使用。请提供以下信息:")
    
    base_url = get_input("Base URL")
    model_name = get_input("Model Name")
    api_key = get_input("API Key", is_password=True)
    description = get_input("模型描述", "我的自定义模型")
    
    # 默认值
    completions_path = "/chat/completions"
    
    print("\n正在保存 AI 模型配置...")
    try:
        sql = """
            INSERT INTO ai_model_config 
            (model_name, base_url, api_key, description, completions_path) 
            VALUES (%s, %s, %s, %s, %s)
        """
        cursor.execute(sql, (model_name, base_url, api_key, description, completions_path))
        conn.commit()
        print("✅ AI 模型配置已保存。")
        
    except pymysql.MySQLError as e:
        print(f"❌ 保存 AI 模型配置失败: {e}")
        print("您可以稍后手动在 ai_model_config 表中添加。")
        get_input("按回车键继续...", "")


def generate_env_file(config):
    """生成 .env 文件"""
    print("\n正在生成 .env 配置文件...")
    
    env_content = f"""SPRING_DATASOURCE_URL=jdbc:mysql://{config['host']}:{config['port']}/{config['db_name']}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME={config['user']}
SPRING_DATASOURCE_PASSWORD={config['password']}
JWT_SECRET_KEY={config['jwt_secret']}
"""
    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.join(script_dir, '..')
    env_path = os.path.join(project_root, '.env')
    
    try:
        with open(env_path, 'w', encoding='utf-8') as f:
            f.write(env_content)
        print(f"✅ 配置文件已生成: {os.path.abspath(env_path)}")
    except IOError as e:
        print(f"❌ 写入 .env 文件失败: {e}")

def main():
    print("========================================")
    print("      Novi 项目数据库初始化脚本")
    print("========================================")

    conn = None
    config = None
    
    # 1. 连接重试循环
    while conn is None:
        config = get_db_config(config)
        conn = connect_to_db(config)
        
        if conn is None:
            retry = get_input("\n连接失败。是否重新输入配置? (y/n)", "y")
            if retry.lower() != 'y':
                print("程序退出。")
                sys.exit(1)

    # 2. 数据库初始化逻辑
    try:
        cursor = conn.cursor()
        db_name = config['db_name']

        # 检查数据库是否存在
        cursor.execute(f"SHOW DATABASES LIKE '{db_name}'")
        if cursor.fetchone():
            print(f"\n检测到数据库 '{db_name}' 已存在。")
            confirm = get_input("⚠️  是否重新初始化 (这将清空旧数据!)? (y/n)", "n")
            if confirm.lower() != 'y':
                print("已取消数据库结构初始化 (仅生成 .env)。")
                # 即使不初始化 DB 结构，也可能需要配置 AI? 
                # 通常如果 DB 存在，AI 配置可能也存在。
                # 让用户选择是否配置 AI
                ai_confirm = get_input("是否需要添加新的 AI 模型配置? (y/n)", "n")
                conn.select_db(db_name)
                if ai_confirm.lower() == 'y':
                    configure_ai_model(conn, cursor)
            else:
                print(f"正在重建数据库 '{db_name}'...")
                cursor.execute(f"DROP DATABASE IF EXISTS {db_name}")
                cursor.execute(f"CREATE DATABASE {db_name} CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
                conn.select_db(db_name)
                execute_sql_scripts(conn, cursor)
                
                # 初始化后必须配置 AI
                configure_ai_model(conn, cursor)
        else:
            print(f"\n数据库 '{db_name}' 不存在，正在创建...")
            cursor.execute(f"CREATE DATABASE {db_name} CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
            conn.select_db(db_name)
            execute_sql_scripts(conn, cursor)
            # 初始化后必须配置 AI
            configure_ai_model(conn, cursor)

    except Exception as e:
        print(f"\n❌ 初始化过程中发生致命错误: {e}")
        conn.close()
        sys.exit(1)

    # 3. 生成配置
    generate_env_file(config)
    
    conn.close()
    print("\n========================================")
    print("      初始化完成! Enjoy Novi.")
    print("========================================")

if __name__ == "__main__":
    main()
