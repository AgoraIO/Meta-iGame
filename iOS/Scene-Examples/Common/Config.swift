//
//  Config.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/4.
//

import Foundation

let SYNC_MANAGER_PARAM_KEY_ID = "defaultChannel"
/// 子房间名
let SYNC_COLLECTION_SUB_ROOM = "SubRoom"

let SYNC_MANAGER_PARAM_KEY_APPID = "appId"
/// 礼物
let SYNC_MANAGER_GIFT_INFO = "giftInfo"
/// PK游戏信息
let SYNC_MANAGER_GAME_APPLY_INFO = "gameApplyInfo"
/// 观众游戏信息
let SYNC_MANAGER_GAME_INFO = "gameInfo"
/// pk信息
let SYNC_MANAGER_PK_INFO = "pkInfo"
/// 用户信息
let SYNC_MANAGER_AGORA_VOICE_USERS = "agoraVoiceUsers"

struct UserInfo {
    static var userId: UInt {
        let id = UserDefaults.standard.integer(forKey: "UserId")
        if id > 0 {
            return UInt(id)
        }
        let user = UInt(arc4random_uniform(8999999) + 1000000)
        UserDefaults.standard.set(user, forKey: "UserId")
        UserDefaults.standard.synchronize()
        return user
    }
    static var uid: String {
        "\(userId)"
    }
    
    static func getGameToken() -> String? {
        guard let token = UserDefaults.standard.value(forKey: "GameToken") as? String else {
            return nil
        }
        return token
    }
    
    static func setGameToken(token: String) {
        UserDefaults.standard.set(token, forKey: "GameToken")
    }
    
    static func setGameCode(code: String?) {
        UserDefaults.standard.set(code, forKey: "GameCode")
    }
    
    static func getGameCode() -> String? {
        guard let code = UserDefaults.standard.value(forKey: "GameCode") as? String else {
            return nil
        }
        return code
    }
}

class SettingData {
    static let share = SettingData()
    
    /// 是否使用自定义WebView
    var useCustomWebView = false
    
    var useTestEnvForGame = false
    
    /// 使用忽然测试环境
    var useTestSubEnv = false
    
    var refreshTime = 4
}
