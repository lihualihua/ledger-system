package com.digital.common.security.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.digital.model.admin.dto.UserInfo;
import com.digital.model.admin.entity.SysDept;
import com.digital.model.admin.entity.SysUser;
import com.digital.common.core.constant.CommonConstants;
import com.digital.common.core.constant.SecurityConstants;
import com.digital.common.core.util.R;
import com.digital.common.core.util.RetOps;
import org.springframework.core.Ordered;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lengleng
 * @date 2021/12/21
 */
public interface PigUserDetailsService extends UserDetailsService, Ordered {

	/**
	 * 是否支持此客户端校验
	 * @param clientId 目标客户端
	 * @return true/false
	 */
	default boolean support(String clientId, String grantType) {
		return true;
	}

	/**
	 * 排序值 默认取最大的
	 * @return 排序值
	 */
	default int getOrder() {
		return 0;
	}

	/**
	 * 构建userdetails
	 * @param result 用户信息
	 * @return UserDetails
	 */
	default UserDetails getUserDetails(R<UserInfo> result) {
		UserInfo info = RetOps.of(result).getData().orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

		Set<String> dbAuthsSet = new HashSet<>();

		if (ArrayUtil.isNotEmpty(info.getRoles())) {
			// 获取角色
			info.getRoleIds().forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role));
			// 获取资源
			dbAuthsSet.addAll(Arrays.asList(info.getPermissions()));
		}

		Collection<GrantedAuthority> authorities = AuthorityUtils
			.createAuthorityList(dbAuthsSet.toArray(new String[0]));
		SysUser user = info.getSysUser();
		SysDept dept = info.getDept();

		// 构造security用户
		return new PigUser(user.getUserId(), dept, user.getUsername(),
				SecurityConstants.BCRYPT + user.getPassword(), user.getPhone(), true, true, true,
				StrUtil.equals(user.getLockFlag(), CommonConstants.STATUS_NORMAL), authorities, user.getDisplayName(), info.getRoles());
	}

	/**
	 * 通过用户实体查询
	 * @param pigUser user
	 * @return
	 */
	default UserDetails loadUserByUser(PigUser pigUser) {
		return this.loadUserByUsername(pigUser.getUsername());
	}

}
