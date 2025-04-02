package com.digital.nav.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.common.core.util.R;
import com.digital.model.nav.dto.WikiDto;
import com.digital.nav.service.IWikiService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/nav/wiki")
@Tag(description = "nav", name = "wiki")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class WikiController {

    @Autowired
    private IWikiService wikiService;

    /**
     * 保存wiki
     *
     * @param wikiDto
     * @return
     */
    @PostMapping("/save")
    public R saveWiki(@Valid @RequestBody WikiDto wikiDto) {
        wikiService.saveWiki(wikiDto);
        return R.ok();
    }

    /**
     * 保存/发布wiki内容
     *
     * @param wikiDto
     * @return
     */
    @PostMapping("/saveContent")
    public R saveWikiContent(@RequestBody WikiDto wikiDto) {
        wikiService.saveWikiContent(wikiDto);
        return R.ok();
    }

    /**
     * 删除wiki
     *
     * @param wikiId
     * @return
     */
    @GetMapping("/delete/{wikiId}")
    public R deleteWiki(@PathVariable Long wikiId) {
        wikiService.deleteWiki(wikiId);
        return R.ok();
    }

    /**
     * 查找wiki基本信息
     *
     * @param wikiDto
     * @return
     */
    @PostMapping("/findWiki")
    public R findWiki(@RequestBody WikiDto wikiDto) {
        return R.ok(wikiService.findWiki(wikiDto));
    }

    /**
     * 查找wiki内容
     *
     * @param wikiDto
     * @return
     */
    @PostMapping("/findWikiContent")
    public R findWikiContent(@RequestBody WikiDto wikiDto) {
        return R.ok(wikiService.findWikiContent(wikiDto));
    }

    /**
     * 查找当前wiki的所有版本号
     *
     * @param wikiCode
     * @return
     */
    @GetMapping("/findWikiVersionList/{wikiCode}")
    public R findWikiVersionList(@PathVariable String wikiCode, @RequestParam(required = false) Integer status) {
        return R.ok(wikiService.findWikiVersionList(wikiCode, status));
    }

    /**
     * 查找流程域下所有wiki
     *
     * @param domainId
     * @return
     */
    @GetMapping("/findWikiList/{domainId}/{current}/{size}")
    public R findWikiList(@PathVariable("domainId") Long domainId, @ModelAttribute("page") PageDTO page) {
        return R.ok(wikiService.findWikiList(domainId, page));
    }

    /**
     * 查找wiki正式版本列表
     *
     * @param wiki
     * @param page
     * @return
     */
    @PostMapping("/findWikiReleaseList/{current}/{size}")
    public R findWikiReleaseList(@RequestBody WikiDto wiki, @ModelAttribute("page") PageDTO page) {
        return R.ok(wikiService.findWikiReleaseList(wiki, page));
    }

}
