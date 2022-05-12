# 互动游戏 GameSDK Demo

目前包含以下场景

|场景|工程名称|
|----|----|
|直播间游戏PK|[AgoraGameEngine](./AgoraGameEngine/)|

# 前提条件
开始前，请确保你的开发环境满足如下条件：
- Xcode 12 或以上版本。
- iOS 13 或以上版本的设备。

# 使用
#### 注册配置所需参数

前往 [Agora官网](https://console.agora.io/) 注册项目，生成不带证书校验的 appId

然后替换工程 `AgoraGameEngine/Common` 中 `KeyCenter.swift` 中 `rtc_app_id`

再注册一个带证书校验的项目，用于游戏 SDK 验证,**你需要提交[工单][工单链接]联系我们开通相关权限**

然后将相关字段填写到上述文件中的`game_app_id`,`game_app_certificate`中


[工单链接]: https://docs.agora.io/cn/Agora%20Platform/ticket?platform=All%20Platforms