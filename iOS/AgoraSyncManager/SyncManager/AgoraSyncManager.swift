//
//  AgoraSyncManager.swift
//  AgoraSyncManager
//
//  Created by xianing on 2021/9/12.
//

import Foundation

public class AgoraSyncManager: NSObject {
    private var proxy: ISyncManager
    
    /// init
    /// - Parameters:
    ///   - config: config of rtm
    ///   - complete: `code = 0` is success, else error
    public init(config: RtmConfig,
                complete: @escaping SuccessBlockInt) {
        let tempConfig = RtmSyncManager.Config(appId: config.appId,
                                               channelName: config.channelName)
        proxy = RtmSyncManager(config: tempConfig,
                               complete: complete)
    }
    
    /// 加入房间
    /// - Parameters:
    ///   - scene: 房间实体
    /// - Returns: `SceneReference`
    public func joinScene(scene: Scene,
                          success: SuccessBlockObj?,
                          fail: FailBlock? = nil) -> SceneReference {
        proxy.joinScene(scene: scene,
                        manager: self,
                        success: success,
                        fail: fail)
    }
    
    /// 获取房间列表
    public func getScenes(success: SuccessBlock? = nil,
                          fail: FailBlock? = nil) {
        proxy.getScenes(success: success,
                        fail: fail)
    }

    /// 删除指定的房间(在房间列表)
    /// - Parameters:
    ///   - attributesByKeys: 房间id列表
    public func deleteScenes(sceneIds: [String],
                             success: SuccessBlockVoid? = nil,
                             fail: FailBlock? = nil) {
        proxy.deleteScenes(sceneIds: sceneIds,
                           success: success,
                           fail: fail)
    }
    
    /// 获取指定属性
    /// - Parameters:
    ///   - documentRef: `Document`类型实体
    ///   - key: 键值
    func get(documentRef: DocumentReference,
             key: String? = nil,
             success: SuccessBlockObjOptional?,
             fail: FailBlock?) {
        proxy.get(documentRef: documentRef,
                  key: key,
                  success: success,
                  fail: fail)
    }
    
    /// 获取所有数据（Collection）
    /// - Parameters:
    ///   - collectionRef: `Collection`类型实体
    func get(collectionRef: CollectionReference,
             success: SuccessBlock?,
             fail: FailBlock?) {
        proxy.get(collectionRef: collectionRef,
                  success: success,
                  fail: fail)
    }
    
    /// 新增一项数据（Collection）
    /// - Parameters:
    ///   - reference: `Collection`类型实体
    ///   - data: 数据
    func add(reference: CollectionReference,
             data: [String: Any?],
             success: SuccessBlockObj?,
             fail: FailBlock?) {
        proxy.add(reference: reference,
                  data: data,
                  success: success,
                  fail: fail)
    }
    
    func update(reference: CollectionReference,
                id: String,
                data: [String : Any?],
                success: SuccessBlockVoid?,
                fail: FailBlock?) {
        proxy.update(reference: reference,
                     id: id,
                     data: data,
                     success: success,
                     fail: fail)
    }
    
    func delete(reference: CollectionReference,
                id: String,
                success: SuccessBlockVoid?,
                fail: FailBlock?) {
        proxy.delete(reference: reference,
                     id: id,
                     success: success,
                     fail: fail)
    }
    
    /// 更新或者增加数据（Document）
    /// - Parameters:
    ///   - reference: `Document`类型实体
    ///   - key: 键值
    ///   - data: 数据
    func update(reference: DocumentReference,
                key: String? = nil,
                data: [String: Any?],
                success: SuccessBlock?,
                fail: FailBlock?) {
        proxy.update(reference: reference,
                     key: key,
                     data: data,
                     success: success,
                     fail: fail)
    }
    
    /// 删除一个document
    /// - Parameters:
    ///   - documentRef: 要删除的`Document`
    func delete(documentRef: DocumentReference,
                success: SuccessBlock?,
                fail: FailBlock?) {
        proxy.delete(documentRef: documentRef,
                     success: success,
                     fail: fail)
    }
    
    /// 删除一个Collection
    /// - Parameters:
    ///   - collectionRef: 要删除的`Collection`
    func delete(collectionRef: CollectionReference,
                success: SuccessBlock?,
                fail: FailBlock?) {
        proxy.delete(collectionRef: collectionRef,
                     success: success,
                     fail: fail)
    }
    
    /// 订阅属性的更新事件
    /// - Parameters:
    ///   - reference: `Document`类型
    ///   - key: 键值
    func subscribe(reference: DocumentReference,
                   key: String? = nil,
                   onCreated: OnSubscribeBlock?,
                   onUpdated: OnSubscribeBlock?,
                   onDeleted: OnSubscribeBlock?,
                   onSubscribed: OnSubscribeBlockVoid?,
                   fail: FailBlock?) {
        return proxy.subscribe(reference: reference,
                               key: key,
                               onCreated: onCreated,
                               onUpdated: onUpdated,
                               onDeleted: onDeleted,
                               onSubscribed: onSubscribed,
                               fail: fail)
    }
    
    /// 取消订阅
    /// - Parameters:
    ///   - reference: `Document`类型
    ///   - key: 键值
    func unsubscribe(reference: DocumentReference,
                     key: String? = nil) {
        proxy.unsubscribe(reference: reference, key: key)
    }
    
    deinit {
        print("AgoraSyncManager deinit")
    }
}
