package com.digital.model.doc.contains;

public interface DocContains {

    /**
     * 默认顶级文件夹id
     */
    Long ROOT_PARENT_ID = 0L;

    /**
     * 上传/下载限制
     */
    Long DEFAULT_UPLOAD_MAX_FILE_SIZE = 500 * 1024 * 1024L;

    Long DEFAULT_DOWNLOAD_MAX_FILE_SIZE = 500 * 1024 * 1024L;

    /**
     * 文件下载缓冲区
     */
    Long LARGE_FILE_SIZE = 100 * 1024 * 1024L;

    int BUFFER_SIZE = 8192;

    int LARGE_FILE_BUFFER_SIZE = 1024 * 1024;

    /**
     * 文件夹标识
     */
    Integer FOLDER_FLAG = 1;

    /**
     * 日期格式化
     */
    String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 全部
     */
    String CATEGORY_ALL = "all";
    /**
     * 文档
     */
    String CATEGORY_DOCUMENT = "document";
    /**
     * 文件夹
     */
    String CATEGORY_FOLDER = "folder";
    /**
     * 图片
     */
    String CATEGORY_PICTURE = "picture";

    String CATEGORY_DOCUMENT_ALL = "all";
    /**
     * doc文件
     */
    String CATEGORY_DOCUMENT_DOC = "doc";
    /**
     * excel文件
     */
    String CATEGORY_DOCUMENT_XLS = "xls";
    /**
     * ppt
     */
    String CATEGORY_DOCUMENT_PPT = "ppt";
    /**
     * pdf
     */
    String CATEGORY_DOCUMENT_PDF = "pdf";
    /**
     * other
     */
    String CATEGORY_DOCUMENT_OTHER = "other";
}
