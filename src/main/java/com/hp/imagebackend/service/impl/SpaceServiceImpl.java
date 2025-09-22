package com.hp.imagebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import com.hp.imagebackend.model.dto.space.SpaceAddRequest;
import com.hp.imagebackend.model.entity.Space;
import com.hp.imagebackend.model.entity.User;
import com.hp.imagebackend.model.enums.SpaceLevelEnum;
import com.hp.imagebackend.service.SpaceService;
import com.hp.imagebackend.mapper.SpaceMapper;
import com.hp.imagebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author hp
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-09-22 06:36:22
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;
    // /service/impl/SpaceServiceImpl.java
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        // 创建时，名称和级别不能为空
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }

        // 通用校验
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    // /service/impl/SpaceServiceImpl.java
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            // 如果 maxSize 未设置，则使用枚举中的默认值
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            // 如果 maxCount 未设置，则使用枚举中的默认值
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    // /service/impl/SpaceServiceImpl.java
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);

        // 填充默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }

        // 根据级别填充限额
        this.fillSpaceBySpaceLevel(space);

        // 校验参数
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);

        // 权限校验：非管理员只能创建普通版空间
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }

        // 使用本地锁+编程式事务，确保同一用户并发创建空间时只有一个成功
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 事务内：检查用户是否已存在空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");

                // 事务内：保存新空间
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

                return space.getId();
            });

            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

}




