package com.hp.imagebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hp.imagebackend.model.dto.picture.*;
import com.hp.imagebackend.model.entity.Picture;
import com.hp.imagebackend.model.entity.User;
import com.hp.imagebackend.model.vo.PictureVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author hp
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-09-21 16:14:29
 */
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    @Async
    void clearPictureFile(Picture oldPicture);

    // /service/impl/PictureServiceImpl.java
    void checkPictureAuth(User loginUser, Picture picture);

    void deletePicture(long pictureId, User loginUser);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    // /service/impl/PictureServiceImpl.java
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    // /service/impl/PictureServiceImpl.java
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证原子性
    void editPictureByBatch(PictureEditByBatchRequest req, User loginUser);
}
