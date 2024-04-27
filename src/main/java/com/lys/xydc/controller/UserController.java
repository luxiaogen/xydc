package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lys.xydc.common.R;
import com.lys.xydc.entity.User;
import com.lys.xydc.service.IUserService;
import com.lys.xydc.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 陆玉升
 * date: 2023/04/2505/14
 * Description:
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {


  private final IUserService userService;

  private final RedisTemplate redisTemplate;

  @Autowired
  public UserController(IUserService userService,RedisTemplate redisTemplate) {
    this.userService = userService;
    this.redisTemplate = redisTemplate;
  }

  @PostMapping("/sendMsg")
  public R<String> sendMsg(@RequestBody User user, HttpSession session) {

    // 获取手机号
    String phone = user.getPhone();

    // 判断手机号是否为空null...
    if (StringUtils.isNotBlank(phone)) {
      // 生成随机的4位验证码
      String code = ValidateCodeUtils.generateValidateCode(4).toString();
      log.info("code={}", code);

      // 调用阿里云提供的短信服务API完成发送短信
      // SMSUtils.sendMessage("校园外卖", "", phone, code);

      // 需要将生成的验证码存入到session中
      // session.setAttribute(phone,code);

      // 将生成的验证码缓存到Redis中，并设置有效期为5分钟
      redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

      return R.success("手机验证码发送成功");
    }

    return R.error("短信发送失败");
  }

  /**
   * 移动端用户登录
   * @param userInfo
   * @param session
   * @return
   */
  @PostMapping("/login")
  public R<User> login(@RequestBody Map userInfo, HttpSession session) {

    log.info(userInfo.toString());

    // 获取手机号
    String phone = userInfo.get("phone").toString();

    // 获取验证码
    String code = userInfo.get("code").toString();

    // 从Session中获取保存的验证码
    // String codeInSession = session.getAttribute(phone).toString();

    // 从Redis中获取缓存的验证码
    String codeInRedis = (String) redisTemplate.opsForValue().get(phone);


    // 进行验证码的比对(页面提交的验证码和Session中把保存的验证码比对)
    if (StringUtils.isNotBlank(codeInRedis) && codeInRedis.equals(code)) {
      // 如果能够比对成功,说明登录成功
      LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(User::getPhone,phone);
      User user = userService.getOne(wrapper);
      // 判断当前手机号对应的用户是否是新用户，如果是新用户就自动完成注册
      if (user == null) {
        user = new User();
        // 随机设置一个名字
        String name = UUID.randomUUID().toString().substring(0, 4);
        user.setName(name);
        user.setPhone(phone);
        user.setStatus(1);
        userService.save(user); // 保存用户
      }

      session.setAttribute("user", user.getId());

      // 用户登录成功，删除Redis中缓存的验证码
      redisTemplate.delete(phone);
      return R.success(user);
    }



    return R.error("登录失败");
  }



}
