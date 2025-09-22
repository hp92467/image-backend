package com.hp.imagebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.hp.imagebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 1. 创建图片处理对象
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1); // 返回原图信息
        List<PicOperations.Rule> rules = new ArrayList<>();

        // 2. 规则1：格式转换（压缩为WebP）
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp"); // 核心处理规则
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey); // 处理后的文件保存路径
        rules.add(compressRule);

        // 3. 规则2：生成缩略图（仅在原图大于2KB时）
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 核心处理规则：缩放到128x128以内，保持比例，>代表不放大
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);
        }

        // 4. 将规则设置到上传请求中
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);

        // 5. 执行上传（会同步触发图片处理）
        return cosClient.putObject(putObjectRequest);
    }

    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

}