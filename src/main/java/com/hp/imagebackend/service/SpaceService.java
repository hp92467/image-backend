package com.hp.imagebackend.service;

import com.hp.imagebackend.model.dto.space.SpaceAddRequest;
import com.hp.imagebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hp.imagebackend.model.entity.User;

/**
* @author hp
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-09-22 06:36:22
*/
public interface SpaceService extends IService<Space> {

    // /service/impl/SpaceServiceImpl.java
    void validSpace(Space space, boolean add);

    // /service/impl/SpaceServiceImpl.java
    void fillSpaceBySpaceLevel(Space space);

    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);
}
