//
//  GameViewModel.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/22.
//

import UIKit
import AgoraGameEngineKit
import WebKit

protocol GameViewModelDelegate: NSObjectProtocol {
    func gameViewModelDidRecv(msg: String)
}

class GameViewModel: NSObject {
    
    var channelName: String = ""
    
    private var ownerId: String = ""
    private var gameList = [Int : [GameCenterModel]]()
    private var gameAllList = [GameCenterModel]()
    private var requestSceneType: SceneType?
    private var requestSuccessBlock: (([GameCenterModel]?) -> Void)?
    private var gameOption: AGEGameOptions?
    weak var delegate: GameViewModelDelegate?
    
    init(channleName: String, ownerId: String) {
        super.init()
        self.channelName = channleName
        self.ownerId = ownerId
        
        let logConfig = AGELogConfig()
        logConfig.level = .debug
        let context = AGEGameContext()
        context.uid = "\(UserInfo.userId)"
        context.rtmToken = UserInfo.getGameToken()!
        context.appId = KeyCenter.game_app_id
        context.logconfig = logConfig
        context.isDebuggable = SettingData.share.useTestSubEnv
        LogUtils.log(message: context.isDebuggable ? "isDebuggable = true" : "isDebuggable = false", level: .info)
        context.delegate = self
        AgoraGameEngine.sharedInstance().setup(gameContext: context)
        if SettingData.share.useTestEnvForGame {
            AgoraGameEngine.sharedInstance().setOption(operationId: 1, optionId: "set_game_center_test_env", args: "1")
        }
        
        AgoraGameEngine.sharedInstance().setOption(operationId: 1, optionId: "frame_rate", args: "\(SettingData.share.refreshTime)")
    }
    
    deinit {
        LogUtils.log(message: "GameViewModel deinit", level: .info)
    }
    
    func joinGame(gameId: String,
                  roomId: String,
                  view: UIView,
                  role: GameRoleType,
                  toUser: String,
                  vendorId: String,
                  viewWidth: CGFloat,
                  viewHeight: CGFloat) {
        
        guard let gameCode = UserInfo.getGameCode() else {
            fatalError("can not find game code")
        }
        
        LogUtils.log(message: "角色 \(role.rawValue)", level: .info)
        LogUtils.log(message: "roomId \(roomId)", level: .info)
        let options = AGEGameOptions()
        options.gameId = gameId
        options.roomId = roomId
        options.userId = UserInfo.uid
        options.role = "\(role.rawValue)"
        options.language = "zh-CN"
        options.gameType = vendorId
        
        LogUtils.log(message: "loadGame vendorId: \(vendorId)", level: .info)
        
        let userName = "User-\(UserInfo.uid)"
        var dict: [String : Any]
        if vendorId == "10020" { /** Provider 1 **/
            dict = ["avatar" : "https://terrigen-cdn-dev.marvel.com/content/prod/1x/012scw_ons_crd_02.jpg",
                    "name" : userName,
                    "time_limit" : 360,
                    "show_join" : 1,
                    "show_ready" : 1,
                    "show_start" : 1,
                    "show_kickout" : 1,
                    "to_user" : toUser,
                    "avatar_type" : 2,
                    "game_code" : gameCode,
                    "game_appid" : KeyCenter.huran_game_app_id,
                    "game_appkey" : KeyCenter.huran_game_app_key,]
        }
        else {
            dict = ["avatar" : "https://terrigen-cdn-dev.marvel.com/content/prod/1x/012scw_ons_crd_02.jpg",
                    "name" : userName,
                    "time_limit" : 360,
                    "show_join" : 1,
                    "show_ready" : 1,
                    "show_start" : 1,
                    "show_kickout" : 1,
                    "to_user" : toUser,
                    "avatar_type" : 2,
                    "game_code" : gameCode,
                    "game_appid" : KeyCenter.huran_game_app_id,
                    "game_appkey" : KeyCenter.huran_game_app_key,
                    "width" : viewWidth * UIScreen.main.nativeScale,
                    "height" : viewHeight * UIScreen.main.nativeScale]
            LogUtils.log(message: "viewWidth: \(viewWidth), viewHeight: \(viewHeight)", level: .info)
        }
        let string = dict.toJsonString()
        options.options = string!
        
        let ret = AgoraGameEngine.sharedInstance().loadGame(options: options, view: view)
        if ret == .OK {
            LogUtils.log(message: "loadGame success", level: .info)
        }
        else {
            LogUtils.log(message: "loadGame fail \(ret)", level: .error)
        }
        gameOption = options
    }
    
    var currentPageIndex = 1
    var currentReqPageIndex: Int?
    var noNeedToRequestGameList = false
    
    func fetchNextPagFameList(sceneType: SceneType,
                              success: @escaping ([GameCenterModel]?) -> Void,
                              all: @escaping ([GameCenterModel]?) -> Void) {
        if noNeedToRequestGameList {
            all(gameAllList)
            return
        }
        getGameList(pageIndex: currentPageIndex,
                    sceneType: sceneType,
                    success: success)
    }
    
    func getGameList(pageIndex: Int,
                     sceneType: SceneType,
                     success: @escaping ([GameCenterModel]?) -> Void) {
        if currentReqPageIndex == pageIndex {
            return
        }
        
        currentReqPageIndex = pageIndex
        if let cache = gameList[pageIndex] {
            success(cache)
            currentReqPageIndex = nil
            return
        }
        requestSceneType = sceneType
        requestSuccessBlock = success
        let ret = AgoraGameEngine.sharedInstance().getOption(operationId: 1, optionId: AGEOptionIDGetGameList, args: "{\"page\":\(pageIndex),\"limit\":10,\"language\":\"zh-CN\"}")
        if ret == .OK {
            LogUtils.log(message: "getOption success", level: .info)
        }
        else {
            LogUtils.log(message: "getOption fail \(ret)", level: .error)
        }
    }
    
    func leaveGame() {
        AgoraGameEngine.sharedInstance().leaveGame()
    }
    
    /// 发送礼物
    func postGiftHandler(gameId: String, giftType: LiveGiftModel.GiftType) {
        postGiftHandler(gameId: gameId, giftType: giftType, playerId: ownerId)
    }
    
    func postGiftHandler(gameId: String, giftType: LiveGiftModel.GiftType, playerId: String) {
        AgoraGameEngine.sharedInstance().setOption(operationId: 2, optionId: AGEOptionIDSendGift, args: "{\"to\":\"\(playerId)\",\"payload\":{\"giftCost\":\(giftType.rawValue),\"count\":1}}")
    }
    
    /// 发弹幕
    func postBarrage(gameId: String) {
        postBarrage(gameId: gameId, playerId: ownerId)
    }
    func postBarrage(gameId: String, playerId: String) {
        
    }
    
    /// 离开游戏
    func leaveGame(gameId: String, roleType: GameRoleType) {
        
    }
    
    func changeRole(gameId: String, oldRole: GameRoleType, newRole: GameRoleType) {
    }
    
    func handleGameListResult(str: String) {
        let decoder = JSONDecoder()
        let data = str.data(using: .utf8)!
        do {
            let obj = try decoder.decode(GameListObj.self, from: data)
            let infos = Array(obj.items.reversed())
            if !infos.isEmpty {
                gameList[currentPageIndex] = infos
                gameAllList.append(contentsOf: infos)
                currentPageIndex += 1
                invokeSuccess(infos: infos)
            }
            else {
                noNeedToRequestGameList = true
            }
            currentReqPageIndex = nil
        } catch let err {
            currentReqPageIndex = nil
            LogUtils.log(message: "handleGameListResult decode error \(err)", level: .error)
        }
    }
    
    func invokeSuccess(infos: [GameCenterModel]) {
        if let success = requestSuccessBlock {
            DispatchQueue.main.async { [weak self] in
                success(infos)
                self?.requestSuccessBlock = nil
            }
        }
    }
}


extension GameViewModel: AGEGameEngineEventProtocol {
    func onSetOptionResult(_ operationId: Int, optionId: String, result: Bool, reason: String?) {
        LogUtils.log(message: "\(optionId): \(reason ?? "none")", level: .info)
    }
    
    func onGetOptionResult(_ operationId: Int, optionId: String, result: Bool, outOption: String?) {
        if result, AGEOptionIDGetGameList == optionId,
           let string = outOption {
            handleGameListResult(str: string)
        }
    }
    
    func onMessage(_ messageId: String, message: String) {
        let str =  "AGE：messageId:\(messageId) message:\(message)"
        LogUtils.log(message: str, level: .info)
        if Thread.isMainThread {
            delegate?.gameViewModelDidRecv(msg: str)
        }
        else {
            DispatchQueue.main.async { [weak self] in
                self?.delegate?.gameViewModelDidRecv(msg: str)
            }
        }
    }
}

extension GameViewModel {
    struct GameListObj: Codable {
        let size: Int
        let currentPage: Int
        let items: [GameCenterModel]
    }
    
    struct GameItem: Codable {
        let gameId: String
        let gameName: String
        let gameDesc: String
        let iconUrl: String
        let vendorId: String
        let display: Display
        let playPattern: PlayPattern
        let billing: Billing
        let language: String
        let updateTime: TimeInterval
    }
    
    struct Display: Codable {
        let layout: String
        let isFullScreen: Bool
        let suggestResolution: String
    }
    
    struct PlayPattern: Codable {
        let maxPlayerNum: Int
        let type: Double
    }
    
    struct Billing: Codable {
        let mode: Double
        let unitPrice: Double
        let miniCost: Double
    }
}

// MARK: 字典转字符串
extension Dictionary {
    
    func toJsonString() -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: self,
                                                     options: []) else {
            return nil
        }
        guard let str = String(data: data, encoding: .utf8) else {
            return nil
        }
        return str
     }
    
}

