package cn.longwingstech.intelligence.longaicodemother.ai;

import cn.longwingstech.intelligence.longaicodemother.ai.model.TitleResult;
import dev.langchain4j.service.SystemMessage;

public interface TitleGeneratorService {

    /**
     * 生成应用名称
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/application_gen_name.txt")
    TitleResult generateApplicationName(String userMessage);
}
