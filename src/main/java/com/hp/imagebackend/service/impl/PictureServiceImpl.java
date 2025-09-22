package com.hp.imagebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import com.hp.imagebackend.manager.CosManager;
import com.hp.imagebackend.manager.FileManager;
import com.hp.imagebackend.manager.upload.FilePictureUpload;
import com.hp.imagebackend.manager.upload.PictureUploadTemplate;
import com.hp.imagebackend.manager.upload.UrlPictureUpload;
import com.hp.imagebackend.mapper.PictureMapper;
import com.hp.imagebackend.model.dto.file.UploadPictureResult;
import com.hp.imagebackend.model.dto.picture.*;
import com.hp.imagebackend.model.entity.Picture;
import com.hp.imagebackend.model.entity.Space;
import com.hp.imagebackend.model.entity.User;
import com.hp.imagebackend.model.enums.PictureReviewStatusEnum;
import com.hp.imagebackend.model.vo.PictureVO;
import com.hp.imagebackend.model.vo.UserVO;
import com.hp.imagebackend.service.PictureService;
import com.hp.imagebackend.service.SpaceService;
import com.hp.imagebackend.service.UserService;
import com.hp.imagebackend.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hp
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-09-21 16:14:29
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;


    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private SpaceService spaceService;

    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {

        // /service/impl/PictureServiceImpl.java - uploadPicture 方法内
// ...
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

// 校验空间权限
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

            // 只有空间创建者才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
// ...

        if (inputSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }


        String uploadPathPrefix = String.format("public/%s", loginUser.getId());

        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);


        return null;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        if (StrUtil.isNotBlank(searchText)) {

            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);

        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {

        PictureVO pictureVO = PictureVO.objToVo(picture);

        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    //分页获取图片封装（多个
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

        //对象列表转封装对象
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());

        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    //数据校验
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);

        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    //审核图片
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验参数
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //图片存在？
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        //新旧图片状态一样？
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }

        //操作数据库
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    //通用的状态改变方法，用户更改图片输出则通过变待审核，管理员不用
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {

            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {

            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();

        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多 30 条");

        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }

            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }

            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }


    @Resource
    private CosManager cosManager;

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {

        //判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();

        //不止一条不清理
        if (count > 1) {
            return;
        }

        cosManager.deleteObject(oldPicture.getUrl());

        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }


    // /service/impl/PictureServiceImpl.java
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库图片：创建者或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间图片：只有创建者可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        checkPictureAuth(loginUser, oldPicture);

        boolean result = this.removeById(pictureId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);

        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));

        picture.setEditTime(new Date());

        this.validPicture(picture);

        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        checkPictureAuth(loginUser, oldPicture);

        this.fillReviewParams(picture, loginUser);

        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    // /service/impl/PictureServiceImpl.java
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // ... 权限校验 ...

        // 1. 查询出指定空间内所有设置了主色调的图片
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }

        // 2. 将目标颜色字符串转换为Color对象
        Color targetColor = Color.decode(picColor);

        // 3. 在内存中对图片列表进行排序
        List<Picture> sortedPictures = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    // 计算每张图片的主色调与目标颜色的相似度
                    // 返回负值以实现降序排序（最相似的在前）
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, Color.decode(picture.getPicColor()));
                }))
                .limit(12) // 返回最相似的12张
                .collect(Collectors.toList());

        // 4. 转换为VO并返回
        return sortedPictures.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    }

    @Resource
    private PictureEditByBatchRequest pictureEditByBatchRequest;
    // /service/impl/PictureServiceImpl.java
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest req, User loginUser) {
        // ... 参数校验和权限校验 ...

        // 1. 查询出需要修改的图片实体（只查询必要字段以优化性能）
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId, Picture::getName) // 增加name字段
                .eq(Picture::getSpaceId, req.getSpaceId())
                .in(Picture::getId, req.getPictureIdList())
                .list();
        if (pictureList.isEmpty()) {
            return;
        }

        // 2. 处理重命名规则
        fillPictureWithNameRule(pictureList, req.getNameRule());

        // 3. 遍历列表，在内存中修改实体对象的属性
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(req.getCategory())) {
                picture.setCategory(req.getCategory());
            }
            if (CollUtil.isNotEmpty(req.getTags())) {
                picture.setTags(JSONUtil.toJsonStr(req.getTags()));
            }
        });

        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);

        // 4. 调用 Mybatis-Plus 的批量更新方法，一次性提交到数据库
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 根据命名规则填充图片名称
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                // 将 {序号} 占位符替换为递增的数字
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }


}




