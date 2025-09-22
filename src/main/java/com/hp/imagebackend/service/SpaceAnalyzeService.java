package com.hp.imagebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hp.imagebackend.model.dto.space.analyze.*;
import com.hp.imagebackend.model.entity.Space;
import com.hp.imagebackend.model.entity.User;
import com.hp.imagebackend.model.vo.analyze.*;

import java.util.List;

/**
* @author hp
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-09-22 06:36:22
*/
public interface SpaceAnalyzeService extends IService<Space> {


    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    void checkSpaceAuth(User loginUser, Space space);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
