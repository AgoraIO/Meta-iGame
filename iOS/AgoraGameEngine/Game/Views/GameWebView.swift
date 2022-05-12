//
//  GameView.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/19.
//

import UIKit
import WebKit
import AgoraGameEngineKit

enum GameRoleType: Int, CaseIterable {
    ///  房主
    case broadcast = 1
    /// 玩家
    case player = 2
    /// 观众
    case audience = 3
}

class GameWebView: UIView {
    var onMuteAudioClosure: ((Bool) -> Void)?
    var onLeaveGameClosure: (() -> Void)?
    var onChangeGameRoleClosure: ((_ oldRole: GameRoleType, _ newRole: GameRoleType) -> Void)?
    
    private(set) lazy var webView: UIView = {
        return UIView()
    }()
    private var viewModel: GameViewModel!
    
    init(viewModel: GameViewModel) {
        super.init(frame: .zero)
        self.viewModel = viewModel
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func loadUrl(gameId: String, roomId: String, toUser: String? = nil, roleType: GameRoleType) {
        viewModel.joinGame(gameId: gameId,
                           roomId: roomId,
                           view: webView,
                           role: roleType,
                           toUser: toUser!)
    }
    
    func reset() {
        viewModel.leaveGame()
    }
    
    private func setupUI() {
        webView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(webView)
        webView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        webView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        webView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        webView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
    
    deinit {
        print("GameWebView is deinit")
    }
}

class WeakScriptMessageDelegate: NSObject, WKScriptMessageHandler {
    weak var scriptDelegate: WKScriptMessageHandler?
    
    init(_ scriptDelegate: WKScriptMessageHandler) {
        self.scriptDelegate = scriptDelegate
        super.init()
    }
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        scriptDelegate?.userContentController(userContentController, didReceive: message)
    }
    
    deinit {
        print("WeakScriptMessageDelegate is deinit")
    }
}
