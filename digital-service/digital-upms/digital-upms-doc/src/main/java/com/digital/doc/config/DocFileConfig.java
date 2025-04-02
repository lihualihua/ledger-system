package com.digital.doc.config;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.digital.doc.helper.DictHelper;
import com.digital.model.doc.contains.DocContains;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class DocFileConfig {

    @Autowired
    private DictHelper dictHelper;

    @Autowired
    private Environment environment;

    /**
     * 文件路径
     */
    @Value("${doc.file.basePath:/home/digital/doc/file}")
    private String basePath;

    @Value("${doc.file.tmpPath:/home/digital/tmp}")
    private String tmpPath;

    @Value("${doc.file.downloadZipFileCount:200}")
    private int downloadZipFileCount;

    @Value("${doc.file.uploadMaxFileSize:}")
    private Long uploadMaxFileSize;

    @Value("${doc.file.downloadMaxFileSize:}")
    private Long downloadMaxFileSize;

    @Value("${doc.file.role.superAdminCode:SUPER_ADMIN}")
    private String superAdminCode;

    @Value("${doc.file.role.deptAdminCode:DEPT_ADMIN}")
    private String deptAdminCode;

    @Value("${doc.file.excelPassword:f74b2fdf6d0ce7cdac2c427cdbbc2a9b}")
    private String excelPassword;

    public String getExcelPassword() {
        String secretKey = environment.getProperty("security.encodeKey");
        if (StringUtils.isBlank(secretKey)) {
            return null;
        }
        AES aes = new AES(Mode.CFB, Padding.NoPadding,
                new SecretKeySpec(secretKey.getBytes(), "AES"),
                new IvParameterSpec(secretKey.getBytes()));
        return aes.decryptStr(excelPassword);
    }

    public String getBasePath() {
        return dictHelper.getDefaultValueIfNull("doc_config", "base_path", basePath);
    }

    public String getTmpPath() {
        return dictHelper.getDefaultValueIfNull("doc_config", "tmp_path", tmpPath);
    }

    public int getDownloadZipFileCount() {
        String value = dictHelper.getStringValue("doc_config", "download_zip_file_count");
        if (StringUtils.isBlank(value)) {
            return downloadZipFileCount;
        }
        return Integer.parseInt(value);
    }

    public String getSuperAdminCode() {
        return dictHelper.getDefaultValueIfNull("sys_role", "SUPER_ADMIN", superAdminCode);
    }

    public String getDeptAdminCode() {
        return dictHelper.getDefaultValueIfNull("sys_role", "DEPT_ADMIN", deptAdminCode);
    }

    /**
     * 获取最大允许上传的文件大小
     *
     * @return
     */
    public Long getUploadMaxFileSize() {
        return dictHelper.getLongValue("doc_config", "upload_max_file_size", DocContains.DEFAULT_UPLOAD_MAX_FILE_SIZE);
    }

    /**
     * 获取最大允许下载的文件大小
     *
     * @return
     */
    public Long getDownloadMaxFileSize() {
        return dictHelper.getLongValue("doc_config", "download_max_file_size", DocContains.DEFAULT_DOWNLOAD_MAX_FILE_SIZE);
    }

    /**
     * 通用文件类型
     */
    @Value("${doc.file.commonFileTypes:txt,zip,svg,json,rar,drawio,et,ett,tif,bmp,dwg,exe,jpeg,tiff,psd,raw,eps}")
    private List<String> commonFileTypes;

    @Value("${doc.file.excelFileTypes:xls,xlsx}")
    private List<String> excelFileTypes;

    @Value("${doc.file.docFileTypes:doc,docx}")
    private List<String> docFileTypes;

    @Value("${doc.file.pdfFileTypes:pdf}")
    private List<String> pdfFileTypes;

    @Value("${doc.file.pptFileTypes:ppt,pptx}")
    private List<String> pptFileTypes;

    @Value("${doc.file.pictureFileTypes:jpg,png,gif}")
    private List<String> pictureFileTypes;

    public List<String> getAllFileTypes() {
        List<String> fileTypes = new ArrayList<>();
        fileTypes.addAll(commonFileTypes);
        fileTypes.addAll(excelFileTypes);
        fileTypes.addAll(docFileTypes);
        fileTypes.addAll(pdfFileTypes);
        fileTypes.addAll(pptFileTypes);
        fileTypes.addAll(pictureFileTypes);
        List<String> strList = dictHelper.getStringValueList("file_types");
        if (CollectionUtils.isNotEmpty(strList)) {
            fileTypes.addAll(strList);
        }
        return fileTypes;
    }

    public List<String> getCategoryDocFileTypes() {
        List<String> fileTypes = new ArrayList<>();
        fileTypes.addAll(excelFileTypes);
        fileTypes.addAll(docFileTypes);
        fileTypes.addAll(pdfFileTypes);
        fileTypes.addAll(pptFileTypes);
        return fileTypes;
    }

    public List<String> getCategoryTypes() {
        List<String> fileTypes = getCategoryDocFileTypes();
        fileTypes.addAll(pictureFileTypes);
        return fileTypes;
    }
}
