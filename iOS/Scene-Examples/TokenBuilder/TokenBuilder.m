//
//  TokenBuilder.m
//  AgoraEducation
//
//  Created by SRS on 2021/1/13.
//  Copyright © 2021 yangmoumou. All rights reserved.
//

#import "TokenBuilder.h"
#import "RtmTokenTool.h"

#define NoNullString(x) ([x isKindOfClass:NSString.class] ? x : @"")
#define NoNullDictionary(x) ([x isKindOfClass:NSDictionary.class] ? x : @{})
#define NoNullArray(x) ([x isKindOfClass:NSArray.class] ? x : @[])

@implementation TokenBuilder
// 本地生成token。用于本地快速演示使用， 我们建议你使用服务器生成token( buildToken:success:failure:)
+ (NSString *)buildToken:(NSString *)appID
          appCertificate:(NSString *)appCertificate
                userUuid:(NSString *)userUuid {
    return [RtmTokenTool token:appID
                appCertificate:appCertificate
                           uid:userUuid];
}


@end
