package com.hp.imagebackend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import com.hp.imagebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.hp.imagebackend.model.entity.Space;
import com.hp.imagebackend.model.entity.SpaceUser;
import com.hp.imagebackend.model.enums.SpaceRoleEnum;
import com.hp.imagebackend.model.vo.SpaceUserVO;
import com.hp.imagebackend.service.SpaceUserService;
import com.hp.imagebackend.mapper.SpaceUserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author hp
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-09-22 09:23:11
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {
    // /service/impl/SpaceUserServiceImpl.java

    /**
     * 添加空间成员
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // ... 参数校验 ...
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);
        boolean result = this.save(spaceUser);
        // ... 结果校验 ...
        return spaceUser.getId();
    }

    /**
     * 校验空间成员对象
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        // ... 参数非空校验 ...
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();

        if (add) {
            // 创建时，校验 spaceId 和 userId 不能为空
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            // 校验用户和空间是否存在
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 校验角色是否合法
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    /**
     * 将查询请求对象转换为 QueryWrapper
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest queryRequest) { /* ... */ }

    /**
     * 获取单个空间成员的视图对象（含关联信息）
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) { /* ... */ }

    /**
     * 批量获取空间成员的视图对象列表（优化查询）
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList, HttpServletRequest request) {
        // ...
        // 1. 收集所有 userId 和 spaceId
        // 2. 一次性查询所有相关的 User 和 Space 对象
        // 3. 将查询结果放入 Map 中
        // 4. 遍历 spaceUserList，从 Map 中组装 VO 对象
        // ...
    }

}




