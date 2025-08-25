package cn.longwingstech.intelligence.longaicodemother.constant;

public interface CodeTemplatesConstants {
    /**
     * 邮件验证码模板
     * @param email 邮箱
     * @param 操作类型 如注册、登录、修改密码等
     * @param code 验证码
     */
    public final String EMAIL_CODE_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>墨刻 AI 应用生成平台-您的验证码</title>
                <style>
                    body {
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        text-align: center;
                        padding: 20px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .logo {
                        max-width: 150px;
                        height: auto;
                    }
                    .content {
                        padding: 30px 20px;
                    }
                    .verification-code {
                        background: #f5f5f5;
                        padding: 15px;
                        text-align: center;
                        font-size: 24px;
                        font-weight: bold;
                        letter-spacing: 5px;
                        color: #2c3e50;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer {
                        text-align: center;
                        padding: 20px 0;
                        font-size: 12px;
                        color: #999;
                        border-top: 1px solid #eee;
                    }
                    .button {
                        display: inline-block;
                        padding: 10px 20px;
                        background-color: #3498db;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .note {
                        font-size: 14px;
                        color: #777;
                        margin-top: 30px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <img src="https://cos.longwingstech.cn/logo.png" alt="墨刻 AI 应用生成平台" class="logo">
                    <h1>墨刻 AI 应用生成平台</h1>
                </div>
            
                <div class="content">
                    <h2>您好，%s</h2>
                    <p>感谢您使用我们的服务。您正在尝试进行账户%s，请在验证码输入框中输入以下验证码：</p>
            
                    <div class="verification-code">
                        %s
                    </div>
            
                    <p>该验证码将在[5分钟]后失效，请尽快使用。</p>
            
                    <p>如果您没有请求此验证码，请忽略此邮件或联系我们的客服团队。</p>
            
                    <div class="note">
                        <p>此为系统自动发送的邮件，请勿直接回复。</p>
                    </div>
                </div>
            
                <div class="footer">
                    <p>© [2025] [墨刻 AI 应用生成平台]. 保留所有权利。</p>
                </div>
            </body>
            </html>
            """;
}
