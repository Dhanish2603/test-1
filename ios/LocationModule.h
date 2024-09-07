
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNLocationModuleSpec.h"

@interface LocationModule : NSObject <NativeLocationModuleSpec>
#else
#import <React/RCTBridgeModule.h>

@interface LocationModule : NSObject <RCTBridgeModule>
#endif

@end
