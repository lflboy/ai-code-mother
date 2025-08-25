package cn.longwingstech.intelligence.longaicodemother.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.longwingstech.intelligence.longaicodemother.common.constant.FileConstants;
import cn.longwingstech.intelligence.longaicodemother.langgraph4j.model.ImageResource;
import cn.longwingstech.intelligence.longaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import cn.longwingstech.intelligence.longaicodemother.manager.CosManager;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class LogoTool extends BaseTool{
    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model:wan2.2-t2i-flash}")
    private String imageModel;

    @Resource
    private CosManager cosManager;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 构建 Logo 设计提示词
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(logoPrompt)
                    .size("512*512")
                    .n(1) // 生成 1 张足够，因为 AI 不知道哪张最好
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for (Map<String, String> imageResult : results) {
                    String imageUrl = imageResult.get("url");
                    if (StrUtil.isNotBlank(imageUrl)) {
                        logoList.add(ImageResource.builder()
                                .category(ImageCategoryEnum.LOGO)
                                .description(description)
                                .url(imageUrl)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        String url = logoList.getLast().getUrl();
        // 下载图片
        log.info("下载图片: {}", url);
        // 创建临时保存目录
        String name = RandomUtil.randomString(16) + ".png";
        String fileName = "gen" + File.separatorChar + "logo" + File.separatorChar + name;
        String tempDir = System.getProperty("user.dir") + File.separatorChar + "tmp" +File.separatorChar+ fileName;
        try {
            FileUtil.mkdir(System.getProperty("user.dir") + File.separatorChar + "tmp");
            long result = HttpUtil.downloadFile(url, new File(tempDir));
            if (result > 0) {
                log.info("下载图片成功: {}", tempDir);
                // 上传图片到cos
                log.info("上传图片到cos: {}", tempDir);
                String key = "gen/logo/"+name;
                String decUrl = cosManager.uploadFile(key, new File(tempDir));
                log.info("上传图片到cos成功: {}", decUrl);
                logoList.getLast().setUrl(decUrl);
            }
            return logoList;
        } finally {
            // 删除临时文件
            log.info("删除临时文件: {}", tempDir);
            boolean res = FileUtil.del(tempDir);
            if (!res) {
                log.error("删除临时文件失败: {}", tempDir);
            } else {
                log.info("删除临时文件成功: {}", tempDir);
            }

        }
    }

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    @Override
    public String getToolName() {
        return "generateLogos";
    }

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    @Override
    public String getDisplayName() {
        return "Logo生成";
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用]Logo生成";
    }
}
