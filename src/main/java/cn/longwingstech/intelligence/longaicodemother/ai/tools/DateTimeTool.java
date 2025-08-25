package cn.longwingstech.intelligence.longaicodemother.ai.tools;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Component;

/**
 * 时间工具类
 */
@Component
public class DateTimeTool extends BaseTool{

    @Tool("用于获取当前时间的工具，当项目创建有关于时间的数据时，应该调用该工具，获取最新时间日期")
    public String getDateTime() {
        return DateTime.now().toDateStr();
    }

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    @Override
    public String getToolName() {
        return "getDateTime";
    }

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    @Override
    public String getDisplayName() {
        return "获取最新时间";
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "[工具调用] 获取最新时间";
    }
}
