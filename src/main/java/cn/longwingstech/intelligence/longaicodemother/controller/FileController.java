package cn.longwingstech.intelligence.longaicodemother.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.longwingstech.intelligence.longaicodemother.aop.RateLimiter;
import cn.longwingstech.intelligence.longaicodemother.common.BaseResponse;
import cn.longwingstech.intelligence.longaicodemother.common.ResultUtils;
import cn.longwingstech.intelligence.longaicodemother.common.constant.FileConstants;
import cn.longwingstech.intelligence.longaicodemother.manager.CosManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@SaCheckLogin
@Tag(name = "文件接口")
public class FileController {
    @Resource
    private CosManager cosManager;
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @RateLimiter(key = "addApp", count = 5)
    public BaseResponse<String> upload(@RequestPart("file") MultipartFile file) {

        String file_preFix = "upload/" + RandomUtil.randomString(16);
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
       /* {
            String fileName = System.getProperty("user.dir") + File.separatorChar
                    + FileConstants.ROOT_PATH
                    + File.separatorChar
                    + file_preFix + "." + suffix;

            try {
                FileUtil.writeBytes(file.getBytes(),fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return ResultUtils.success(FileConstants.IMAGE_PATH+file_preFix+"."+suffix);
        }*/
        try {
            String result = cosManager.uploadFile(file_preFix + "." + suffix, file.getInputStream());
            return ResultUtils.success(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
