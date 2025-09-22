package com.hp.imagebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.hp.imagebackend.exception.ErrorCode;
import com.hp.imagebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {  
    @Override  
    protected void validPicture(Object inputSource) {  
        String fileUrl = (String) inputSource;  
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        
    }  
  
    @Override  
    protected String getOriginFilename(Object inputSource) {  
        String fileUrl = (String) inputSource;  
        
        return FileUtil.mainName(fileUrl);
    }  
  
    @Override  
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;  
        
        HttpUtil.downloadFile(fileUrl, file);
    }  
}
