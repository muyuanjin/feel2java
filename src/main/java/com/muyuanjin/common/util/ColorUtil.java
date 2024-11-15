package com.muyuanjin.common.util;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author muyuanjin
 */
@UtilityClass
public class ColorUtil {
    //@formatter:off
    // 重置
    public static final String RESET                    =  "\033[0m";                 // 重置

    // 常规颜色
    public static final String BLACK                    =  "\033[0;30m";              // 黑
    public static final String RED                      =  "\033[0;31m";              // 红
    public static final String GREEN                    =  "\033[0;32m";              // 绿
    public static final String YELLOW                   =  "\033[0;33m";              // 黄
    public static final String BLUE                     =  "\033[0;34m";              // 蓝
    public static final String PURPLE                   =  "\033[0;35m";              // 紫
    public static final String CYAN                     =  "\033[0;36m";              // 青
    public static final String WHITE                    =  "\033[0;37m";              // 白

    // 粗体
    public static final String BLACK_BOLD               =  "\033[1;30m";              // 黑
    public static final String RED_BOLD                 =  "\033[1;31m";              // 红
    public static final String GREEN_BOLD               =  "\033[1;32m";              // 绿
    public static final String YELLOW_BOLD              =  "\033[1;33m";              // 黄
    public static final String BLUE_BOLD                =  "\033[1;34m";              // 蓝
    public static final String PURPLE_BOLD              =  "\033[1;35m";              // 紫
    public static final String CYAN_BOLD                =  "\033[1;36m";              // 青
    public static final String WHITE_BOLD               =  "\033[1;37m";              // 白

    // 下划线
    public static final String BLACK_UNDERLINED         =  "\033[4;30m";              // 黑
    public static final String RED_UNDERLINED           =  "\033[4;31m";              // 红
    public static final String GREEN_UNDERLINED         =  "\033[4;32m";              // 绿
    public static final String YELLOW_UNDERLINED        =  "\033[4;33m";              // 黄
    public static final String BLUE_UNDERLINED          =  "\033[4;34m";              // 蓝
    public static final String PURPLE_UNDERLINED        =  "\033[4;35m";              // 紫
    public static final String CYAN_UNDERLINED          =  "\033[4;36m";              // 青
    public static final String WHITE_UNDERLINED         =  "\033[4;37m";              // 白

    // 背景色
    public static final String BLACK_BACKGROUND         =  "\033[40m";                // 黑
    public static final String RED_BACKGROUND           =  "\033[41m";                // 红
    public static final String GREEN_BACKGROUND         =  "\033[42m";                // 绿
    public static final String YELLOW_BACKGROUND        =  "\033[43m";                // 黄
    public static final String BLUE_BACKGROUND          =  "\033[44m";                // 蓝
    public static final String PURPLE_BACKGROUND        =  "\033[45m";                // 紫
    public static final String CYAN_BACKGROUND          =  "\033[46m";                // 青
    public static final String WHITE_BACKGROUND         =  "\033[47m";                // 白

    // 高亮度
    public static final String BLACK_BRIGHT             =  "\033[0;90m";              // 黑
    public static final String RED_BRIGHT               =  "\033[0;91m";              // 红
    public static final String GREEN_BRIGHT             =  "\033[0;92m";              // 绿
    public static final String YELLOW_BRIGHT            =  "\033[0;93m";              // 黄
    public static final String BLUE_BRIGHT              =  "\033[0;94m";              // 蓝
    public static final String PURPLE_BRIGHT            =  "\033[0;95m";              // 紫
    public static final String CYAN_BRIGHT              =  "\033[0;96m";              // 青
    public static final String WHITE_BRIGHT             =  "\033[0;97m";              // 白

    // 粗体 高亮度
    public static final String BLACK_BOLD_BRIGHT        =  "\033[1;90m";              // 黑
    public static final String RED_BOLD_BRIGHT          =  "\033[1;91m";              // 红
    public static final String GREEN_BOLD_BRIGHT        =  "\033[1;92m";              // 绿
    public static final String YELLOW_BOLD_BRIGHT       =  "\033[1;93m";              // 黄
    public static final String BLUE_BOLD_BRIGHT         =  "\033[1;94m";              // 蓝
    public static final String PURPLE_BOLD_BRIGHT       =  "\033[1;95m";              // 紫
    public static final String CYAN_BOLD_BRIGHT         =  "\033[1;96m";              // 青
    public static final String WHITE_BOLD_BRIGHT        =  "\033[1;97m";              // 白

    // 高亮度 背景色
    public static final String BLACK_BACKGROUND_BRIGHT  =  "\033[0;100m";             // 黑
    public static final String RED_BACKGROUND_BRIGHT    =  "\033[0;101m";             // 红
    public static final String GREEN_BACKGROUND_BRIGHT  =  "\033[0;102m";             // 绿
    public static final String YELLOW_BACKGROUND_BRIGHT =  "\033[0;103m";             // 黄
    public static final String BLUE_BACKGROUND_BRIGHT   =  "\033[0;104m";             // 蓝
    public static final String PURPLE_BACKGROUND_BRIGHT =  "\033[0;105m";             // 紫
    public static final String CYAN_BACKGROUND_BRIGHT   =  "\033[0;106m";             // 青
    public static final String WHITE_BACKGROUND_BRIGHT  =  "\033[0;107m";             // 白
    //@formatter:on

    public static final List<String> COLORS = List.of(
            BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN, WHITE,
            BLACK_BOLD, RED_BOLD, GREEN_BOLD, YELLOW_BOLD, BLUE_BOLD, PURPLE_BOLD, CYAN_BOLD, WHITE_BOLD,
            BLACK_UNDERLINED, RED_UNDERLINED, GREEN_UNDERLINED, YELLOW_UNDERLINED, BLUE_UNDERLINED, PURPLE_UNDERLINED, CYAN_UNDERLINED, WHITE_UNDERLINED,
            BLACK_BACKGROUND, RED_BACKGROUND, GREEN_BACKGROUND, YELLOW_BACKGROUND, BLUE_BACKGROUND, PURPLE_BACKGROUND, CYAN_BACKGROUND, WHITE_BACKGROUND,
            BLACK_BRIGHT, RED_BRIGHT, GREEN_BRIGHT, YELLOW_BRIGHT, BLUE_BRIGHT, PURPLE_BRIGHT, CYAN_BRIGHT, WHITE_BRIGHT,
            BLACK_BOLD_BRIGHT, RED_BOLD_BRIGHT, GREEN_BOLD_BRIGHT, YELLOW_BOLD_BRIGHT, BLUE_BOLD_BRIGHT, PURPLE_BOLD_BRIGHT, CYAN_BOLD_BRIGHT, WHITE_BOLD_BRIGHT,
            BLACK_BACKGROUND_BRIGHT, RED_BACKGROUND_BRIGHT, GREEN_BACKGROUND_BRIGHT, YELLOW_BACKGROUND_BRIGHT, BLUE_BACKGROUND_BRIGHT, PURPLE_BACKGROUND_BRIGHT, CYAN_BACKGROUND_BRIGHT, WHITE_BACKGROUND_BRIGHT
    );

    public static final Map<String, String> COLOR_MAP = Collections.unmodifiableMap(MapUtil.of(
            "BLACK", BLACK, "RED", RED, "GREEN", GREEN, "YELLOW", YELLOW, "BLUE", BLUE, "PURPLE", PURPLE, "CYAN", CYAN, "WHITE", WHITE,
            "BLACK_BOLD", BLACK_BOLD, "RED_BOLD", RED_BOLD, "GREEN_BOLD", GREEN_BOLD, "YELLOW_BOLD", YELLOW_BOLD, "BLUE_BOLD", BLUE_BOLD, "PURPLE_BOLD", PURPLE_BOLD, "CYAN_BOLD", CYAN_BOLD, "WHITE_BOLD", WHITE_BOLD,
            "BLACK_UNDERLINED", BLACK_UNDERLINED, "RED_UNDERLINED", RED_UNDERLINED, "GREEN_UNDERLINED", GREEN_UNDERLINED, "YELLOW_UNDERLINED", YELLOW_UNDERLINED, "BLUE_UNDERLINED", BLUE_UNDERLINED, "PURPLE_UNDERLINED", PURPLE_UNDERLINED, "CYAN_UNDERLINED", CYAN_UNDERLINED, "WHITE_UNDERLINED", WHITE_UNDERLINED,
            "BLACK_BACKGROUND", BLACK_BACKGROUND, "RED_BACKGROUND", RED_BACKGROUND, "GREEN_BACKGROUND", GREEN_BACKGROUND, "YELLOW_BACKGROUND", YELLOW_BACKGROUND, "BLUE_BACKGROUND", BLUE_BACKGROUND, "PURPLE_BACKGROUND", PURPLE_BACKGROUND, "CYAN_BACKGROUND", CYAN_BACKGROUND, "WHITE_BACKGROUND", WHITE_BACKGROUND,
            "BLACK_BRIGHT", BLACK_BRIGHT, "RED_BRIGHT", RED_BRIGHT, "GREEN_BRIGHT", GREEN_BRIGHT, "YELLOW_BRIGHT", YELLOW_BRIGHT, "BLUE_BRIGHT", BLUE_BRIGHT, "PURPLE_BRIGHT", PURPLE_BRIGHT, "CYAN_BRIGHT", CYAN_BRIGHT, "WHITE_BRIGHT", WHITE_BRIGHT,
            "BLACK_BOLD_BRIGHT", BLACK_BOLD_BRIGHT, "RED_BOLD_BRIGHT", RED_BOLD_BRIGHT, "GREEN_BOLD_BRIGHT", GREEN_BOLD_BRIGHT, "YELLOW_BOLD_BRIGHT", YELLOW_BOLD_BRIGHT, "BLUE_BOLD_BRIGHT", BLUE_BOLD_BRIGHT, "PURPLE_BOLD_BRIGHT", PURPLE_BOLD_BRIGHT, "CYAN_BOLD_BRIGHT", CYAN_BOLD_BRIGHT, "WHITE_BOLD_BRIGHT", WHITE_BOLD_BRIGHT,
            "BLACK_BACKGROUND_BRIGHT", BLACK_BACKGROUND_BRIGHT, "RED_BACKGROUND_BRIGHT", RED_BACKGROUND_BRIGHT, "GREEN_BACKGROUND_BRIGHT", GREEN_BACKGROUND_BRIGHT, "YELLOW_BACKGROUND_BRIGHT", YELLOW_BACKGROUND_BRIGHT, "BLUE_BACKGROUND_BRIGHT", BLUE_BACKGROUND_BRIGHT, "PURPLE_BACKGROUND_BRIGHT", PURPLE_BACKGROUND_BRIGHT, "CYAN_BACKGROUND_BRIGHT", CYAN_BACKGROUND_BRIGHT, "WHITE_BACKGROUND_BRIGHT", WHITE_BACKGROUND_BRIGHT
    ));
}