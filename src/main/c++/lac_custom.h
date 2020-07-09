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

#ifndef BAIDU_LAC_CUSTOM_H
#define BAIDU_LAC_CUSTOM_H

#include<vector>
#include<string>
#include <memory>

#include "lac_util.h"
#include "ahocorasick.h"

/* ��Ԥ��item */
struct customization_term {
	std::vector<std::string> tags;
	std::vector<int> split;
	customization_term(const std::vector<std::string>& tags,
		const std::vector<int>& split) :
		tags(tags),
		split(split) {}
};

/* ��Ԥʹ�õ��� */
class Customization {
private:
	// ��¼ÿ��item�ı�ǩ�ͷִ���Ϣ
	std::vector<customization_term> _customization_dic;

	// AC�Զ�������item�Ĳ�ѯ
	AhoCorasick _ac_dict;

	// AC�Զ�����ѯ���ؽ��
	std::vector<std::pair<int, int>> _ac_res;

	// �����ַ�������ʱ����
	std::vector<std::string> line_vector;
public:
	Customization(const std::string &customization_dic_path) {
		load_dict(customization_dic_path);
	}

	/* ���û��ʵ��н���װ�� */
	RVAL load_dict(const std::string &customization_dic_path);

	/* ��lac��Ԥ�������и�Ԥ */
	RVAL parse_customization(const std::vector<std::string> &seq_chars, std::vector<std::string> &tag_ids);
};

#endif  //BAIDU_LAC_CUSTOM_H