package com.digital.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.digital.common.core.util.R;
import com.digital.ledger.service.IImportLedgerService;
import com.digital.ledger.service.ILedgerOperateService;
import com.digital.ledger.service.LedgerService;
import com.digital.ledger.service.LedgerTemplateService;
import com.digital.ledger.util.BaseUtils;
import com.digital.model.ledger.dto.LedgerDataDto;
import com.digital.model.ledger.dto.TemplateDto;
import com.digital.model.ledger.entity.LedgerEntity;
import com.digital.model.ledger.dto.LedgerTemplateDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ledger")
@Tag(description = "ledger", name = "台账管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class LedgerController extends BaseUtils {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerTemplateService templateService;

    @Autowired
    private IImportLedgerService importLedgerService;

    @Autowired
    private ILedgerOperateService operateService;

    /**
     * 导入（分发）
     *
     * @param file 文件
     * @return R
     */
    @PostMapping(value = "/upload/{templateId}")
    public R upload(@RequestPart("file") MultipartFile file, @PathVariable Long templateId) {
        checkFile(file);
        importLedgerService.readExcel(file, templateId);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 导入模板库
     *
     * @param file 文件
     * @param systemFlag 是否系统模版0:否，1：是
     * @return R
     */
    @PostMapping(value = "/template/upload/{systemFlag}")
    public R templateUpload(@RequestPart("file") MultipartFile file,
                          @PathVariable Integer systemFlag) {
        checkFile(file);
        if(systemFlag == 1) {
            templateService.checkSysTemplateCount();
        }
        templateService.readTemplateExcel(file, systemFlag);
        return R.ok(Boolean.TRUE, "导入成功！");
    }

    /**
     * 删除模板（个人创建）
     *
     * @param id 模板id
     * @return R
     */
    @PostMapping("/template/{id}")
    public R removeById(@PathVariable Long id) {
        templateService.removeTemplateById(id);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 查询系统默认模版
     *
     * @return R
     */
    @GetMapping("/findSysTemplateList")
    public R findSysTemplateList() {
        return R.ok(templateService.findSysTemplateList());
    }

    /**
     * 分页查询模板库(自定义模版)
     *
     * @param templateDto 入参
     * @param page 分页
     * @return R
     */
    @PostMapping("/findMyTemplates/{current}/{size}")
    public R findMyTemplates(@RequestBody LedgerTemplateDto templateDto, @ModelAttribute("page") PageDTO page) {
        return R.ok(templateService.findMyTemplates(templateDto, page));
    }

    /**
     * 查找我分配的台账列表
     *
     * @param page
     * @return
     */
    @GetMapping("/findMyTemplates/{current}/{size}")
    public R findMyLedgerList(@ModelAttribute("page") PageDTO page) {
        return R.ok(ledgerService.findMyLedgerList(page));
    }

    /**
     * 查找分配给我的台账列表
     *
     * @param page
     * @return
     */
    @GetMapping("/findTemplatesWithMe/{current}/{size}")
    public R findLedgerListWithMe(@ModelAttribute("page") PageDTO page) {
        return R.ok(ledgerService.findLedgerListWithMe(page));
    }

    /**
     * 查找台账数据
     *
     * @param tempId
     * @return
     */
    @GetMapping("/findTemplateData/{tempId}")
    public R findLedgerData(@PathVariable String tempId) {
        return R.ok(ledgerService.findLedgerData(tempId));
    }

    /**
     * 归档
     *
     * @param tempId
     * @return
     */
    @GetMapping("/archive/{tempId}")
    public R archive(@PathVariable String tempId, @RequestParam(required = false) boolean ignoreCheck) {
        LedgerEntity ledgerEntity = findCreatorLedger(tempId);
        operateService.archive(ledgerEntity, ignoreCheck);
        return R.ok("归档成功!");
    }

    /**
     * 撤销
     *
     * @param tempId
     * @return
     */
    @GetMapping("/revoke/{tempId}")
    public R revoke(@PathVariable String tempId) {
        LedgerEntity ledgerEntity = findCreatorLedger(tempId);
        operateService.revoke(ledgerEntity);
        return R.ok("撤销成功!");
    }

    /**
     * 删除
     *
     * @param tempId
     * @return
     */
    @GetMapping("/delete/{tempId}")
    public R delete(@PathVariable String tempId) {
        LedgerEntity ledgerEntity = findCreatorLedger(tempId);
        operateService.delete(ledgerEntity);
        return R.ok("撤销成功!");
    }

    /**
     * 催办
     *
     * @param tempId
     * @return
     */
    @GetMapping("/urge/{tempId}")
    public R urge(@PathVariable String tempId) {
        LedgerEntity ledgerEntity = findCreatorLedger(tempId);
        operateService.urge(ledgerEntity);
        return R.ok("催办成功!");
    }

    /**
     * 下载台账模板
     *
     * @param type
     * @param response
     */
    @GetMapping("/downloadLedgerTemplate/{type}/{templateId}")
    public void downloadLedgerTemplate(@PathVariable("type") Integer type, @PathVariable("templateId") Long templateId, HttpServletResponse response) {
        operateService.downloadLedgerTemplate(templateId, type, response);
    }

    /**
     * 下载台账excel数据
     *
     * @param template
     * @param response
     */
    @PostMapping("/downloadLedgerExcelData")
    public void downloadLedgerExcel(@Valid @RequestBody TemplateDto template, HttpServletResponse response) {
        operateService.downloadLedgerExcel(template, response);
    }

    /**
     * 编辑人导入
     *
     * @param file 文件
     * @param tempId 模版id
     * @return R
     */
    @PostMapping(value = "editor/upload")
    public R editorUpload(@RequestPart("file") MultipartFile file, @RequestParam(value = "tempId") String tempId) {
        checkFile(file);
        importLedgerService.editorUpload(file, tempId);
        return R.ok(Boolean.TRUE);
    }

    /**
     * 保存台账数据
     *
     * @param dataList
     * @return
     */
    @PostMapping("/save/{tempId}")
    public R saveLedgerData(@RequestBody List<LedgerDataDto> dataList, @PathVariable("tempId") String tempId) {
        LedgerEntity ledgerEntity = findEditorLedger(tempId);
        ledgerService.saveLedgerData(dataList, ledgerEntity);
        return R.ok("保存成功!");
    }
}
