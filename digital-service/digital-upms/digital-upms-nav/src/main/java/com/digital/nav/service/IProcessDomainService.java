package com.digital.nav.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digital.model.nav.dto.ProcessDomainDto;
import com.digital.model.nav.entity.ProcessDomainEntity;
import com.digital.model.nav.vo.FolderInfoVO;

import java.util.List;

public interface IProcessDomainService  extends IService<ProcessDomainEntity> {
    /**
     * 创建流程域
     *
     * @param processDomainDto
     */
    void create(ProcessDomainDto processDomainDto);

    /**
     * 修改流程域
     *
     * @param processDomainDto
     */
    void update(ProcessDomainDto processDomainDto);

    /**
     * 删除流程域
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 查找单个流程域信息
     *
     * @param processDomainDto
     * @return
     */
    ProcessDomainEntity findProcessDomain(ProcessDomainDto processDomainDto);

    /**
     * 查找所有的流程域
     *
     * @return
     */
    List<ProcessDomainEntity> findProcessDomainList(ProcessDomainDto processDomainDto);

    /**
     * 查找文件夹存储目录
     *
     * @return
     */
    List<FolderInfoVO> findStorageDirList();

    /**
     * 根据ID查找流程域
     *
     * @param id
     * @return
     */
    ProcessDomainEntity findById(Long id);
}
