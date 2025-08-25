package cn.longwingstech.intelligence.longaicodemother.ai;

import cn.longwingstech.intelligence.longaicodemother.ai.model.PromptAuthResult;
import dev.langchain4j.service.SystemMessage;

public interface PromptAuthService {
    /**
     * 生成应用提示词验证
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/promptAuthPrompt.txt")
    PromptAuthResult promptAuth(String userMessage);
}
