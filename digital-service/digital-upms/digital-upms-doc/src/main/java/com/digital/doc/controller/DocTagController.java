package com.digital.doc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.common.core.util.R;
import com.digital.doc.service.IDocTagService;
import com.digital.model.doc.dto.DocFileTagDto;
import com.digital.model.doc.dto.DocTagDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/doc/tag")
@Tag(description = "doc", name = "文档分享")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DocTagController extends BaseController {

    @Autowired
    private IDocTagService docTagService;

    /**
     * 新增标签
     *
     * @param docTagDto
     * @return
     */
    @PostMapping("/save")
    public R save(@Valid @RequestBody DocTagDto docTagDto) {
        return R.ok(docTagService.saveTag(docTagDto));
    }

    /**
     * 删除标签
     *
     * @param tagId
     * @return
     */
    @GetMapping("/delete/{id}")
    public R deleteTag(@PathVariable("id") Long tagId) {
        return R.ok(docTagService.deleteTag(tagId));
    }

    /**
     * 批量操作标签
     *
     * @param fileTagDto
     * @return
     */
    @PostMapping("/saveTags2Files")
    public R saveTags2Files(@Valid @RequestBody DocFileTagDto fileTagDto) {
        docTagService.saveTags2Files(fileTagDto);
        return R.ok();
    }

    /**
     * 查找我的标签
     *
     * @return
     */
    @GetMapping("/findMyTags")
    public R findMyTags() {
        return R.ok(docTagService.findMyTags());
    }

    /**
     * 分页查找公开标签
     *
     * @return
     */
    @PostMapping("/findPublicTags/{current}/{size}")
    public R findPublicTags(@RequestBody DocTagDto docTagDto, @ModelAttribute("page") Page page) {
        return R.ok(docTagService.findPublicTags(docTagDto, page));
    }

    /**
     * 根据标签编号分页查找文件
     *
     * @param tagDto
     * @param page
     * @return
     */
    @PostMapping("/findFilesByTagId/{current}/{size}")
    public R findFilesByTagId(@RequestBody DocTagDto tagDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docTagService.findFilesByTag(tagDto, page));
    }

    /**
     * 根据文件ID查找标签
     *
     * @param fileId
     * @return
     */
    @GetMapping("/findTagByFileId/{fileId}")
    public R findTagByFileId(@PathVariable("fileId") Long fileId) {
        return R.ok(docTagService.findTagByFileId(fileId));
    }
}
