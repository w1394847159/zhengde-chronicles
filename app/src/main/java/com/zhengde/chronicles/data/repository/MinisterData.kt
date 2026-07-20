package com.zhengde.chronicles.data.repository

import com.zhengde.chronicles.game.world.Minister

/**
 * 正德朝初始大臣数据 — 真实历史人物 35 人
 *
 * 覆盖：内阁 / 六部 / 八虎 / 边军 / 言官 / 宗室
 */
object MinisterData {

    // ==================== 内阁·部堂（文官核心） ====================

    val LI_DONG_YANG = Minister(
        id = "li_dongyang", name = "李东阳", title = "内阁首辅·大学士",
        faction = "内阁", politics = 88, military = 35, intelligence = 90,
        loyalty = 75, influence = 85,
        personality = "老成持重，善周旋",
        stance = "调和派，在八虎与文官间走钢丝",
        description = "茶陵诗派领袖，四朝元老。面对刘瑾专权，表面妥协暗中保护了不少忠良。"
    )

    val YANG_TING_HE = Minister(
        id = "yang_tinghe", name = "杨廷和", title = "内阁大学士",
        faction = "内阁", politics = 90, military = 40, intelligence = 88,
        loyalty = 80, influence = 80,
        personality = "刚直果敢，强势",
        stance = "清流领袖，力主铲除八虎",
        description = "后来的首辅，嘉靖初年大礼议的核心人物。此时已是内阁中坚，对刘瑾深恶痛绝。"
    )

    val LIANG_CHU = Minister(
        id = "liang_chu", name = "梁储", title = "内阁大学士",
        faction = "内阁", politics = 78, military = 30, intelligence = 80,
        loyalty = 70, influence = 65,
        personality = "谨慎温和",
        stance = "中间派，不轻易表态",
        description = "为人谨慎，在内阁中属于调和型人物，不轻易得罪任何一方。"
    )

    val FEI_HONG = Minister(
        id = "fei_hong", name = "费宏", title = "内阁大学士",
        faction = "内阁", politics = 82, military = 25, intelligence = 85,
        loyalty = 75, influence = 60,
        personality = "正直，但不失灵活",
        stance = "倾向于清流，但不极端",
        description = "状元出身，文采斐然。后因反对宁王被诬陷罢官，嘉靖初年复起。"
    )

    val WANG_AO = Minister(
        id = "wang_ao", name = "王鏊", title = "内阁大学士",
        faction = "内阁", politics = 80, military = 30, intelligence = 82,
        loyalty = 70, influence = 55,
        personality = "博学持重",
        stance = "清流，但主张循序渐进",
        description = "学识渊博，正德初年入阁。对流瑾专权不满，但主张以柔克刚。"
    )

    val YANG_YI_QING = Minister(
        id = "yang_yiqing", name = "杨一清", title = "吏部尚书",
        faction = "内阁", politics = 85, military = 70, intelligence = 90,
        loyalty = 78, influence = 75,
        personality = "文武全才，善谋",
        stance = "能臣，主张务实治国",
        description = "三边总制备受边军爱戴，后入主吏部。曾与张永合谋除掉刘瑾，是正德朝不可多得的全才。"
    )

    // ==================== 六部·寺监 ====================

    val JIAO_FANG = Minister(
        id = "jiao_fang", name = "焦芳", title = "吏部尚书",
        faction = "八虎", politics = 45, military = 20, intelligence = 50,
        loyalty = 60, influence = 70,
        personality = "贪鄙无耻，善钻营",
        stance = "刘瑾死党，卖官鬻爵",
        description = "靠攀附刘瑾上位，为人卑鄙。曾对刘瑾说'公若为帝，我当为公效力'，为士林不齿。"
    )

    val ZHANG_CAI = Minister(
        id = "zhang_cai", name = "张彩", title = "吏部侍郎",
        faction = "八虎", politics = 40, military = 15, intelligence = 55,
        loyalty = 55, influence = 60,
        personality = "狡猾善变",
        stance = "刘瑾党羽，善于逢迎",
        description = "焦芳的副手，同样依附刘瑾。善于揣摩上意，在刘瑾面前极尽谄媚之能事。"
    )

    val LIU_YU = Minister(
        id = "liu_yu", name = "刘宇", title = "兵部尚书（刘瑾党）",
        faction = "八虎", politics = 35, military = 30, intelligence = 40,
        loyalty = 50, influence = 55,
        personality = "庸碌无能，唯利是图",
        stance = "刘瑾党羽，贪财好利",
        description = "靠贿赂刘瑾得任兵部尚书，对边务一窍不通。军中文书皆由他人代笔。"
    )

    val HE_JIAN = Minister(
        id = "he_jian", name = "何鉴", title = "兵部尚书",
        faction = "内阁", politics = 72, military = 65, intelligence = 70,
        loyalty = 75, influence = 60,
        personality = "老成宿将",
        stance = "务实派，专注边防",
        description = "长期主管边防事务，熟悉边情。虽非刘瑾一党，但在朝中也不轻易得罪人。"
    )

    val WANG_QIONG = Minister(
        id = "wang_qiong", name = "王琼", title = "兵部侍郎",
        faction = "内阁", politics = 75, military = 68, intelligence = 78,
        loyalty = 70, influence = 55,
        personality = "精明干练",
        stance = "能臣，重实务",
        description = "熟悉边防和漕运，后任兵部尚书。善于识人，曾力荐王阳明。"
    )

    val HONG_ZHONG = Minister(
        id = "hong_zhong", name = "洪锺", title = "刑部尚书",
        faction = "内阁", politics = 70, military = 45, intelligence = 72,
        loyalty = 72, influence = 50,
        personality = "方正严明",
        stance = "清流，依法办事",
        description = "执掌刑部，为人刚正不阿。虽不参与党争，但对刘瑾专权多有不满。"
    )

    // ==================== 八虎（宦官） ====================

    val LIU_JIN = Minister(
        id = "liu_jin", name = "刘瑾", title = "司礼监掌印太监",
        faction = "八虎", politics = 60, military = 25, intelligence = 75,
        loyalty = 65, influence = 95,
        personality = "狠辣精明，权力欲极强",
        stance = "独揽大权，打压文官",
        description = "八虎之首，正德初年权倾朝野。利用司礼监之权批红奏章，实为\"立皇帝\"。为人阴狠，曾创'罚米法'整治官员。"
    )

    val ZHANG_YONG = Minister(
        id = "zhang_yong", name = "张永", title = "御马监太监",
        faction = "八虎", politics = 55, military = 50, intelligence = 70,
        loyalty = 70, influence = 75,
        personality = "较有正义感，与刘瑾不和",
        stance = "八虎内部分化，可拉拢",
        description = "八虎二号人物，掌御马监和京营。与刘瑾渐生嫌隙，后与杨一清合谋除刘瑾。在八虎中算是最有良知的一个。"
    )

    val MA_YONG_CHENG = Minister(
        id = "ma_yongcheng", name = "马永成", title = "司礼监太监",
        faction = "八虎", politics = 30, military = 15, intelligence = 35,
        loyalty = 50, influence = 50,
        personality = "平庸，唯刘瑾马首是瞻",
        stance = "刘瑾跟班",
        description = "八虎之一，才能平庸，但对刘瑾忠心耿耿。主要负责为刘瑾跑腿办事。"
    )

    val GU_DA_YONG = Minister(
        id = "gu_dayong", name = "谷大用", title = "提督西厂太监",
        faction = "八虎", politics = 25, military = 20, intelligence = 40,
        loyalty = 55, influence = 60,
        personality = "凶狠贪婪",
        stance = "刘瑾党羽，掌特务机构",
        description = "提督西厂，权势极大。为人凶狠，屡兴大狱，百官闻之色变。"
    )

    val WEI_BIN = Minister(
        id = "wei_bin", name = "魏彬", title = "内官监太监",
        faction = "八虎", politics = 20, military = 10, intelligence = 25,
        loyalty = 45, influence = 35,
        personality = "庸碌无为",
        stance = "随大流，无主见",
        description = "八虎中较为低调的一个，没什么主见，跟着其他人行事。"
    )

    // ==================== 言官·文士 ====================

    val WANG_SHOU_REN = Minister(
        id = "wang_shouren", name = "王守仁", title = "兵部主事",
        faction = "心学", politics = 75, military = 88, intelligence = 98,
        loyalty = 82, influence = 25,
        personality = "知行合一，千古完人",
        stance = "心学宗师，文武全才",
        description = "王阳明！此时尚是兵部主事，因上疏言事得罪刘瑾，即将被贬龙场。然而龙场悟道后，他将成为影响东亚数百年的心学宗师。军事上更是天才，平宁王之乱只用了三十五天。"
    )

    val LI_MENG_YANG = Minister(
        id = "li_mengyang", name = "李梦阳", title = "户部郎中",
        faction = "内阁", politics = 60, military = 15, intelligence = 78,
        loyalty = 70, influence = 30,
        personality = "狂放不羁，才华横溢",
        stance = "清流，前七子领袖",
        description = "明代文坛前七子领袖，提倡'文必秦汉，诗必盛唐'。为人狂傲，多次上书弹劾刘瑾，曾被下狱。"
    )

    val HE_JING_MING = Minister(
        id = "he_jingming", name = "何景明", title = "中书舍人",
        faction = "内阁", politics = 55, military = 10, intelligence = 75,
        loyalty = 65, influence = 20,
        personality = "才子气重，清高",
        stance = "清流，前七子之一",
        description = "与李梦阳同为前七子领袖，文名极盛。对刘瑾专权不满，但不像李梦阳那样激烈。"
    )

    val KANG_HAI = Minister(
        id = "kang_hai", name = "康海", title = "翰林院修撰·状元",
        faction = "八虎", politics = 45, military = 10, intelligence = 72,
        loyalty = 50, influence = 25,
        personality = "才华横溢但依附权阉",
        stance = "曾依附刘瑾",
        description = "状元出身，文采斐然。为救李梦阳曾求助于刘瑾，因此被目为刘瑾党，后半生郁郁不得志。"
    )

    val WANG_TING_XIANG = Minister(
        id = "wang_tingxiang", name = "王廷相", title = "监察御史",
        faction = "内阁", politics = 75, military = 30, intelligence = 80,
        loyalty = 78, influence = 25,
        personality = "刚正不阿，敢言直谏",
        stance = "清流，正气凛然",
        description = "后来成为明代著名政治家、哲学家。此时任监察御史，屡次上疏弹劾刘瑾，不畏权贵。"
    )

    val LIU_JIAN = Minister(
        id = "liu_jian", name = "刘健", title = "前首辅（致仕）",
        faction = "内阁", politics = 90, military = 30, intelligence = 85,
        loyalty = 95, influence = 40,
        personality = "三朝元老，刚直不阿",
        stance = "清流领袖，虽致仕仍有影响力",
        description = "弘治朝首辅，正德初年因反对八虎被勒令致仕。虽已不在朝堂，但在文官中威望极高，一言九鼎。"
    )

    // ==================== 边军·武将 ====================

    val JIANG_BIN = Minister(
        id = "jiang_bin", name = "江彬", title = "边军游击将军",
        faction = "边军", politics = 25, military = 80, intelligence = 50,
        loyalty = 75, influence = 50,
        personality = "勇猛粗豪，善逢迎",
        stance = "主战派，陪皇帝玩",
        description = "朱厚照最宠信的武将。作战勇猛，善于逢迎圣意。常陪皇帝微服出巡、上阵杀敌，后来权势渐大。"
    )

    val XU_TAI = Minister(
        id = "xu_tai", name = "许泰", title = "边军参将",
        faction = "边军", politics = 20, military = 75, intelligence = 40,
        loyalty = 70, influence = 40,
        personality = "骁勇善战",
        stance = "主战派，江彬党羽",
        description = "边军猛将，弓马娴熟。江彬的得力助手，随皇帝征战时冲锋陷阵。"
    )

    val SHEN_ZHOU = Minister(
        id = "shen_zhou", name = "神周", title = "边军参将",
        faction = "边军", politics = 15, military = 70, intelligence = 35,
        loyalty = 65, influence = 30,
        personality = "勇猛但残暴",
        stance = "主战派",
        description = "边军将领，以勇猛著称。作战时身先士卒，但治军残暴，常纵兵劫掠。"
    )

    val QIAN_NING = Minister(
        id = "qian_ning", name = "钱宁", title = "锦衣卫指挥使",
        faction = "边军", politics = 30, military = 40, intelligence = 55,
        loyalty = 60, influence = 55,
        personality = "狡猾多变，善投机",
        stance = "先附刘瑾，后投江彬",
        description = "锦衣卫头子，靠攀附刘瑾上位。刘瑾倒台后迅速投靠江彬，为人反复无常。"
    )

    // ==================== 宗室·外藩 ====================

    val ZHU_CHEN_HAO = Minister(
        id = "zhu_chenhao", name = "朱宸濠", title = "宁王",
        faction = "宗室", politics = 55, military = 60, intelligence = 65,
        loyalty = 20, influence = 40,
        personality = "野心勃勃，善伪装",
        stance = "表面恭顺，暗藏反心",
        description = "正德朝最大的隐患。在江西暗中蓄养死士，勾结地方官将，图谋不轨。但表面上对朝廷极为恭顺，常进献珍玩以麻痹朝廷。"
    )

    // ==================== 后妃·宫廷 ====================

    val EMPRESS_XIAO = Minister(
        id = "empress_xiao", name = "夏皇后", title = "皇后",
        faction = "宫廷", politics = 40, military = 5, intelligence = 55,
        loyalty = 85, influence = 30,
        personality = "贤淑端庄",
        stance = "规劝皇帝勤政",
        description = "朱厚照的皇后，为人贤淑。对皇帝沉迷豹房深感忧虑，常婉言劝谏，但收效甚微。"
    )

    // ==================== 补充朝臣 ====================

    val SHI_DAO = Minister(
        id = "shi_dao", name = "石玠", title = "户部尚书",
        faction = "内阁", politics = 68, military = 20, intelligence = 65,
        loyalty = 70, influence = 45,
        personality = "谨小慎微",
        stance = "中规中矩",
        description = "掌管户部，为人谨慎。面对国库日益空虚的局面，常感力不从心。"
    )

    val TIAN_YU = Minister(
        id = "tian_yu", name = "田毓", title = "都察院左都御史",
        faction = "内阁", politics = 72, military = 15, intelligence = 68,
        loyalty = 72, influence = 40,
        personality = "方正古板",
        stance = "清流，主张严惩贪腐",
        description = "执掌都察院，对官员贪腐深恶痛绝。多次上书弹劾刘瑾党羽，虽屡遭打击而不悔。"
    )

    val LU_WAN = Minister(
        id = "lu_wan", name = "陆完", title = "兵部侍郎",
        faction = "内阁", politics = 65, military = 60, intelligence = 70,
        loyalty = 68, influence = 35,
        personality = "务实干练",
        stance = "主和派",
        description = "主管边务，对鞑靼主张以防御为主。后曾任兵部尚书，处理宁王叛乱相关事宜。"
    )

    val YANG_WEI_XUE = Minister(
        id = "yang_weixue", name = "杨维学", title = "工部尚书",
        faction = "内阁", politics = 60, military = 25, intelligence = 62,
        loyalty = 65, influence = 35,
        personality = "平庸守成",
        stance = "中间派",
        description = "掌管工部，能力平平，但也不参与党争，属于埋头做事的类型。"
    )

    /** 获取所有初始大臣 */
    fun getAllMinisters(): List<Minister> = listOf(
        // 内阁·部堂
        LI_DONG_YANG, YANG_TING_HE, LIANG_CHU, FEI_HONG, WANG_AO, YANG_YI_QING,
        // 六部
        JIAO_FANG, ZHANG_CAI, LIU_YU, HE_JIAN, WANG_QIONG, HONG_ZHONG, SHI_DAO, YANG_WEI_XUE,
        // 八虎
        LIU_JIN, ZHANG_YONG, MA_YONG_CHENG, GU_DA_YONG, WEI_BIN,
        // 言官·文士
        WANG_SHOU_REN, LI_MENG_YANG, HE_JING_MING, KANG_HAI, WANG_TING_XIANG, LIU_JIAN,
        // 边军·武将
        JIANG_BIN, XU_TAI, SHEN_ZHOU, QIAN_NING,
        // 宗室
        ZHU_CHEN_HAO,
        // 宫廷
        EMPRESS_XIAO,
        // 补充
        TIAN_YU, LU_WAN
    )

    /** 按派系分组 */
    fun getByFaction(faction: String): List<Minister> =
        getAllMinisters().filter { it.faction == faction }

    /** 获取某派系大臣的ID列表 */
    fun getFactionIds(faction: String): List<String> =
        getByFaction(faction).map { it.id }
}
