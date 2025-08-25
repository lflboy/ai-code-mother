package cn.longwingstech.intelligence.longaicodemother.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.longwingstech.intelligence.longaicodemother.aop.RateLimiter;
import cn.longwingstech.intelligence.longaicodemother.common.BaseResponse;
import cn.longwingstech.intelligence.longaicodemother.common.DeleteRequest;
import cn.longwingstech.intelligence.longaicodemother.common.ResultUtils;
import cn.longwingstech.intelligence.longaicodemother.model.dto.user.*;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserLoginVO;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import cn.longwingstech.intelligence.longaicodemother.model.entity.User;
import cn.longwingstech.intelligence.longaicodemother.service.UserService;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author 君墨
 */
@RestController
@RequestMapping("/user")
@SaCheckLogin
@Tag(name = "用户接口")
public class UserController {

    @Resource
    private UserService userService;



    /**
     * 用户注销
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销")
    public BaseResponse<Boolean> logout() {
        boolean result = userService.logout();
        return ResultUtils.success( result);
    }

    /**
     * 获取当前登录用户
     * @return 当前登录用户
     */
    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户")
    public BaseResponse<UserLoginVO> getLoginUser() {
        User user = userService.getLoginUser();
        // 返回脱敏信息
        return ResultUtils.success(BeanUtil.toBean(user, UserLoginVO.class));
    }

    //注册
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @SaIgnore
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        String code = userRegisterRequest.getCode();
        Long result = userService.register(userAccount, userPassword, checkPassword,email,code);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @SaIgnore
    public BaseResponse<UserLoginVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        UserLoginVO result = userService.login(userLoginRequest);
        return ResultUtils.success(result);
    }
    /**
     * 保存用户。
     *
     * @param userAddRequest 用户
     * @return BaseResponse<Long> 用户id
     */
    @SaCheckRole("admin")
    @PostMapping("/add")
    @Operation(summary = "添加用户")
    public BaseResponse<Long> save(@RequestBody UserAddRequest userAddRequest) {
        Long result= userService.add(userAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @SaCheckRole("admin")
    @DeleteMapping("remove/{id}")
    @Operation(summary = "删除用户")
    public BaseResponse<Boolean> remove(@PathVariable Long id) {
        // 不能删除自己
        long userId = StpUtil.getLoginIdAsLong();
        Assert.state(id != userId, "不能删除自己");
        // 踢下线
        StpUtil.logout(id);
        return ResultUtils.success(userService.removeById(id));
    }

    /**
     * 根据主键更新用户。
     *
     * @param userUpdateRequest 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @SaCheckRole("admin")
    @PostMapping("updateById")
    @Operation(summary = "管理员更新用户")
    public BaseResponse<Boolean> updateById(@RequestBody UserUpdateRequest userUpdateRequest) {
        User user = BeanUtil.toBean(userUpdateRequest, User.class);
        if (user.getUserPassword()!=null && !user.getUserPassword().isEmpty()) {
            // 密码加密
            user.setUserPassword(SaSecureUtil.sha256(user.getUserPassword()));
            // 如果是修改密码 直接强制下线
            StpUtil.logout(user.getId());
            // 更新信息
            boolean result = userService.updateById(user);
            // 直接响应
            return ResultUtils.success(result);
        }
        boolean result = userService.updateById(user);
        // 更新缓存
        user = userService.getById(user.getId());
        SaSession session = StpUtil.getSessionByLoginId(user.getId(), false);
        if (session != null) {
            session.set("user", user);
        }
        return ResultUtils.success( result);
    }

    @PostMapping("update")
    @Operation(summary = "用户更新个人信息")
    public BaseResponse<Boolean> update(@RequestBody UserUpdateRequest userUpdateRequest) {
        long userid = StpUtil.getLoginIdAsLong();
        User user = BeanUtil.toBean(userUpdateRequest, User.class);
        user.setId(userid);
        if (user.getUserPassword()!=null && !user.getUserPassword().isEmpty()) {
            // 密码加密
            user.setUserPassword(SaSecureUtil.sha256(user.getUserPassword()));
            // 如果是修改密码 直接强制下线
            StpUtil.logout(user.getId());
            // 更新信息
            boolean result = userService.updateById(user);
            // 直接响应
            return ResultUtils.success(result);
        }
        // 更新信息
        boolean result = userService.updateById(user);
        // 刷新缓存
        SaSession session = StpUtil.getSessionByLoginId(userid, false);
        // 先查询缓存是否存在，如果存在则更新
        if (session != null) {
            user = userService.getById(userid);
            session.set("user", user);
        }
        return ResultUtils.success(result);
    }

    @PostMapping("delete")
    @SaCheckRole("admin")
    @Operation(summary = "删除用户")
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        long id = deleteRequest.getId();
        Assert.isTrue(id != StpUtil.getLoginIdAsLong(), "不能删除自己");
        // 踢下线
        StpUtil.logout(id);
        return ResultUtils.success(userService.removeById(id));
    }
    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @SaCheckRole("admin")
    @GetMapping("get")
    @Operation(summary = "根据ID获取用户信息")
    public BaseResponse<UserVO> getInfo(Long id) {
        User user = userService.getById(id);
        UserVO userVO = BeanUtil.toBean(user, UserVO.class);
        return ResultUtils.success(userVO);
    }
    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        User user = userService.getById(id);
        return ResultUtils.success(userService.getUserVO(user));
    }
    /**
     * 分页查询用户。
     *
     * @param userQueryRequest 分页对象
     * @return 分页对象
     */
    @SaCheckRole("admin")
    @PostMapping("/list/page/vo")
    @Operation(summary = "分页查询用户")
    public BaseResponse<Page<UserVO>> page(@RequestBody UserQueryRequest userQueryRequest) {
        Page<UserVO> result = userService.pageList(userQueryRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取验证码
     * @param email 邮箱
     */
    @GetMapping("/get/captcha")
    @Operation(summary = "获取验证码")
    @RateLimiter(key = "getCaptcha",count = 2)
    @SaIgnore
    public BaseResponse<Boolean> getCaptcha(@RequestParam String email) {
        boolean result = userService.getCode(email);
        return ResultUtils.success(result);
    }
}
