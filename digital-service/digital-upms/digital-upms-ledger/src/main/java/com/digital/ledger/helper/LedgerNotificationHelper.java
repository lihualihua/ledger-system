package com.digital.ledger.helper;

import com.digital.ledger.service.INotificationService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LedgerNotificationHelper {
    private static final Logger LOGGER = LogManager.getLogger(LedgerNotificationHelper.class);

    @Autowired
    private INotificationService notificationService;

    public void sendNotification(String tempName, List<String> cellValueList, boolean isNew) {
        if (CollectionUtils.isEmpty(cellValueList)) {
            LOGGER.error("台账通知的用户列表为空，台账名称：{}，是否新通知:{}", tempName, isNew);
            return;
        }
        cellValueList = cellValueList.stream().distinct().collect(Collectors.toList());
        String title = null;
        String content = null;
        if (isNew) {
            title = "台账【" + tempName + "】 填写";
            content = "您有新的台账需要填写，台账名称：" + tempName + "， 请点击查看!";
        } else {
            title = "台账【" + tempName + "】 催办";
            content = "您还有台账未完成，台账名称：" + tempName + "， 请尽快填写!";
        }

        notificationService.sendNotification(title, content, "ledgerEdit", cellValueList);
    }
}
