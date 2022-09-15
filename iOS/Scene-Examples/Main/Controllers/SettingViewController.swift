//
//  SettingViewController.swift
//  AgoraGameEngine
//
//  Created by ZYP on 2022/5/10.
//

import Foundation
import UIKit
import AgoraRtcKit
import AgoraGameEngineKit

class SettingViewController: UIViewController {
    let appVersionLabel = UILabel()
    let rtcVersionLabel = UILabel()
    let gameVersionLabel = UILabel()
    let descLabel = UILabel()
    let switchButton = UISwitch()
    let envLabel = UILabel()
    let envSwitch = UISwitch()
    let refreshButton = UIButton()
    let subEnvLabel = UILabel()
    let subEnvSwitch = UISwitch()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    func setup() {
        title = "设置"
        view.backgroundColor = .white
        let infoDict = Bundle.main.infoDictionary!
        let version = (infoDict["CFBundleShortVersionString"] as! String)
        let buildVersion = (infoDict["CFBundleVersion"] as! String)
        let appVersionStr = version + "(\(buildVersion))"
        
        appVersionLabel.text = "app版本: \(appVersionStr)"
        rtcVersionLabel.text = "rtc版本: \(AgoraRtcEngineKit.getSdkVersion())"
        gameVersionLabel.text = "game版本: \(AgoraGameEngine.getVersion())"
        descLabel.text = "是否使用自定义webview"
        envLabel.text = "使用测试环境的游戏"
        subEnvLabel.text = "忽然测试ENV"
        
        view.addSubview(appVersionLabel)
        view.addSubview(rtcVersionLabel)
        view.addSubview(gameVersionLabel)
        view.addSubview(descLabel)
        view.addSubview(switchButton)
        view.addSubview(envLabel)
        view.addSubview(envSwitch)
        view.addSubview(refreshButton)
        view.addSubview(subEnvLabel)
        view.addSubview(subEnvSwitch)
        
        appVersionLabel.translatesAutoresizingMaskIntoConstraints = false
        rtcVersionLabel.translatesAutoresizingMaskIntoConstraints = false
        gameVersionLabel.translatesAutoresizingMaskIntoConstraints = false
        descLabel.translatesAutoresizingMaskIntoConstraints = false
        switchButton.translatesAutoresizingMaskIntoConstraints = false
        envLabel.translatesAutoresizingMaskIntoConstraints = false
        envSwitch.translatesAutoresizingMaskIntoConstraints = false
        subEnvLabel.translatesAutoresizingMaskIntoConstraints = false
        subEnvSwitch.translatesAutoresizingMaskIntoConstraints = false
        refreshButton.translatesAutoresizingMaskIntoConstraints = false
        
        appVersionLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        appVersionLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 15).isActive = true
        
        rtcVersionLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        rtcVersionLabel.topAnchor.constraint(equalTo: appVersionLabel.bottomAnchor, constant: 5).isActive = true
        
        gameVersionLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        gameVersionLabel.topAnchor.constraint(equalTo: rtcVersionLabel.bottomAnchor, constant: 5).isActive = true
        
        descLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: gameVersionLabel.bottomAnchor, constant: 20).isActive = true
        
        switchButton.centerYAnchor.constraint(equalTo: descLabel.centerYAnchor).isActive = true
        switchButton.leftAnchor.constraint(equalTo: descLabel.rightAnchor, constant: 10).isActive = true
        
        envLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        envLabel.topAnchor.constraint(equalTo: switchButton.bottomAnchor, constant: 20).isActive = true
        
        envSwitch.centerYAnchor.constraint(equalTo: envLabel.centerYAnchor).isActive = true
        envSwitch.leftAnchor.constraint(equalTo: envLabel.rightAnchor, constant: 10).isActive = true
        
        subEnvLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        subEnvLabel.topAnchor.constraint(equalTo: envSwitch.bottomAnchor, constant: 20).isActive = true
        
        subEnvSwitch.centerYAnchor.constraint(equalTo: subEnvLabel.centerYAnchor).isActive = true
        subEnvSwitch.leftAnchor.constraint(equalTo: subEnvLabel.rightAnchor, constant: 10).isActive = true
        
        refreshButton.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        refreshButton.topAnchor.constraint(equalTo: subEnvSwitch.bottomAnchor, constant: 20).isActive = true
    }
    
    func commonInit() {
        switchButton.isOn = SettingData.share.useCustomWebView
        subEnvSwitch.isOn = SettingData.share.useTestSubEnv
        switchButton.addTarget(self, action: #selector(valueChange(_:)), for: .valueChanged)
        subEnvSwitch.addTarget(self, action: #selector(valueChange(_:)), for: .valueChanged)
        envSwitch.isOn = SettingData.share.useTestEnvForGame
        envSwitch.addTarget(self, action: #selector(valueChange(_:)), for: .valueChanged)
        refreshButton.setTitle("刷新时间间隔：\(SettingData.share.refreshTime)", for: .normal)
        refreshButton.setTitleColor(.blue, for: .normal)
        refreshButton.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
        
        /// 是否能切换忽然环境
        #if DEBUG
        subEnvSwitch.isEnabled = true
        #else
        envLabel.isHidden = true
        envSwitch.isHidden = true
        subEnvLabel.isHidden = true
        subEnvSwitch.isHidden = true
        #endif
    }
    
    @objc func valueChange(_ sender: UISwitch) {
        if sender == switchButton {
            SettingData.share.useCustomWebView = sender.isOn
            return
        }
        if sender == envSwitch {
            SettingData.share.useTestEnvForGame = sender.isOn
            return
        }
        if sender == subEnvSwitch {
            SettingData.share.useTestSubEnv = sender.isOn
            return
        }
    }
    
    @objc func buttonTap(_ btn: UIButton) {
        showRefreshTimeView()
    }
    
    func showRefreshTimeView() {
        let vc = UIAlertController(title: "刷新时间", message: "游戏间隔指定时间刷新一次", preferredStyle: .actionSheet)
        vc.addAction(.init(title: "1", style: .default, handler: { [weak self](_) in
            SettingData.share.refreshTime = 1
            self?.refreshButton.setTitle("刷新时间间隔：\(SettingData.share.refreshTime)", for: .normal)
        }))
        vc.addAction(.init(title: "2", style: .default, handler: { [weak self](_) in
            SettingData.share.refreshTime = 2
            self?.refreshButton.setTitle("刷新时间间隔：\(SettingData.share.refreshTime)", for: .normal)
        }))
        vc.addAction(.init(title: "3", style: .default, handler: { [weak self](_) in
            SettingData.share.refreshTime = 3
            self?.refreshButton.setTitle("刷新时间间隔：\(SettingData.share.refreshTime)", for: .normal)
        }))
        vc.addAction(.init(title: "4", style: .default, handler: { [weak self](_) in
            SettingData.share.refreshTime = 4
            self?.refreshButton.setTitle("刷新时间间隔：\(SettingData.share.refreshTime)", for: .normal)
        }))
        vc.addAction(.init(title: "取消", style: .cancel, handler: nil))
        present(vc, animated: true, completion: nil)
    }
}
