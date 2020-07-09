// baidulac.cpp: 定义应用程序的入口点。
//

#include "baidulac.h"

#include "lac.h"
#include "com_aitek_common_lac_BaiduLac.h"
#include <string>
#include <cstring>
#include <iostream>

#ifdef __cplusplus
extern "C"
{
#endif

	 
	static jclass arrayListClass;
	static jmethodID arrayListAdd;
	static jmethodID arrayListClear;

	static jclass baiduLacClass;
	static jfieldID fidSelfPtr;

	// 返回Java类别中的self_ptr地址，用于指向创建的LAC对象
	static jfieldID _get_self_id(JNIEnv *env, jobject thisObj)
	{
		return fidSelfPtr;
	}

	// 设置self_ptr地址，指向创建的LAC对象
	static void _set_self(JNIEnv *env, jobject thisObj, LAC *self)
	{
		jlong selfPtr = *(jlong *)&self;
		env->SetLongField(thisObj, _get_self_id(env, thisObj), selfPtr);
	}

	// 返回LAC对象的指针
	static LAC *_get_self(JNIEnv *env, jobject thisObj)
	{
		jlong selfPtr = env->GetLongField(thisObj, _get_self_id(env, thisObj));
		return *(LAC **)&selfPtr;
	}

	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void*) {
		JNIEnv* env;
		if (jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
			return JNI_EVERSION;
		}
		jclass list_jcs = env->FindClass("java/util/ArrayList");
		arrayListClass = reinterpret_cast<jclass>(env->NewGlobalRef(list_jcs));
		arrayListAdd = env->GetMethodID(list_jcs, "add","(Ljava/lang/Object;)Z");
		arrayListClear = env->GetMethodID(list_jcs, "clear", "()V");

		jclass baiduLac_jcs = env->FindClass("com/aitek/common/lac/BaiduLac");
		baiduLacClass = reinterpret_cast<jclass>(env->NewGlobalRef(baiduLac_jcs));
		fidSelfPtr = env->GetFieldID(baiduLac_jcs, "self_ptr", "J");
		return JNI_VERSION_1_6;
	}

	JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* jvm, void*) {
		JNIEnv* env;
		if (jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
			// Everything is hosed if this happens. We can't even throw an
			// exception back into Java, since to do that we'd need a valid JNIEnv.
			return;
		}
		env->DeleteGlobalRef(arrayListClass);
		env->DeleteGlobalRef(baiduLacClass);
	}

	/*
   * Class:     LAC
   * Method:    init
   * Signature: (Ljava/lang/String;Ljava/lang/String;)V
   */
	JNIEXPORT void JNICALL Java_com_aitek_common_lac_BaiduLac_init(JNIEnv *env, jobject thisObj, jstring model_dir)
	{

		LAC *self = new LAC(env->GetStringUTFChars(model_dir, 0));
		_set_self(env, thisObj, self);
	}

	JNIEXPORT void JNICALL Java_com_aitek_common_lac_BaiduLac_from(JNIEnv *env, jobject thisObj, jlong ptr) {
		LAC *self = (LAC*)ptr;
		if (self) {
			_set_self(env, thisObj, new LAC(*self));
		}
	}

	JNIEXPORT void JNICALL Java_com_aitek_common_lac_BaiduLac_clear(JNIEnv * env, jclass clz, jlong self) {
		if (self) {
			delete (LAC *)self;
		}
	}
	/*
   * Class:     LAC
   * Method:    load_customization
   * Signature: (Ljava/lang/String;)Jint
   */
	JNIEXPORT jint JNICALL Java_com_aitek_common_lac_BaiduLac_loadCustomization
	(JNIEnv *env, jobject thisObj, jstring dict_path)
	{
		LAC *self = _get_self(env, thisObj);
		return self->load_customization(env->GetStringUTFChars(dict_path, 0));
	}

	/*
	 * Class:     LAC
	 * Method:    run
	 * Signature: (Ljava/lang/String;Ljava/lang/ArrayList;Ljava/lang/ArrayList)Jint
	 */
	JNIEXPORT jint JNICALL Java_com_aitek_common_lac_BaiduLac_run
	(JNIEnv *env, jobject thisObj, jstring sentence, jobject words, jobject tags)
	{	
		env->CallVoidMethod(words, arrayListClear);
		env->CallVoidMethod(tags, arrayListClear);

		LAC *self = _get_self(env, thisObj);
		std::string input = env->GetStringUTFChars(sentence, 0);
		auto result = self->run(input);

		for (size_t i = 0; i < result.size(); i++)
		{
			env->CallBooleanMethod(words, arrayListAdd, env->NewStringUTF(result[i].word.c_str()));
			env->CallBooleanMethod(tags, arrayListAdd, env->NewStringUTF(result[i].tag.c_str()));
		}

		return 0;

	}

	JNIEXPORT jint JNICALL Java_com_aitek_common_lac_BaiduLac_runLabels
	(JNIEnv * env, jobject thisObj, jstring sentence, jobject labels) {
		env->CallVoidMethod(labels, arrayListClear);
		LAC *self = _get_self(env, thisObj);
		std::string input = env->GetStringUTFChars(sentence, 0);
		self->runlabels(input, [labels, env](const std::string& item) {
			env->CallBooleanMethod(labels, arrayListAdd, env->NewStringUTF(item.c_str()));
		});
		return 0;
	}

#ifdef __cplusplus
}
#endif
