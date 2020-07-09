/* Copyright (c) 2020 Baidu, Inc. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

#ifndef BAIDU_LAC_LAC_H
#define BAIDU_LAC_LAC_H

#include <unordered_map>
#include <memory>
#include <string>
#include <vector>
#include <functional>
#include "paddle_inference_api.h"

/* �������� */
enum CODE_TYPE
{
	CODE_GB18030 = 0,
	CODE_UTF8 = 1,
};

/* ģ������Ľṹ */
struct OutputItem
{
	std::string word;   // �ִʽ��
	std::string tag;    // ��������
};


#ifndef LAC_CLASS
#define LAC_CLASS

// ǰ������, ȥ��ͷ�ļ�����
class Customization;

class LAC
{
public:
	/* ��ʼ����װ��ģ�ͺʹʵ� */
	LAC(LAC &lac);      // 
	LAC(const std::string& model_path, CODE_TYPE type = CODE_UTF8);

	/* ���ó��� */
	std::vector<OutputItem> run(const std::string &query);                           // ����query
	std::vector<std::vector<OutputItem>> run(const std::vector<std::string> &query); // ����query

	/* װ���û��ʵ� */
	int load_customization(const std::string& filename);

	void runlabels(const std::string &query, std::function<void(const std::string)> function);

private:
	/* ���ַ�������תΪTensor */
	int feed_data(const std::vector<std::string> &querys);

	/* ��ģ�ͱ�ǩ���ת��Ϊģ�������ʽ */
	int parse_targets(
		const std::vector<std::string> &tag_ids,
		const std::vector<std::string> &words,
		std::vector<OutputItem> &result);

	// �������ͣ���Ҫͬʱ�޸��ֵ��ļ��ı���
	CODE_TYPE _codetype;

	// �м����
	std::vector<std::string> _seq_words;
	std::vector<std::vector<std::string>> _seq_words_batch;
	std::vector<std::vector<size_t>> _lod;
	std::vector<std::string> _labels;
	std::vector<OutputItem> _results;
	std::vector<std::vector<OutputItem>> _results_batch;

	// ����ת���ʵ�
	std::shared_ptr<std::unordered_map<int64_t, std::string>> _id2label_dict;
	std::shared_ptr<std::unordered_map<std::string, std::string>> _q2b_dict;
	std::shared_ptr<std::unordered_map<std::string, int64_t>> _word2id_dict;
	int64_t _oov_id;

	// paddle���ݽṹ
	paddle::PaddlePlace _place;                             //PaddlePlace::kGPU��KCPU
	std::unique_ptr<paddle::PaddlePredictor> _predictor;    // Ԥ����
	std::unique_ptr<paddle::ZeroCopyTensor>;  // ����ռ�
	std::unique_ptr<paddle::ZeroCopyTensor> _output_tensor; // ����ռ�


	// �˹���Ԥ�ʵ�
	std::shared_ptr<Customization> custom;
};
#endif  // LAC_CLASS

#endif  // BAIDU_LAC_LAC_H