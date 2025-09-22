package com.hp.imagebackend.controller;

import com.hp.imagebackend.annotation.AuthCheck;
import com.hp.imagebackend.constant.UserConstant;
import com.hp.imagebackend.exception.BusinessException;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.manager.CosManager;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther:hp
 * @Date:2025/9/21
 * @Description:com.hp.imagebackend.controller
 **/
@RestController
@RequestMapping
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();

            byte[] bytes = IOUtils.toByteArray(cosObjectInput);

            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);

            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
