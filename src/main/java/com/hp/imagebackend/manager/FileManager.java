package com.hp.imagebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.hp.imagebackend.config.CosClientConfig;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import com.hp.imagebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {

        validPicture(multipartFile);

        String uuid = RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {

            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);

            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }
    }

    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {


        validPicture(fileUrl);

        String uuid = RandomUtil.randomString(16);

        String originFilename = FileUtil.mainName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {

            file = File.createTempFile(uploadPath, null);

            HttpUtil.downloadFile(fileUrl, file);


        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }
        return null;
    }

    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");

        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }


    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }

        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {

            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }


        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");


        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();

            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }

            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {

                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }

            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


}