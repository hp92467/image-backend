package com.hp.imagebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hp.imagebackend.annotation.AuthCheck;
import com.hp.imagebackend.common.BaseResponse;
import com.hp.imagebackend.common.DeleteRequest;
import com.hp.imagebackend.common.ResultUtils;
import com.hp.imagebackend.constant.UserConstant;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import com.hp.imagebackend.model.dto.picture.*;
import com.hp.imagebackend.model.entity.Picture;
import com.hp.imagebackend.model.entity.User;
import com.hp.imagebackend.model.vo.PictureVO;
import com.hp.imagebackend.service.PictureService;
import com.hp.imagebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther:hp
 * @Date:2025/9/21
 * @Description:com.hp.imagebackend.controller
 **/
@RestController
@RequestMapping
public class PictureController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    // 通常在Service类中声明一个Cache实例
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder()
                    .initialCapacity(1024) // 初始容量
                    .maximumSize(10000L) // 最大容量，防止内存溢出
                    .expireAfterWrite(5L, TimeUnit.MINUTES) // 写入5分钟后过期
                    .build();


    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        //直接在这里处理逻辑，不到Service中（比较简单
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();

        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        //权限校验 删除人是不是当前用户而且不是管理员
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //操作数据库
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    //仅管理员可以
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);

        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        pictureService.validPicture(picture);

        //是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(picture);
    }


    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }


    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // ... 参数校验 ...

        // 1. 设计缓存Key：将查询条件转为JSON，再计算MD5，最后拼接前缀
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = "yupicture:listPictureVOByPage:" + hashKey;


        // 2. 先查缓存

        // 生成Key（去掉了项目前缀，因为本地缓存是服务内独立的）
        String cacheKey = "listPictureVOByPage:" + hashKey;

// 查缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

// ... 查询数据库 ...

// 写缓存
        Page<PictureVO> pictureVOPage = null;
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        LOCAL_CACHE.put(cacheKey, cacheValue);

        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        //String cachedValue = valueOps.get(redisKey);
        if (cachedValue != null) {
            // 缓存命中，直接返回
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 3. 缓存未命中，查数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        // 4. 将数据库查询结果写入缓存，并设置随机过期时间防止雪崩
        cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300); // 随机时间
        valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        // 5. 返回结果
        return ResultUtils.success(pictureVOPage);
    }


    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);

        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));

        //设置编辑时间
        picture.setEditTime(new Date());

        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);

        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    //审核图片接口
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    // 接口
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest req, HttpServletRequest request) {
        // ... 参数校验和获取登录用户 ...
        List<PictureVO> result = pictureService.searchPictureByColor(req.getSpaceId(), req.getPicColor(), loginUser);
        return ResultUtils.success(result);
    }
}
