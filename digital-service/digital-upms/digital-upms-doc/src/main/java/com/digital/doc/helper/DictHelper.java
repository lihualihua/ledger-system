package com.digital.doc.helper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.digital.admin.service.SysDictItemService;
import com.digital.common.core.constant.CacheConstants;
import com.digital.model.admin.entity.SysDictItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DictHelper {

    @Autowired
    private SysDictItemService sysDictItemService;

    @Autowired
    private DictHelper self;

    /**
     * 根据名称获取value
     *
     * @param type
     * @param name
     * @return
     */
    @Cacheable(value = CacheConstants.DICT_DETAILS, key = "#type + '::'+ #name")
    public String getStringValue(String type, String name) {
        SysDictItem dictItem = sysDictItemService.getOne(Wrappers.<SysDictItem>lambdaQuery().
                eq(SysDictItem::getDictType, type).eq(SysDictItem::getLabel, name));
        if (Objects.isNull(dictItem)) {
            return null;
        }
        Integer delFlag = dictItem.getDelFlag();
        if (delFlag == 1) {
            return null;
        }
        return dictItem.getItemValue();
    }

    public Long getLongValue(String type, String name, Long defaultValue) {
        String value = self.getStringValue(type, name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    public int getIntValue(String type, String name, int defaultValue) {
        String value = self.getStringValue(type, name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * 根据名称获取value, 如果为空取默认值
     *
     * @param type
     * @param name
     * @param defaultValue
     * @return
     */
    public String getDefaultValueIfNull(String type, String name, String defaultValue) {
        String value = self.getStringValue(type, name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 根据类型获取子项
     *
     * @param type
     * @return
     */
    @Cacheable(value = CacheConstants.DICT_DETAILS, key = "'getStringValueList::' + #type")
    public List<String> getStringValueList(String type) {
        List<SysDictItem> sysDictItems = sysDictItemService.list(Wrappers.<SysDictItem>lambdaQuery().eq(
                SysDictItem::getDictType, type).orderByAsc(SysDictItem::getSortOrder));
        if (CollectionUtils.isEmpty(sysDictItems)) {
            return Collections.emptyList();
        }
        sysDictItems = sysDictItems.stream().filter(item -> 0 == item.getDelFlag()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sysDictItems)) {
            return Collections.emptyList();
        }
        List<String> strList = new ArrayList<>();
        sysDictItems.forEach(item -> {
            String value = item.getItemValue();
            if (StringUtils.isNotBlank(value)) {
                strList.addAll(Arrays.asList(value.split(",")));
            }
        });
        return strList;
    }

    /**
     * 获取字典配置的多项
     *
     * @param type
     * @return
     */
    @Cacheable(value = CacheConstants.DICT_DETAILS, key = "'getStringValueMap::' + #type")
    public Map<String, String> getStringValueMap(String type) {
        List<SysDictItem> sysDictItems = sysDictItemService.list(Wrappers.<SysDictItem>lambdaQuery().eq(SysDictItem::getDictType, type));
        if (CollectionUtils.isEmpty(sysDictItems)) {
            return Collections.emptyMap();
        }
        sysDictItems = sysDictItems.stream().filter(item -> 0 == item.getDelFlag()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sysDictItems)) {
            return Collections.emptyMap();
        }
        return sysDictItems.stream().collect(Collectors.toMap(SysDictItem::getLabel, SysDictItem::getItemValue));
    }
}
