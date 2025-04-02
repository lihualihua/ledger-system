package com.digital.nav.controller;

import com.digital.common.core.exception.CheckedException;
import com.digital.common.core.util.R;
import com.digital.model.nav.dto.ProcessDomainDto;
import com.digital.nav.service.IProcessDomainService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/nav/process/domain")
@Tag(description = "nav", name = "流程域管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ProcessDomainController {

    @Autowired
    private IProcessDomainService processDomainService;

    /**
     * 保存流程域
     *
     * @param processDomainDto
     * @return
     */
    @PostMapping("/create")
    public R create(@Valid @RequestBody ProcessDomainDto processDomainDto) {
        processDomainService.create(processDomainDto);
        return  R.ok();
    }

    /**
     * 修改流程域
     *
     * @param processDomainDto
     * @return
     */
    @PostMapping("/update")
    public R update(@Valid @RequestBody ProcessDomainDto processDomainDto) {
        if (Objects.isNull(processDomainDto.getId())) {
            throw new CheckedException("流程域编号不能为空!");
        }
        processDomainService.update(processDomainDto);
        return  R.ok();
    }

    /**
     * 删除流程域
     *
     * @param id
     * @return
     */
    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        processDomainService.delete(id);
        return  R.ok();
    }

    /**
     * 根据ID查找流程域信息
     *
     * @param id
     */
    @GetMapping("/findById/{id}")
    public R findById(@PathVariable Long id) {
        return R.ok(processDomainService.findById(id));
    }

    /**
     * 查找所有流程域
     *
     * @return
     */
    @GetMapping("/findAllProcessDomain")
    public R findAllProcessDomain(@ModelAttribute ProcessDomainDto processDomainDto) {
        return  R.ok(processDomainService.findProcessDomainList(processDomainDto));
    }

    /**
     * 查找模板目录列表
     *
     * @return
     */
    @GetMapping("/findStorageDirList")
    public R findStorageDirList() {
        return R.ok(processDomainService.findStorageDirList());
    }
}
