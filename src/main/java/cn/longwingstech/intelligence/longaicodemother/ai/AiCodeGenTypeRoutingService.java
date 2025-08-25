package cn.longwingstech.intelligence.longaicodemother.ai;

import cn.longwingstech.intelligence.longaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 *
 * @author yupi
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户需求智能选择代码生成类型
     * (枚举)
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);

    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt-new.txt")
    String routeCodeGenTypeNew(String userPrompt);


}
