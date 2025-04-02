package com.digital.doc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.doc.entity.DocCollectEntity;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2019-06-18 17:18:42
 */
public interface IDocCollectService extends IService<DocCollectEntity> {

	void collect(Long fileId);

	void removeByFileId(Long fileId);

	void removeByCollectId(Long collectId);

}
