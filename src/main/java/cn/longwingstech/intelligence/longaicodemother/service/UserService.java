package cn.longwingstech.intelligence.longaicodemother.service;

import cn.longwingstech.intelligence.longaicodemother.model.dto.user.UserAddRequest;
import cn.longwingstech.intelligence.longaicodemother.model.dto.user.UserLoginRequest;
import cn.longwingstech.intelligence.longaicodemother.model.dto.user.UserQueryRequest;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserLoginVO;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import cn.longwingstech.intelligence.longaicodemother.model.entity.User;

/**
 * 用户 服务层。
 *
 * @author 君墨
 */
public interface UserService extends IService<User> {



    /**
     * 用户注销
     * @return
     */
    boolean logout();
    // 获取当前登录用户

    /**
     * 获取当前登录用户
     * @return 返回未脱敏数据
     */
    User getLoginUser();


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param email         用户邮箱
     * @param code          验证码
     * @return 新用户 id
     */
    Long register(String userAccount, String userPassword, String checkPassword, String email,String code);

    /**
     * 用户登录
     * @param userLoginRequest 用户登录参数
     * @return 脱敏后的用户信息
     */
    UserLoginVO login(UserLoginRequest userLoginRequest);


    /**
     * 用户拼接字段
     * @param user
     * @return
     */
    QueryWrapper buildQueryWrapper(User user);


    /**
     * 管理员 分页查询用户
     * @param userQueryRequest
     * @return
     */
    Page<UserVO> pageList(UserQueryRequest userQueryRequest);

    /**
     * 管理员 添加用户
     * @param userAddRequest
     * @return
     */
    Long add(UserAddRequest userAddRequest);

    /**
     * 脱敏
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取验证码
     * @param email
     * @return
     */
    boolean getCode(String email);
}
