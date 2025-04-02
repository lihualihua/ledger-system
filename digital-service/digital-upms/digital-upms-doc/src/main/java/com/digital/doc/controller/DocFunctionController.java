package com.digital.doc.controller;

import com.digital.common.core.util.R;
import com.digital.doc.service.IDocFunctionService;
import com.digital.model.doc.dto.DocFunctionDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/doc/function")
@Tag(description = "doc", name = "文档功能管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DocFunctionController {

    @Autowired
    private IDocFunctionService docFunctionService;

    /**
     * 保存功能
     *
     * @param functionDto
     * @return
     */
    @PostMapping("/save")
    public R saveFunction(@Valid @RequestBody DocFunctionDto functionDto) {
        docFunctionService.saveFunction(functionDto);
        return R.ok();
    }

    /**
     * 用户绑定功能
     *
     * @param functionIds
     * @return
     */
    @PostMapping("/binding")
    public R bindingFunction(@RequestBody List<Long> functionIds) {
        docFunctionService.bindingFunction(functionIds);
        return R.ok();
    }

    /**
     * 删除功能
     *
     * @param functionId
     * @return
     */
    @PostMapping("/delete/{functionId}")
    public R deleteFunction(@PathVariable Long functionId) {
        docFunctionService.deleteFunction(functionId);
        return R.ok();
    }

    /**
     * 查找所有功能
     *
     * @return
     */
    @GetMapping("/findAllFunction")
    public R findAllFunction() {
        return R.ok(docFunctionService.findAllFunction());
    }

    /**
     * 查找用户绑定的功能
     *
     * @return
     */
    @GetMapping("/findMyFunction")
    public R findMyFunction() {
        return R.ok(docFunctionService.findMyFunction());
    }
}
