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

INSERT INTO question_example (id, subject, question_type, difficulty, content, description, created_at, updated_at) VALUES
                                                                                                                        (1, '湖北专升本英语', '语法填空', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_part1",
                                                                                                                          "title": "Part Ⅰ 语法填空",
                                                                                                                          "answers": [
                                                                                                                            "biggest",
                                                                                                                            "for",
                                                                                                                            "started",
                                                                                                                            "and",
                                                                                                                            "beautiful",
                                                                                                                            "designs",
                                                                                                                            "finished",
                                                                                                                            "walking",
                                                                                                                            "a",
                                                                                                                            "happiness"
                                                                                                                          ],
                                                                                                                          "content": "Chinese architect, Liu Jiakun won the 2025 Pritzker Architecture Prize on the 4th of March. This is the {{index}} {{short_blank}} (big) award of all in architecture. He is the second person from China to win it. {{br}} Liu lives in Chengdu and has worked {{index}} {{short_blank}} 40 years. In 1999, he {{index}} {{short_blank}} (start) his company, Jiakun Architects, and has completed over 30 projects, including small museums, big buildings, {{index}} {{short_blank}} city plans. {{br}} Liu does not follow one style. He uses local materials and simple designs to make {{index}} {{short_blank}} (beauty) buildings. After the 2008 Wenchuan earthquake, Liu made \\"rebirth bricks\\" from broken buildings — a symbol of hope. {{br}} Liu also {{index}} {{short_blank}} (design) for ordinary people. For example, West Village in Chengdu, {{index}} {{short_blank}} (finish) in 2015, has a soccer field, a market and paths for running, {{index}} {{short_blank}} (walk), and cycling. Grass grows through the bricks, showing the beauty of daily life. Many people enjoy visiting this place. {{br}} \\"The purpose of architecture is to create {{index}} {{short_blank}} pleasant and wonderful environment. People’s {{index}} {{short_blank}} (happy) is what we work for,\\" he said.",
                                                                                                                          "analyses": [
                                                                                                                            "考查最高级。范围是of all，故用biggest。",
                                                                                                                            "考查介词。for + 时间段（40 years）表示持续的时间。",
                                                                                                                            "考查时态。时间状语是1999年（过去），故用一般过去时started。",
                                                                                                                            "考查连词。列举最后一项前通常用and。",
                                                                                                                            "考查形容词。修饰名词buildings，用beautiful。",
                                                                                                                            "考查时态/主谓一致。主语Liu是第三人称单数，且陈述一般事实，用designs。",
                                                                                                                            "考查非谓语动词。West Village与finish是被动关系，且发生在过去，用过去分词finished作定语或状语。",
                                                                                                                            "考查平行结构。running, walking, and cycling 保持形式一致。",
                                                                                                                            "考查冠词。pleasant是辅音音素开头，且environment在此处是单数可数名词，用a。",
                                                                                                                            "考查名词。People''s是所有格，后接名词happiness。"
                                                                                                                          ],
                                                                                                                          "difficulty": "Medium",
                                                                                                                          "score_desc": "本大题共10小题，每小题2分，共20分"
                                                                                                                        }', '语法填空', '2025-11-28 17:09:51', '2025-12-04 19:47:13'),
                                                                                                                        (2, '湖北专升本英语', '连词成句', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_part2",
                                                                                                                          "title": "Part II 连词成句",
                                                                                                                          "answers": [
                                                                                                                            "She has completed her homework.",
                                                                                                                            "Can you tell me your plan?",
                                                                                                                            "Let me explain how this works.",
                                                                                                                            "My friends and I went to the same lecture.",
                                                                                                                            "DeepSeek was established in 2023 by a group of young people.",
                                                                                                                            "\\"Made in China 2025\\" is a plan to improve China’s technology and industries."
                                                                                                                          ],
                                                                                                                          "content": "{{index}}. her homework / She / has completed {{br}} {{long_line}} {{br}} {{index}}. your plan / tell me / Can you {{br}} {{long_line}} {{br}} {{index}}. me / explain / Let / how this works {{br}} {{long_line}} {{br}} {{index}}. went to / and I / My friends / the same lecture {{br}} {{long_line}} {{br}} {{index}}. young people / DeepSeek / was established in 2023 / by a group of {{br}} {{long_line}} {{br}} {{index}}. to improve / is a plan / China’s technology and industries / \\"Made in China 2025\\" {{br}} {{long_line}}",
                                                                                                                          "analyses": [
                                                                                                                            "考查陈述句语序。主语 (She) + 谓语 (has completed) + 宾语 (her homework)。",
                                                                                                                            "考查一般疑问句语序。情态动词 (Can) + 主语 (you) + 谓语 (tell) + 间接宾语 (me) + 直接宾语 (your plan)。",
                                                                                                                            "考查祈使句。Let + 宾语 (me) + 动词原形 (explain) + 宾语从句 (how this works)。",
                                                                                                                            "考查并列主语及语序。并列主语通常将 ''I'' 放在后面 (My friends and I) + 谓语 (went to) + 宾语 (the same lecture)。",
                                                                                                                            "考查被动语态。主语 (DeepSeek) + 被动结构 (was established) + 时间状语 (in 2023) + 动作执行者 (by a group of young people)。",
                                                                                                                            "考查主系表结构及不定式作定语。主语 (\\"Made in China 2025\\") + 系动词 (is) + 表语 (a plan) + 不定式短语 (to improve...) 修饰 plan。"
                                                                                                                          ],
                                                                                                                          "difficulty": "Easy",
                                                                                                                          "score_desc": "本大题共6小题，每小题2分，共12分"
                                                                                                                        }', '连词成句 - 题11', '2025-11-28 17:09:51', '2025-12-03 22:13:38'),
                                                                                                                        (3, '湖北专升本英语', '语法填空', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_ai_medical",
                                                                                                                          "title": "Part Ⅰ 语法填空",
                                                                                                                          "answers": [
                                                                                                                            "transforming",
                                                                                                                            "in",
                                                                                                                            "powered",
                                                                                                                            "trained",
                                                                                                                            "and",
                                                                                                                            "rivals",
                                                                                                                            "this",
                                                                                                                            "making",
                                                                                                                            "integrating",
                                                                                                                            "a"
                                                                                                                          ],
                                                                                                                          "content": "{{indent}}Artificial intelligence (AI) is rapidly {{index}} {{short_blank}}(transform) the healthcare industry in numerous ways. One of the most prominent applications is {{index}} {{short_blank}} medical diagnosis. AI systems, {{index}} {{short_blank}}(power) by machine learning, can analyze complex medical data such as images from MRIs or CT scans. These systems are {{index}} {{short_blank}}(train) on large datasets {{index}} {{short_blank}} can detect patterns indicative of diseases like cancer or neurological disorders. For example, some AI tools can identify early-stage tumors with an accuracy that {{index}} {{short_blank}}(rival) or even surpasses that of experienced radiologists. {{index}} {{short_blank}} capability allows for earlier intervention, which can significantly improve survival rates.{{br}}{{indent}}Beyond diagnosis, AI is also {{index}} {{short_blank}}(make) strides in treatment personalization. By {{index}} {{short_blank}}(integrate) data from electronic health records, genetic tests, and lifestyle factors, AI algorithms can help doctors devise customized treatment plans. In fields like oncology, this means therapies can be targeted to the unique genetic makeup of a patient''s cancer, increasing the effectiveness of treatments while minimizing side effects.{{br}}{{indent}}The drug development process is another area where AI is having {{index}} {{short_blank}} profound impact. Traditional drug discovery is time-consuming and expensive, often taking over a decade and billions of dollars to bring a new drug to market. AI can accelerate this by predicting how different compounds will interact with biological targets, identifying promising drug candidates much faster. This was evident during the COVID-19 pandemic, where AI played a role in developing vaccines and treatments rapidly.",
                                                                                                                          "analyses": [
                                                                                                                            "考查现在进行时。主语AI是单数，与is构成进行时，动词需用现在分词transforming。",
                                                                                                                            "考查介词。application in 表示“在...方面的应用”，固定搭配。",
                                                                                                                            "考查非谓语动词。过去分词powered作后置定语，修饰systems，表示被动关系。",
                                                                                                                            "考查被动语态。系统被训练，用过去分词trained。",
                                                                                                                            "考查连词。连接两个并列谓语are trained和can detect，用and。",
                                                                                                                            "考查动词时态。定语从句中，关系代词that指代accuracy，第三人称单数，故用rivals。",
                                                                                                                            "考查代词。this指代前文提到的能力，用指示代词this。",
                                                                                                                            "考查现在进行时。is making表示正在取得进展。",
                                                                                                                            "考查非谓语动词。by integrating表示通过整合的方式，动名词作介词宾语。",
                                                                                                                            "考查冠词。impact是可数名词，且首次提到，用不定冠词a。"
                                                                                                                          ],
                                                                                                                          "difficulty": "中等",
                                                                                                                          "score_desc": "本大题共10小题，每小题2分，共20分"
                                                                                                                        }', '语法填空', '2025-12-04 19:47:13', '2025-12-04 19:47:13'),
                                                                                                                        (8, '湖北专升本英语', '阅读理解-填空', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_part3_p1",
                                                                                                                          "title": "Part Ⅲ 阅读理解",
                                                                                                                          "answers": [
                                                                                                                            "musical",
                                                                                                                            "written",
                                                                                                                            "listening",
                                                                                                                            "fewer",
                                                                                                                            "classes"
                                                                                                                          ],
                                                                                                                          "content": "{{subtitle:Passage}} {{indent}}The Dong Grand Choir (侗族大歌) is a unique musical tradition of the Dong people in China. For centuries, the Dong people, who live in mountain villages of Guizhou, Guangxi, and Hunan, have used these songs to share stories, teach moral values, and celebrate nature. Unlike most choirs, there is no conductor (指挥) or written music — the harmony is created naturally by singers of all ages. {{br}} {{indent}}The songs are divided into two types: Galao (grand songs) and Gaxia (small songs). Galao are performed at festivals and tell long stories about history or love, while Gaxia are short tunes. People sing during daily work. The most amazing part is their singing style, combining different voice parts (high, middle, and low). Traditionally, children learn these songs by listening to elders while sitting around a big fire at night. {{br}} {{indent}}However, this tradition faces challenges. With many young people moving to cities for jobs, fewer villagers are there to keep the singing tradition alive. To protect the tradition, local schools have started choir classes. The Dong Grand Song Festival now attracts tourists worldwide every year. The Dong people believe that \\"a song is more valuable than rice\\", showing how deeply music is rooted in their culture. {{br}}{{br}} {{subtitle:The Dong Grand Choir}} {{br}} Type of Tradition: The Dong Grand Choir is a unique {{index}} {{short_blank}} tradition of the Dong people in China. {{br}} Unique Feature: Unlike most choirs, the Dong Grand Choir has no conductor or {{index}} {{short_blank}} music. {{br}} Passing-Down of Tradition: Children learn the songs by {{index}} {{short_blank}} to elders around a big fire at night. {{br}} Challenges: There are {{index}} {{short_blank}} villagers to keep the tradition alive. {{br}} Action Taken: Local school have started choir {{index}} {{short_blank}} to protect the tradition.",
                                                                                                                          "analyses": [
                                                                                                                            "细节题。根据第一段第一句...",
                                                                                                                            "细节题。根据第一段最后一句...",
                                                                                                                            "细节题。根据第二段...",
                                                                                                                            "推断题。根据第三段...",
                                                                                                                            "细节题。根据第三段..."
                                                                                                                          ],
                                                                                                                          "difficulty": "Medium",
                                                                                                                          "score_desc": "本篇共5小题，每题2分，共10分"
                                                                                                                        }', '阅读理解 Passage 1 - 侗族大歌', '2025-11-28 17:09:51', '2025-12-03 22:24:46'),
                                                                                                                        (10, '湖北专升本英语', '阅读理解-问答', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_part4_p1",
                                                                                                                          "title": "Part Ⅳ 阅读理解",
                                                                                                                          "answers": [
                                                                                                                            "They packed food, drinks, and blankets.",
                                                                                                                            "She loved it.",
                                                                                                                            "They stopped at a quiet spot.",
                                                                                                                            "They promised to return to the river for another journey soon.",
                                                                                                                            "后来（之后），她们继续她们的旅程，发现了新的景色。"
                                                                                                                          ],
                                                                                                                          "content": "{{subtitle:Passage}} {{indent}}Anna and her girl friends decided to take a journey along the river’s path. They rented a small boat and packed food, drinks, and blankets for the day. The sun was shining brightly, making the river look like a sea of stars. The gentle breeze carried the scent of blooming flowers from the riverbanks nearby. {{br}} {{indent}}As they moved forward, birds flew overhead, singing songs in the air. They enjoyed watching the ducks swim and play in the clear, sparkling water. Anna loved the calming sound of the river as it flowed past them. Soon, they reached a quiet spot where they decided to stop for lunch. They spread out the blankets and sat down, enjoying sandwiches and fresh fruit. Laughter filled the air as they shared stories and recalled past adventures together. The river flowed gently by, listening quietly to their cheerful conversations. {{br}} {{indent}}Afterwards, they continued their journey, discovering new sights. They saw a family of ducks bathing in the warm afternoon sun. Anna felt grateful for this peaceful day spent with nature and good friends. {{br}} {{indent}}As evening approached, they knew it was time to head back home. They rowed slowly, watching the sunset paint the sky with shades of red and gold. The river shimmered one last time before the stars began to twinkle above. Anna and her friends promised to return to the river for another journey soon. Their hearts were full of joy and gratitude for this unforgettable day together. {{br}}{{br}} {{index}}. What did Anna and her girl friends pack for their boat journey? {{br}} {{long_line}} {{br}}{{br}} {{index}}. How did Anna feel about the calming sound of the river? {{br}} {{long_line}} {{br}}{{br}} {{index}}. Where did Anna and her friends stop for lunch? {{br}} {{long_line}} {{br}}{{br}} {{index}}. What promise did Anna and her friends make at the end of their journey? {{br}} {{long_line}} {{br}}{{br}} {{index}}. Translate the sentence “Afterwards, they continued their journey, discovering new sights.” into Chinese. {{br}} {{long_line}}",
                                                                                                                          "analyses": [
                                                                                                                            "细节题。根据第一段第二句“They rented a small boat and packed food, drinks, and blankets for the day.”可知她们带了食物、饮料和毯子。",
                                                                                                                            "细节题。根据第二段第三句“Anna loved the calming sound of the river as it flowed past them.”可知Anna喜爱这平静的流水声。",
                                                                                                                            "细节题。根据第二段第四句“Soon, they reached a quiet spot where they decided to stop for lunch.”可知她们在一个安静的地方停下来吃午饭。",
                                                                                                                            "细节题。根据最后一段倒数第二句“Anna and her friends promised to return to the river for another journey soon.”可知她们许诺很快会再来这里旅行。",
                                                                                                                            "翻译题。Afterwards意为“后来/之后”，continued their journey意为“继续她们的旅程”，discovering new sights意为“发现新的景色/景观”。"
                                                                                                                          ],
                                                                                                                          "difficulty": "Medium",
                                                                                                                          "score_desc": "本篇共5小题，每题2分，共10分"
                                                                                                                        }', '阅读理解问答 Passage 1 - Anna的旅程', '2025-11-28 17:09:51', '2025-12-03 22:24:46'),
                                                                                                                        (12, '湖北专升本英语', '翻译', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_part5",
                                                                                                                          "title": "Part V 翻译",
                                                                                                                          "answers": [
                                                                                                                            "it snows",
                                                                                                                            "wonderful sunrise",
                                                                                                                            "after lunch",
                                                                                                                            "to use",
                                                                                                                            "be quiet",
                                                                                                                            "improving"
                                                                                                                          ],
                                                                                                                          "content": "{{index}}. They will cancel the event if {{short_blank}} . (snow) {{br}} 如果下雪，他们会取消活动。 {{br}}{{br}} {{index}}. This {{short_blank}} painted the sky in pink and orange hues. (sunrise) {{br}} 这完美的日出将天空染成了粉橙交织的画卷。Wonderful {{br}}{{br}} {{index}}. I started drinking herbal tea instead of coffee {{short_blank}} . (lunch) {{br}} 午饭后，我改喝养生茶，不喝咖啡了。 {{br}}{{br}} {{index}}. Xiao Ming taught me how {{short_blank}} an English dictionary. (use) {{br}} 小明教会了我如何使用英语词典。 {{br}}{{br}} {{index}}. Can you {{short_blank}} ? I need to concentrate. (quiet) {{br}} 你能保持安静吗？我需要集中精力。 {{br}}{{br}} {{index}}. Jame’s Chinese is {{short_blank}} through daily practice with language apps. (improve) {{br}} 靠着每天用语言应用软件打卡，詹姆斯的汉语水平正在稳步地提升。",
                                                                                                                          "analyses": [
                                                                                                                            "考查条件状语从句。主句是一般将来时，if引导的条件状语从句用一般现在时表将来。主语it指代天气，动词用单三形式snows。",
                                                                                                                            "考查名词短语。根据中文“完美的日出”及提示词Wonderful，此处应填wonderful sunrise。This修饰单数名词。",
                                                                                                                            "考查介词短语。根据中文“午饭后”，lunch是午饭，后用介词after，故填after lunch。",
                                                                                                                            "考查“疑问词+不定式”结构。teach sb. how to do sth. 意为“教某人如何做某事”，故填to use。",
                                                                                                                            "考查祈使句/系表结构。quiet是形容词，前需加系动词be或keep构成谓语，情态动词can后接原形，故填be quiet或keep quiet。",
                                                                                                                            "考查现在进行时。根据中文“正在……提升”及句中is，可知应用现在进行时，improve的现在分词是improving。"
                                                                                                                          ],
                                                                                                                          "difficulty": "Medium",
                                                                                                                          "score_desc": "本大题共6小题，每小题3分，共18分"
                                                                                                                        }', '翻译 - 题37', '2025-11-28 17:09:51', '2025-12-03 20:53:34'),
                                                                                                                        (18, '湖北专升本英语', '书面表达', 'medium', '{
                                                                                                                          "uuid": "2025_hb_eng_part6",
                                                                                                                          "title": "Part Ⅵ 书面表达",
                                                                                                                          "answers": [
                                                                                                                            "Dear Sir/Madam,\\n    I am Li Hua from the organizing committee. I am writing to express our warm welcome to your art troupe for wishing to join the 2025 Music Festival in East Lake.\\n    The festival is scheduled to take place from July 18th to July 20th. It will be a great opportunity to share music and culture. We would appreciate it if you could prepare 2 to 3 programs for the performance.\\n    We are looking forward to your participation.\\nYours sincerely,\\nLi Hua"
                                                                                                                          ],
                                                                                                                          "content": "{{index}}. 国外某艺术团希望参加2025年在东湖举行的音乐节。假设你是组委会的工作人员李华，请你用英语给他们发一封邮件。内容包括：{{br}}（1）表示欢迎{{br}}（2）活动将于7月18号至20日举行{{br}}（3）请他们准备2-3个节目参加表演{{br}}注意：{{br}}（1）词数不少于40词{{br}}（2）邮件格式不做要求",
                                                                                                                          "analyses": [
                                                                                                                            "本题考查应用文写作（邮件）。写作要点包括：1. 表达欢迎（warm welcome）；2. 说明活动时间（July 18th to 20th）；3. 提出表演要求（prepare 2-3 programs）。注意时态以一般将来时为主，语气需礼貌、诚恳。"
                                                                                                                          ],
                                                                                                                          "difficulty": "Medium",
                                                                                                                          "score_desc": "本大题共1小题，共10分"
                                                                                                                        }', '书面表达 - 李华邮件', '2025-11-28 17:09:52', '2025-12-03 20:53:34');
