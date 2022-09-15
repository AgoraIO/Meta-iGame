//
//  GameNetworkManager.m
//  AgoraGameEngine
//
//  Created by ZYP on 2022/7/21.
//

#import "GameNetworkManager.h"

#define MGP_GAME_LOGIN_URL @"https://txy.jyanedu.com/sud/login"

@implementation GameNetworkManager

+ (void)getCode:(NSString *)userId
        success:(void (^)(NSString *code))success
           fail:(void(^)(NSError *error))fail {
    NSAssert(userId.length != 0, @"用户ID不能为空");
    NSDictionary *dicParam = @{@"user_id": userId};
    [self postHttpRequestWithURL:MGP_GAME_LOGIN_URL
                           param:dicParam
                         success:^(NSDictionary *rootDict) {
        NSDictionary *dic = [rootDict objectForKey:@"data"];
        NSString *code = [dic objectForKey:@"code"];
        success(code);
    } failure:^(NSError *error) {
        NSLog(@"login game server error:%@", error.debugDescription);
        if (fail) {
            fail(error);
        }
    }];
}

+ (void)postHttpRequestWithURL:(NSString *)api
                         param:(NSDictionary *)param
                       success:(void (^)(NSDictionary *_Nonnull))success
                       failure:(void (^)(id _Nonnull))failure {
    NSURL *url = [NSURL URLWithString:api];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = @"POST";
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    if (param) {
        NSData *bodyData = [NSJSONSerialization dataWithJSONObject:param options:NSJSONWritingPrettyPrinted error:nil];
        request.HTTPBody = bodyData;
    }
    
    NSURLSession *session = [NSURLSession sharedSession];
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData *_Nullable data, NSURLResponse *_Nullable response, NSError *_Nullable error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (error) {
                if (failure) {
                    failure(error);
                }
                return;
            }
            NSError *error;
            NSMutableDictionary *responseObject = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
            if (error) {
                if (failure) {
                    failure(error);
                }
                return;
            }
            if (success) {
                success(responseObject);
            }
        });
    }];
    
    [dataTask resume];
}

@end
