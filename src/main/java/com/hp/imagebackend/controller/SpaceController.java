package com.hp.imagebackend.controller;

import com.hp.imagebackend.annotation.AuthCheck;
import com.hp.imagebackend.common.BaseResponse;
import com.hp.imagebackend.common.ResultUtils;
import com.hp.imagebackend.constant.UserConstant;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import com.hp.imagebackend.model.dto.space.SpaceAddRequest;
import com.hp.imagebackend.model.dto.space.SpaceUpdateRequest;
import com.hp.imagebackend.model.entity.Space;
import com.hp.imagebackend.model.entity.SpaceLevel;
import com.hp.imagebackend.model.enums.SpaceLevelEnum;
import com.hp.imagebackend.service.SpaceService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Auther:hp
 * @Date:2025/9/22
 * @Description:com.hp.imagebackend.controller
 **/
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    // /controller/SpaceController.java
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 必须是管理员才能调用
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        // 根据级别自动填充限额（如果未指定）
        spaceService.fillSpaceBySpaceLevel(space);
        // 校验参数
        spaceService.validSpace(space, false);

        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // /service/impl/SpaceServiceImpl.java
    @Resource
    private TransactionTemplate transactionTemplate;



    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


}
