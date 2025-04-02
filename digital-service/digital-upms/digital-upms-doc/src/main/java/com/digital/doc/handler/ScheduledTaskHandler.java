package com.digital.doc.handler;

import com.digital.common.core.handler.IStartupAfterHandler;
import com.digital.doc.helper.FileHelper;
import com.digital.doc.service.IDocShareService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledTaskHandler implements IStartupAfterHandler {
    private static final Logger LOGGER = LogManager.getLogger(ScheduledTaskHandler.class);

    @Autowired
    private FileHelper fileHelper;

    @Autowired
    private IDocShareService docShareService;

    /**
     * 定时删除临时文件，凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void schedulerDeleteTmpFile() {
        LOGGER.info("The system starts the scheduled task of deleting temporary files.");
        fileHelper.deleteTmpFile();
        LOGGER.info("The scheduled task for deleting temporary files is complete.");
    }

    /**
     * 定时删除过期分享数据，凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void schedulerDeleteExpiredShare() {
        LOGGER.info("The system starts the scheduled task of deleting share records.");
        docShareService.deleteExpiredShare();
        LOGGER.info("The scheduled task for deleting share records is complete.");
    }

    @Override
    public void execute(ApplicationStartedEvent event) {
        schedulerDeleteTmpFile();
        schedulerDeleteExpiredShare();
    }
}
