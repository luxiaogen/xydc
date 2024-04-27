package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.entity.User;
import com.lys.xydc.mapper.UserMapper;
import com.lys.xydc.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * @author 陆玉升
 * date: 2023/04/2505/14
 * Description:
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
