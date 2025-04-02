package com.digital.doc.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.common.core.util.R;
import com.digital.doc.enums.ValidFlagEnum;
import com.digital.doc.service.IDocShareService;
import com.digital.model.doc.contains.DocContains;
import com.digital.model.doc.dto.DocShareDto;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/doc/share")
@Tag(description = "doc", name = "文档分享")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DocShareController extends BaseController {

    @Autowired
    private IDocShareService docShareService;

    /**
     * 保存文件分享
     *
     * @param docShareDto
     * @return
     */
    @PostMapping("/save")
    public R saveShare(@Valid @RequestBody DocShareDto docShareDto, HttpServletRequest request) {
        docShareDto.getFileIds().forEach(this::checkDocFile);
        if (ValidFlagEnum.CUSTOM.getCode() == docShareDto.getValidFlag()) {
            if (Objects.isNull(docShareDto.getValidDate())) {
                return R.failed("有效期不能为空!");
            }
            if (DateUtil.compare(DateUtil.parse(docShareDto.getValidDate()), new Date(), DocContains.DATE_FORMAT) < 0) {
                return R.failed("有效期不能早于当前日期!");
            }
        }
        StringBuffer url = request.getRequestURL();
        String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append(
                request.getServletContext().getContextPath()).toString();
        return R.ok(docShareService.saveShare(docShareDto, tempContextUrl));
    }

    /**
     * 根据分享记录查找分享的文件
     *
     * @param shareDto
     * @return
     */
    @PostMapping("/findShareFiles")
    public R findFilesByMd5Value(@RequestBody DocShareDto shareDto) {
        return R.ok(docShareService.findFilesByMd5Value(shareDto));
    }

    /**
     * 取消文件分享
     *
     * @param shareId
     * @return
     */
    @GetMapping("/cancel/{shareId}")
    public R cancelShare(@PathVariable("shareId") Long shareId) {
        return R.ok(docShareService.deleteShare(shareId));
    }

    /**
     * 查找我分享的记录
     *
     * @param shareDto
     * @param page
     * @return
     */
    @PostMapping(value = "findMyShare/{current}/{size}")
    public R findMyShare(@RequestBody(required = false) DocShareDto shareDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docShareService.findMyShare(shareDto, page));
    }

    /**
     * 查找别人分享给我的记录
     *
     * @param shareDto
     * @param page
     * @return
     */
    @PostMapping(value = "findShareWithMe/{current}/{size}")
    public R findShareWithMe(@RequestBody(required = false) DocShareDto shareDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docShareService.findShareWithMe(shareDto, page));
    }

    /**
     * 查找分享明细
     *
     * @param shareId
     * @return
     */
    @GetMapping("/findShareDetail/{shareId}")
    public R findShareDetail(@PathVariable Long shareId) {
        return R.ok(docShareService.findShareDetail(shareId));
    }
}
