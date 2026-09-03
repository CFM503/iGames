package com.igames.kids.games.trafficlight.model

enum class TrafficLightStyle(
    val title: String,
    val description: String
) {
    CLASSIC_3_LAMP(
        title = "经典三色灯",
        description = "标准道路机动车红黄绿信号灯"
    ),
    PEDESTRIAN(
        title = "行人过街灯",
        description = "会走动的绿小人与静止红小人"
    ),
    DIGITAL_COUNTDOWN(
        title = "数字倒计时灯",
        description = "超大数字倒计时显示屏"
    ),
    VEHICLE_WITH_TIMER(
        title = "复合倒计时灯",
        description = "机动车信号灯附带数显倒计时"
    )
}
