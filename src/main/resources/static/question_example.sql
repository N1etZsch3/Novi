CREATE TABLE question_example (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    subject VARCHAR(50) NOT NULL COMMENT '科目（如：湖北专升本英语、高等数学等）',
    question_type VARCHAR(50) NOT NULL COMMENT '题型（如：语法填空、阅读理解、选择题等）',
    difficulty VARCHAR(20) NOT NULL COMMENT '难度（simple/medium/hard）',
    content JSON NOT NULL COMMENT '题目内容（JSON格式，包含题目、选项、答案、解析等）',
    description VARCHAR(255) COMMENT '题目描述或备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_subject_type_difficulty (subject, question_type, difficulty) -- 优化查询示例题目的索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库示例表 - 存储各科目各题型的示例题目';


SET NAMES utf8mb4;

-- --------------------------------------------------------
-- Part I: 语法填空 (修复：使用 \\n 转义换行符)
-- --------------------------------------------------------
INSERT INTO question_example (subject, question_type, difficulty, content, description)
VALUES (
           '湖北专升本英语',
           '语法填空',
           'medium',
           '{"title": "Part Ⅰ 语法填空", "instructions": "阅读下面短文，在题号（第1-10题）空白处填入括号内提示词的正确形式，若无提示词则填入一个适当的单词。", "article_content": "Chinese architect, Liu Jiakun won the 2025 Pritzker Architecture Prize on the 4th of March.\\nThis is the 1 (big) award of all in architecture. He is the second person from China to win it.\\n\\nLiu lives in Chengdu and has worked 2 40 years. In 1999, he 3 (start) his company, Jiakun Architects, and has completed over 30 projects, including small museums, big buildings, 4 city plans.\\n\\nLiu does not follow one style. He uses local materials and simple designs to make 5 (beauty) buildings. After the 2008 Wenchuan earthquake, Liu made “rebirth bricks” from broken buildings — a symbol of hope.\\n\\nLiu also 6 (design) for ordinary people. For example, West Village in Chengdu, 7 (finish) in 2015, has a soccer field, a market and paths for running, 8 (walk), and cycling. Grass grows through the bricks, showing the beauty of daily life. Many people enjoy visiting this place.\\n\\n“The purpose of architecture is to create 9 pleasant and wonderful environment. People’s 10 (happy) is what we work for,” he said."}',
           '2025年湖北省专升本英语真题 - 语法填空'
       );

-- --------------------------------------------------------
-- Part II: 连词成句 (无换行符，原样保留)
-- --------------------------------------------------------
INSERT INTO question_example (subject, question_type, difficulty, content, description)
VALUES
    ('湖北专升本英语', '连词成句', 'medium', '{"question_number": 11, "words": "her homework / She / has completed"}', '连词成句 - 题11'),
    ('湖北专升本英语', '连词成句', 'medium', '{"question_number": 12, "words": "your plan / tell me / Can you"}', '连词成句 - 题12'),
    ('湖北专升本英语', '连词成句', 'medium', '{"question_number": 13, "words": "me / explain / Let / how this works"}', '连词成句 - 题13'),
    ('湖北专升本英语', '连词成句', 'medium', '{"question_number": 14, "words": "went to / and I / My friends / the same lecture"}', '连词成句 - 题14'),
    ('湖北专升本英语', '连词成句', 'medium', '{"question_number": 15, "words": "young people / DeepSeek / was established in 2023 / by a group of"}', '连词成句 - 题15'),
    ('湖北专升本英语', '连词成句', 'medium', '{"question_number": 16, "words": "to improve / is a plan / China’s technology and industries / “Made in China 2025”"}', '连词成句 - 题16');

-- --------------------------------------------------------
-- Part III: 阅读理解 - 选词/摘要填空 (修复：使用 \\n 转义换行符)
-- --------------------------------------------------------
INSERT INTO question_example (subject, question_type, difficulty, content, description)
VALUES
    (
        '湖北专升本英语', '阅读理解-填空', 'medium',
        '{"title": "Passage 1 - The Dong Grand Choir", "article_content": "The Dong Grand Choir (侗族大歌) is a unique musical tradition of the Dong people in China. For centuries, the Dong people, who live in mountain villages of Guizhou, Guangxi, and Hunan, have used these songs to share stories, teach moral values, and celebrate nature.\\n\\nUnlike most choirs, there is no conductor (指挥) or written music — the harmony is created naturally by singers of all ages. The songs are divided into two types: Galao (grand songs) and Gaxia (small songs). Galao are performed at festivals and tell long stories about history or love, while Gaxia are short tunes.\\n\\nPeople sing during daily work. The most amazing part is their singing style, combining different voice parts (high, middle, and low). Traditionally, children learn these songs by listening to elders while sitting around a big fire at night.\\n\\nHowever, this tradition faces challenges. With many young people moving to cities for jobs, fewer villagers are there to keep the singing tradition alive. To protect the tradition, local schools have started choir classes.\\n\\nThe Dong Grand Song Festival now attracts tourists worldwide every year. The Dong people believe that “a song is more valuable than rice”, showing how deeply music is rooted in their culture.", "questions": [{"num": 17, "clue": "Type of Tradition: The Dong Grand Choir is a unique ____ tradition of the Dong people in China."}, {"num": 18, "clue": "Unique Feature: Unlike most choirs, the Dong Grand Choir has no conductor or ____ music."}, {"num": 19, "clue": "Passing-Down of Tradition: Children learn the songs by ____ to elders around a big fire at night."}, {"num": 20, "clue": "Challenges: There are ____ villagers to keep the tradition alive."}, {"num": 21, "clue": "Action Taken: Local school have started choir ____ to protect the tradition."}]}',
        '阅读理解 Passage 1 - 侗族大歌'
    ),
    (
        '湖北专升本英语', '阅读理解-填空', 'medium',
        '{"title": "Passage 2 - Communication", "article_content": "When people disagree, it can be hard to find common. Research on how people handle differences has uncovered useful ways to improve communication.\\n\\nWhat the Studies Found\\nA survey of 1912 people found that we often guess others’ feelings wrong. For example, we often think that others want to argue, but the fact is that they just want to learn. This proves that most people are more open than we think.\\n\\nEasy Steps to Try\\nAsking Questions: “Can you tell me why you think that way?” Questions like this make others feel more willing to share their ideas. In a Stanford test, this simple question helped 5% more people listen to different ideas.\\n\\nTelling Stories: Sharing your life stories works very well. In the 2018 U.S. elections, volunteers talked to voters about immigration. Those who shared both personal stories and facts changed 5% more voters’ minds in just 11 minutes!\\n\\nBeing Polite: Being polite helps people keep listening. Staying calm and kind helps others understand your ideas. Remember: “Being polite costs nothing but wins everything.”\\n\\nThe Final Thought\\nAs a famous thinker said, by asking questions, sharing stories, and being polite, we can turn differences into teachers. By asking questions, we can turn disagreements into chances to grow.", "questions": [{"num": 22, "clue": "Finding: A survey of 1912 people found that we often guess others’ feelings ____."}, {"num": 23, "clue": "Solutions: The three easy steps to try include ____ questions, telling stories, and being 24."}, {"num": 24, "clue": "Solutions: ...and being ____."}, {"num": 25, "clue": "Example: Volunteers who shared both personal stories and facts changed 5% more voters’ minds about ____."}, {"num": 26, "clue": "The Final Thought: Since different opinions are “teachers”, we can turn them into chances to ____."}]}',
        '阅读理解 Passage 2 - 沟通技巧'
    );

-- --------------------------------------------------------
-- Part IV: 阅读理解 - 问答 (修复：使用 \\n 转义换行符)
-- --------------------------------------------------------
INSERT INTO question_example (subject, question_type, difficulty, content, description)
VALUES
    (
        '湖北专升本英语', '阅读理解-问答', 'medium',
        '{"title": "Passage 1 - A Journey Along the River", "article_content": "Anna and her girl friends decided to take a journey along the river’s path. They rented a small boat and packed food, drinks, and blankets for the day.\\nThe sun was shining brightly, making the river look like a sea of stars. The gentle breeze carried the scent of blooming flowers from the riverbanks nearby.\\nAs they moved forward, birds flew overhead, singing songs in the air. They enjoyed watching the ducks swim and play in the clear, sparkling water. Anna loved the calming sound of the river as it flowed past them.\\nSoon, they reached a quiet spot where they decided to stop for lunch. They spread out the blankets and sat down, enjoying sandwiches and fresh fruit. Laughter filled the air as they shared stories and recalled past adventures together. The river flowed gently by, listening quietly to their cheerful conversations.\\nAfterwards, they continued their journey, discovering new sights. They saw a family of ducks bathing in the warm afternoon sun. Anna felt grateful for this peaceful day spent with nature and good friends.\\nAs evening approached, they knew it was time to head back home. They rowed slowly, watching the sunset paint the sky with shades of red and gold. The river shimmered one last time before the stars began to twinkle above.\\nAnna and her friends promised to return to the river for another journey soon. Their hearts were full of joy and gratitude for this unforgettable day together.", "questions": ["27. What did Anna and her girl friends pack for their boat journey?", "28. How did Anna feel about the calming sound of the river?", "29. Where did Anna and her friends stop for lunch?", "30. What promise did Anna and her friends make at the end of their journey?", "31. Translate the sentence “Afterwards, they continued their journey, discovering new sights.” into Chinese."]}',
        '阅读理解问答 Passage 1 - Anna的旅程'
    ),
    (
        '湖北专升本英语', '阅读理解-问答', 'medium',
        '{"title": "Passage 2 - Letter from Kath", "article_content": "Dear Jane,\\nSorry for the late reply. I’ve been quite busy in the past months, and finally today I’m actually doing something about it.\\nSince we last saw each other, I’ve unpacked my bags in three different cities, thanks to my job.\\nI went from London to Prague to set up a new regional office there. Winter was really hard, with -15℃ in the mornings and dark really early in the evenings.\\nFrom there, I was on another three-month mission to ensure the set-up of the office in New York. I did every touristy thing you can think of when I wasn’t working, and spent most of my salary on eating out.\\nThen I was posted to Los Angeles in California. I could definitely get used to that kind of outdoor, beach lifestyle... But, as you know, I had to fly back to London to see my boyfriend Michael every other weekend.\\nMichael and I are getting married, which is also why I wanted to write. I can’t get married without my oldest friend there! The marriage ceremony is going to be at home in London in September.\\nI hope you can come!\\nAnyway, tell me all your news, and I’ll write back soon!\\nLots of love,\\nKath", "questions": ["32. For what did Kath feel sorry?", "33. Why did Kath go to Prague?", "34. On what did Kath spend most of her salary in New York?", "35. Why did Kath have to fly to London every other weekend?", "36. Translate the sentence “The marriage ceremony is going to be at home in London in September.” into Chinese."]}',
        '阅读理解问答 Passage 2 - Kath的信'
    );

-- --------------------------------------------------------
-- Part V: 翻译 (无特殊字符，原样保留)
-- --------------------------------------------------------
INSERT INTO question_example (subject, question_type, difficulty, content, description)
VALUES
    ('湖北专升本英语', '翻译', 'medium', '{"question_number": 37, "english": "They will cancel the event if ____.", "keyword": "snow", "chinese": "如果下雪，他们会取消活动。"}', '翻译 - 题37'),
    ('湖北专升本英语', '翻译', 'medium', '{"question_number": 38, "english": "This ____ painted the sky in pink and orange hues.", "keyword": "sunrise", "chinese": "这完美的日出将天空染成了粉橙交织的画卷。"}', '翻译 - 题38'),
    ('湖北专升本英语', '翻译', 'medium', '{"question_number": 39, "english": "I started drinking herbal tea instead of coffee ____.", "keyword": "lunch", "chinese": "午饭后，我改喝养生茶，不喝咖啡了。"}', '翻译 - 题39'),
    ('湖北专升本英语', '翻译', 'medium', '{"question_number": 40, "english": "Xiao Ming taught me how ____ an English dictionary.", "keyword": "use", "chinese": "小明教会了我如何使用英语词典。"}', '翻译 - 题40'),
    ('湖北专升本英语', '翻译', 'medium', '{"question_number": 41, "english": "Can you ____? I need to concentrate.", "keyword": "quiet", "chinese": "你能保持安静吗？我需要集中精力。"}', '翻译 - 题41'),
    ('湖北专升本英语', '翻译', 'medium', '{"question_number": 42, "english": "Jame’s Chinese is ____ through daily practice with language apps.", "keyword": "improve", "chinese": "靠着每天用语言应用软件打卡，詹姆斯的汉语水平正在稳步地提升。"}', '翻译 - 题42');

-- --------------------------------------------------------
-- Part VI: 书面表达 (无特殊字符，原样保留)
-- --------------------------------------------------------
INSERT INTO question_example (subject, question_type, difficulty, content, description)
VALUES (
           '湖北专升本英语',
           '书面表达',
           'medium',
           '{"question_number": 43, "prompt": "国外某艺术团希望参加2025年在东湖举行的音乐节。假设你是组委会的工作人员李华，请你用英语给他们发一封邮件。", "requirements": ["表示欢迎", "活动将于7月18号至20日举行", "请他们准备2-3个节目参加表演"], "word_limit": "不少于100词"}',
           '书面表达 - 李华邮件'
       );