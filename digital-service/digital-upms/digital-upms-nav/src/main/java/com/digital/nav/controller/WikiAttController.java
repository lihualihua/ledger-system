package com.digital.nav.controller;

import com.digital.common.core.util.R;
import com.digital.model.nav.dto.WikiAttDto;
import com.digital.nav.service.IWikiAttService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * wiki附件
 */
@RestController
@RequestMapping("/nav/wiki/att")
@Tag(description = "nav", name = "wikiAttr")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class WikiAttController {

    @Autowired
    private IWikiAttService wikiAttService;

    /**
     * 保存wiki附件
     *
     * @param wikiAttDto
     * @return
     */
    @PostMapping("/save")
    public R saveWikiAtt(@Valid @RequestBody WikiAttDto wikiAttDto) {
        wikiAttService.saveWikiAtt(wikiAttDto);
        return R.ok("保存成功!");
    }

    /**
     * 删除wiki附件
     *
     * @return
     */
    @PostMapping("/delete")
    public R deleteWikiAtt(@RequestBody WikiAttDto wikiAttDto) {
        wikiAttService.deleteWikiAtt(wikiAttDto);
        return R.ok("删除成功!");
    }


}
