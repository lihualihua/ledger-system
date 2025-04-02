package com.digital.doc.helper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.digital.doc.config.DocFileConfig;
import com.digital.doc.enums.OwnershipEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@EnableAsync
@Component
public class FileHelper {
    private static final Logger LOGGER = LogManager.getLogger(FileHelper.class);

    @Autowired
    private DocFileConfig docFileConfig;

    @Autowired
    private DictHelper dictHelper;

    private static final Map<String, String> defaultIconMap = new HashMap<>();

    static {
        defaultIconMap.put("folder", "icon-file-fill-24");
        defaultIconMap.put("doc", "icon-doc-fill-24");
        defaultIconMap.put("docx", "icon-doc-fill-24");
        defaultIconMap.put("xls", "icon-excel-fill-24");
        defaultIconMap.put("xlsx", "icon-excel-fill-24");
        defaultIconMap.put("et", "icon-excel-fill-24");
        defaultIconMap.put("ett", "icon-excel-fill-24");
        defaultIconMap.put("ppt", "icon-ppt-fill-24");
        defaultIconMap.put("pptx", "icon-ppt-fill-24");
        defaultIconMap.put("pdf", "icon-pdf-fill-24");
        defaultIconMap.put("svg", "icon-ai-fill-24");
        defaultIconMap.put("jpg", "icon-ai-fill-24");
        defaultIconMap.put("png", "icon-ai-fill-24");
        defaultIconMap.put("gif", "icon-ai-fill-24");
        defaultIconMap.put("tif", "icon-ai-fill-24");
        defaultIconMap.put("tiff", "icon-ai-fill-24");
        defaultIconMap.put("bmp", "icon-ai-fill-24");
        defaultIconMap.put("jpeg", "icon-ai-fill-24");
        defaultIconMap.put("psd", "icon-ai-fill-24");
        defaultIconMap.put("raw", "icon-ai-fill-24");
        defaultIconMap.put("eps", "icon-ai-fill-24");
        defaultIconMap.put("txt", "icon-txt-fill-24");
        defaultIconMap.put("json", "icon-nor-m-24");
        defaultIconMap.put("zip", "icon-zip-fill-24");
        defaultIconMap.put("rar", "icon-zip-fill-24");
        defaultIconMap.put("drawio", "icon-nor-m-24");
        defaultIconMap.put("dwg", "icon-nor-m-24");
    }

    @Autowired
    private UserHelper userHelper;

    /**
     * 异步删除文件
     *
     * @param filePaths
     */
    @Async
    public void asyncDeleteFile(List<String> filePaths) {
        if(CollectionUtils.isEmpty(filePaths)) {
            return;
        }
        LOGGER.info("The system starts to delete files. total counts: {}", filePaths.size());
        filePaths.forEach(FileUtil::del);
        LOGGER.info("The system ends by deleting the file.");
    }

    /**
     * 删除临时文件
     */
    public void deleteTmpFile() {
        String tmpPath = docFileConfig.getTmpPath();
        LOGGER.info("The system starts to delete file: {}", tmpPath);
        try {
            File file = new File(tmpPath);
            if (file.exists()) {
                FileUtils.cleanDirectory(file);
            }

        } catch (IOException e) {
            LOGGER.error(e);
            LOGGER.error("临时文件删除失败！");
        }
    }

    /**
     * 根据文件后缀获取图标
     *
     * @param icon
     * @param fileType
     * @return
     */
    public String getFileIcon(String icon, String fileType) {
        if (StringUtils.isNotBlank(icon)) {
            return icon;
        }
        String value = dictHelper.getStringValueMap("icon_type").get(fileType);
        if (StringUtils.isBlank(value)) {
            value = defaultIconMap.get(fileType.toLowerCase(Locale.ROOT));
        }
        return value;
    }

    /**
     * 获取上传的文件夹路径
     *
     * @param ownership
     * @return
     */
    public String getFolderPath(Integer ownership) {
        String basePath = docFileConfig.getBasePath();
        String ownershipPath = null;
        if (Objects.equals(OwnershipEnum.COMMON.getCode(), ownership)) {
            ownershipPath = "common";
        } else if (Objects.equals(OwnershipEnum.DEPT.getCode(), ownership)) {
            ownershipPath = "dept/" + userHelper.getDept().getName();
        } else if (Objects.equals(OwnershipEnum.USER.getCode(), ownership)) {
            ownershipPath = "user/" + userHelper.getUser().getUsername();
        }
        return String.join("/", basePath, ownershipPath, DateUtil.today(), "");
    }
}
