//
//  ViewController.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit
import AgoraUIKit_iOS

class MainViewController: BaseViewController {
    private lazy var tableView: AGETableView = {
        let view = AGETableView()
        view.estimatedRowHeight = 100
        view.delegate = self
        view.register(MainTableViewCell.self,
                      forCellWithReuseIdentifier: MainTableViewCell.description())
        view.dataArray = MainModel.mainDatas()
        return view
    }()
    
    var rightButtonItem: UIBarButtonItem!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "home".localized
        setupUI()
        
        updateToken()
        updateGameCode()
    }
    
    private func setupUI() {
        view.addSubview(tableView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        
        rightButtonItem = UIBarButtonItem(title: "设置", style: .plain, target: self, action: #selector(rightButtonTap))
        navigationItem.rightBarButtonItem = rightButtonItem
    }
    
    private func updateToken() {
        let token = TokenBuilder.buildToken(KeyCenter.game_app_id,
                                            appCertificate: KeyCenter.game_app_certificate,
                                            userUuid: UserInfo.uid)
        UserInfo.setGameToken(token: token)
    }
    
    fileprivate func updateGameCode() {
        GameNetworkManager.getCode(UserInfo.uid) { gameCode in
            UserInfo.setGameCode(code: gameCode)
        } fail: { error in
            LogUtils.log(message: error.localizedDescription, level: .info)
        }
    }
    
    @objc func rightButtonTap() {
        let vc = SettingViewController()
        vc.modalPresentationStyle = .pageSheet
        navigationController?.pushViewController(vc, animated: true)
    }
}

extension MainViewController: AGETableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: MainTableViewCell.description(),
                                                 for: indexPath) as! MainTableViewCell
        cell.setupData(model: MainModel.mainDatas()[indexPath.row])
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let sceneType = MainModel.mainDatas()[indexPath.row].sceneType
        SyncUtil.initSyncManager(sceneId: sceneType.rawValue)
        
        let roomListVC = LiveRoomListController(sceneType: sceneType)
        roomListVC.title = MainModel.mainDatas()[indexPath.row].title
        navigationController?.pushViewController(roomListVC, animated: true)
    }
    
    func pullToRefreshHandler() {
        
    }
}
