package com.hp.imagebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hp.imagebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.hp.imagebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hp.imagebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author hp
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-09-22 09:23:11
*/
public interface SpaceUserService extends IService<SpaceUser> {

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest queryRequest);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList, HttpServletRequest request);
}
