/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_aitek_common_lac_BaiduLac */

#ifndef _Included_com_aitek_common_lac_BaiduLac
#define _Included_com_aitek_common_lac_BaiduLac
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_aitek_common_lac_BaiduLac
 * Method:    init
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_aitek_common_lac_BaiduLac_init
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_aitek_common_lac_BaiduLac
 * Method:    from
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_aitek_common_lac_BaiduLac_from
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_aitek_common_lac_BaiduLac
 * Method:    clear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_aitek_common_lac_BaiduLac_clear
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_aitek_common_lac_BaiduLac
 * Method:    loadCustomization
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_aitek_common_lac_BaiduLac_loadCustomization
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_aitek_common_lac_BaiduLac
 * Method:    run
 * Signature: (Ljava/lang/String;Ljava/util/ArrayList;Ljava/util/ArrayList;)I
 */
JNIEXPORT jint JNICALL Java_com_aitek_common_lac_BaiduLac_run
  (JNIEnv *, jobject, jstring, jobject, jobject);

/*
 * Class:     com_aitek_common_lac_BaiduLac
 * Method:    runLabels
 * Signature: (Ljava/lang/String;Ljava/util/ArrayList;)I
 */
JNIEXPORT jint JNICALL Java_com_aitek_common_lac_BaiduLac_runLabels
  (JNIEnv *, jobject, jstring, jobject);

#ifdef __cplusplus
}
#endif
#endif
