package cn.dong.nexus.modules.vrs.util;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.config.properties.AppProperties;
import cn.dong.nexus.core.exception.BizException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class VrsQrCodeUtil {

    public static String generate(String content) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dir = SpringUtil.getBean(AppProperties.class).getFileUploadPath() + today;
        FileUtil.mkdir(dir);

        String fileName = SecureUtil.md5(content) + ".png";
        String path = dir + "/" + fileName;
        try {
            File tempFile = File.createTempFile("logoTemp", ".png");
            ClassPathResource resource =
                    new ClassPathResource("static/kede-logo.png");
            FileCopyUtils.copy(resource.getInputStream(),
                    new FileOutputStream(tempFile));
            QrConfig qrConfig = QrConfig.create().setImg(tempFile)
                    .setHeight(500)
                    .setWidth(500);
            QrCodeUtil.generate(content, qrConfig, FileUtil.file(path));
            return today + "/" + fileName;
        } catch (Exception e) {
            log.error("二维码生成失败：{}", e.getMessage());
            throw new BizException(ApiMessage.INTERNAL_ERROR);
        }
    }
}
