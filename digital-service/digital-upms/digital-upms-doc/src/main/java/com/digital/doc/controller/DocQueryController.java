package com.digital.doc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.common.core.util.R;
import com.digital.doc.service.IDocHistoryService;
import com.digital.doc.service.IDocSearchService;
import com.digital.model.doc.dto.DocFileDto;
import com.digital.model.doc.dto.DocHistoryDto;
import com.digital.model.doc.dto.DocSearchDto;
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

import java.util.List;

/**
 * 文档查询
 */
@RestController
@RequestMapping("/doc/query")
@Tag(description = "doc", name = "文档查找")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DocQueryController extends BaseController {

    @Autowired
    private IDocSearchService docSearchService;

    @Autowired
    private IDocHistoryService docHistoryService;

    @PostMapping(value = "childFiles/{current}/{size}")
    public R findChildFiles(@RequestBody DocFileDto docFileDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docQueryService.findChildFiles(docFileDto, page));
    }

    @PostMapping(value = "findAllChildFiles/{current}/{size}")
    public R findAllChildFiles(@RequestBody DocFileDto docFileDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docQueryService.findAllChildFiles(docFileDto, page));
    }

    @PostMapping(value = "collectFiles/{current}/{size}")
    public R findCollectFiles(@RequestBody DocFileDto docFileDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docQueryService.findCollectFiles(docFileDto, page));
    }

    @PostMapping(value = "/searchFiles/fileList/{current}/{size}")
    public R findSearchFiles(@RequestBody DocSearchDto docSearchDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(docSearchService.findSearchFiles(docSearchDto, page));
    }

    @PostMapping(value = "/add/searchFiles/content")
    public R createHistory(@RequestBody DocHistoryDto docHistoryDto){
        return R.ok(docHistoryService.createHistory(docHistoryDto));
    }

    @PostMapping(value = "/searchFiles/historyList/{current}/{size}")
    public R findHistoryList(@RequestBody DocHistoryDto docHistoryDto, @ModelAttribute("page") Page page) {
        return R.ok(docHistoryService.findHistoryList(docHistoryDto, page));
    }

    @PostMapping(value = "/findFileSizeSum")
    public R findFileSizeSum(@RequestBody List<DocFileDto> docFileDto) {
        return R.ok(docQueryService.findFileSizeSum(docFileDto));
    }

    @GetMapping("/findFileCount")
    public R findFileCount() {
        return R.ok(docQueryService.findFileCount());
    }

    @GetMapping("/findFileDetailInfo/{fileId}")
    public R findFileDetailInfo(@PathVariable Long fileId) {
        return R.ok(docQueryService.findFileDetailInfo(fileId, false));
    }
}
