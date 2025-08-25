package cn.longwingstech.intelligence.longaicodemother.generator;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MyBatisCodeGenerator {
    private static final String[] PRO_FIX= {"chat_message"};

    public static void main(String[] args) {
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/ai_code_mother?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfig() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("cn.longwingstech.generator");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(PRO_FIX);

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        // 设置生成 mapper.xml
        globalConfig.enableMapperXml();
        // 设置生成 service
        globalConfig.enableService();
        // 设置生成 serviceImpl
        globalConfig.enableServiceImpl();
        // 设置生成 controller
        globalConfig.enableController();

        globalConfig.setLogicDeleteColumn("isDelete");

        globalConfig.getJavadocConfig()
                .setAuthor("君墨")
                .setSince("");

        return globalConfig;
    }
}