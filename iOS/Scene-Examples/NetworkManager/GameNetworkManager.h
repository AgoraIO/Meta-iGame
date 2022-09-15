//
//  GameNetworkManager.h
//  AgoraGameEngine
//
//  Created by ZYP on 2022/7/21.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// 主要用于game code 更新，这一块需要客户的服务器完成
@interface GameNetworkManager : NSObject

/// game code 更新
+ (void)getCode:(NSString *)userId
        success:(void (^)(NSString *code))success
           fail:(void(^)(NSError *error))fail;

@end

NS_ASSUME_NONNULL_END
