package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.entity.ShoppingCart;
import com.lys.xydc.mapper.ShoppingCartMapper;
import com.lys.xydc.service.IShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper,ShoppingCart> implements IShoppingCartService {
}
