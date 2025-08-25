package cn.longwingstech.intelligence.longaicodemother.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册用户
 *
 * @author 君墨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;


    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

}
