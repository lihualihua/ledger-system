package com.digital.common.log.event;

import com.digital.model.admin.entity.SysLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * spring event log
 *
 * @author lengleng
 * @date 2023/8/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysLogEventSource extends SysLog {

	/**
	 * 参数重写成object
	 */
	private Object body;

}
