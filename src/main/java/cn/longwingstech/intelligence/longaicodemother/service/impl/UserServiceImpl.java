package cn.longwingstech.intelligence.longaicodemother.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import cn.longwingstech.intelligence.longaicodemother.constant.CodeTemplatesConstants;
import cn.longwingstech.intelligence.longaicodemother.constant.RedisConstants;
import cn.longwingstech.intelligence.longaicodemother.exception.BusinessException;
import cn.longwingstech.intelligence.longaicodemother.exception.ErrorCode;
import cn.longwingstech.intelligence.longaicodemother.model.dto.user.UserAddRequest;
import cn.longwingstech.intelligence.longaicodemother.model.dto.user.UserLoginRequest;
import cn.longwingstech.intelligence.longaicodemother.model.dto.user.UserQueryRequest;
import cn.longwingstech.intelligence.longaicodemother.model.enums.UserRoleEnum;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserLoginVO;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserVO;
import cn.longwingstech.intelligence.longaicodemother.mq.MqEnums;
import cn.longwingstech.intelligence.longaicodemother.mq.dto.EmailMqMessage;
import cn.longwingstech.intelligence.longaicodemother.utils.EmailUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.longwingstech.intelligence.longaicodemother.model.entity.User;
import cn.longwingstech.intelligence.longaicodemother.mapper.UserMapper;
import cn.longwingstech.intelligence.longaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * 用户 服务层实现。
 *
 * @author 君墨
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Resource
    private EmailUtil emailUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 管理员 添加用户
     *
     * @param userAddRequest
     * @return
     */
    @Override
    public Long add(UserAddRequest userAddRequest) {
        // 字段校验
        Assert.notNull(userAddRequest.getUserAccount(), "账号不能为空");
        // 重复判断
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount,userAddRequest.getUserAccount());
        long count = this.mapper.selectCountByQuery(queryWrapper);
        Assert.state(count == 0, "账号已存在");

        User user = BeanUtil.toBean(userAddRequest, User.class);
        // 如果密码为空
        if (Strings.isBlank(user.getUserPassword())) {
            // 密码加密
            user.setUserPassword(SaSecureUtil.sha256("12345678"));
        } else {
            // 密码加密
            user.setUserPassword(SaSecureUtil.sha256(user.getUserPassword()));
        }
        user.setUserRole(UserRoleEnum.USER.getValue());
        // 如果用户名为空
        if (Strings.isBlank(user.getUserName())) {
            user.setUserName("无名");
        }

        int result = this.mapper.insertSelective(user);
        Assert.state(result > 0, "插入数据失败");
        return user.getId();
    }

    /**
     * 用户注销
     *
     * @return
     */
    @Override
    public boolean logout() {
        StpUtil.logout();
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @return 返回未脱敏数据
     */
    @Override
    public User getLoginUser() {
        return StpUtil.getSession().getModel("user", User.class);
    }
    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录参数
     * @return 脱敏后的用户信息
     */
    @Override
    public UserLoginVO login(UserLoginRequest userLoginRequest) {
        // 字段校验
        Assert.notNull(userLoginRequest.getUserAccount(), "账号不能为空");
        Assert.notNull(userLoginRequest.getUserPassword(), "密码不能为空");
        // 账号匹配
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userLoginRequest.getUserAccount());
        User user = this.mapper.selectOneByQuery(queryWrapper);
        Assert.notNull(user, "用户名或者密码错误!");
        // 密码匹配
            //密码加密
        String sha256Password = SaSecureUtil.sha256(userLoginRequest.getUserPassword());
            // 匹配
        Assert.state(sha256Password.equals(user.getUserPassword()), "用户名或者密码错误!");
        // 登录
        StpUtil.login(user.getId());
        // 缓存
        StpUtil.getSession().set("user", user);
        // 脱敏信息
        UserLoginVO loginVO = BeanUtil.toBean(user, UserLoginVO.class);
        // 获取token
        loginVO.setToken(StpUtil.getTokenInfo().getTokenValue());
        // 返回脱敏信息
        return loginVO;
    }



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
    @Override
    public Long register(String userAccount, String userPassword, String checkPassword,String email,String code) {
        // 参数校验
        Assert.notNull(userAccount, "账号不能为空");
        Assert.notNull(userPassword, "密码不能为空");
        Assert.notNull(checkPassword, "校验密码不能为空");
        Assert.notNull(email, "邮箱不能为空");
        Assert.notNull(code, "验证码不能为空");
        // 两次密码验证
        Assert.state(userPassword.equals(checkPassword), "两次密码不一致");
        // 邮箱重复验证
        long emailCount = this.mapper.selectCountByQuery(QueryWrapper.create().eq(User::getEmail, email));
        Assert.state(emailCount == 0, "邮箱已被绑定！");
        // 账号重复验证
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long userCount = this.mapper.selectCountByQuery(queryWrapper);
        Assert.state(userCount == 0, "账号已存在！");
        // 验证码验证
        Assert.state(validateCode(email, code), "验证码错误");
        // 密码加密
        String sha256Password = SaSecureUtil.sha256(userPassword);
        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(sha256Password);
        user.setUserName("user_"+RandomUtil.randomString(6));
        user.setEmail(email);
        user.setUserRole(UserRoleEnum.USER.getValue());
        // 插入
        int result = this.mapper.insertSelective(user);
        Assert.state(result > 0, "插入数据失败");
        return user.getId();
    }

    /**
     * 用户拼接字段
     *
     * @param user
     * @return
     */
    @Override
    public QueryWrapper buildQueryWrapper(User user) {
        return QueryWrapper.create()
                .where(User::getId).eq(user.getId())
                .and(User::getUserAccount).eq(user.getUserAccount())
                .and(User::getUserName).like(user.getUserName())
                .and(User::getUserRole).eq(user.getUserRole());
    }

    /**
     * 管理员 分页查询用户
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<UserVO> pageList(UserQueryRequest userQueryRequest) {
        long pageSize = userQueryRequest.getPageSize();
        long pageNum = userQueryRequest.getPageNum();

        User user = BeanUtil.toBean(userQueryRequest, User.class);
        QueryWrapper queryWrapper = new QueryWrapper();
        if (user!=null) {
            queryWrapper = this.buildQueryWrapper(user);
        }
        return this.mapper.paginateAs(pageNum, pageSize, queryWrapper, UserVO.class);
    }

    /**
     * 脱敏
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        return BeanUtil.toBean(user, UserVO.class);
    }

    /**
     * 验证验证码
     * @param email
     * @param code
     */
    private boolean validateCode(String email, String code) {
        String redisCode = stringRedisTemplate.opsForValue().get(RedisConstants.EMAIL_CODE_KEY + email);
        stringRedisTemplate.delete(RedisConstants.EMAIL_CODE_KEY + email);
        return code.equalsIgnoreCase(redisCode);
    }

    /**
     * 获取验证码
     *
     * @param email
     * @return
     */
    @Override
    public boolean getCode(String email) {
        String code = RandomUtil.randomString(4);
        stringRedisTemplate.opsForValue().set(RedisConstants.EMAIL_CODE_KEY + email, code, 5, TimeUnit.MINUTES);
        String text = String.format(CodeTemplatesConstants.EMAIL_CODE_TEMPLATE, email,"验证、注册、绑定等相关操作", code);
        // 虚拟线程异步发送邮件
        Thread.ofVirtual().name("email-sender-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        emailUtil.sendHtmlEmail(email,"墨刻ai代码生成平台",text);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        // 消息队列异步方式
        /*EmailMqMessage emailMqMessage = EmailMqMessage.builder().email( email).title("墨刻ai代码生成平台").text(text).build();
        rabbitTemplate.convertAndSend(MqEnums.EMAIL.getExchange(), MqEnums.EMAIL.getRoutingKey(), JSONUtil.toJsonStr(emailMqMessage));*/
        return true;
    }
}
