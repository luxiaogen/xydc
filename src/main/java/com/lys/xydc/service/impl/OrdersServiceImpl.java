package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.common.BaseContext;
import com.lys.xydc.common.CustomServiceException;
import com.lys.xydc.entity.*;
import com.lys.xydc.mapper.OrdersMapper;
import com.lys.xydc.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

  private final IUserService userService;

  private final IAddressBookService addressBookService;

  private final IShoppingCartService shoppingCartService;

  private final IOrderDetailService orderDetailService;

  @Autowired
  public OrdersServiceImpl(IUserService userService, IAddressBookService addressBookService, IShoppingCartService shoppingCartService, IOrderDetailService orderDetailService) {
    this.userService = userService;
    this.addressBookService = addressBookService;
    this.shoppingCartService = shoppingCartService;
    this.orderDetailService = orderDetailService;
  }


  /**
   * 用户下单功能
   * @param orders
  @Override
   */
  @Transactional
  public void submit(Orders orders) {

    // 获取当前用户id
    Long userId = BaseContext.getCurrentId();

    // 查询购物车数据
    LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
    shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
    List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);

    if (shoppingCartList == null) {
      // 如果购物车数据为空   抛出异常  防小人
      throw new CustomServiceException("购物车为空,不能下单");
    }

    // ===== 准备需要填充的数据 =====
    // 查询用户信息
    User user = userService.getById(userId);
    // 查询地址信息
    AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

    // 判断地址是否为空  防小人
    if (addressBook == null) {
      throw new CustomServiceException("地址为空,不能下单");
    }

    // 生成订单号
    long orderId = IdWorker.getId();

    AtomicInteger amount = new AtomicInteger(0); // 多线程时也可以使用 JUC 知识  暂时没有学到

    // 填充订单详细表表数据
    List<OrderDetail> orderDetailList = shoppingCartList.stream().map(shoppingCart -> {
      OrderDetail orderDetail = new OrderDetail();
      orderDetail.setOrderId(orderId);
      orderDetail.setNumber(shoppingCart.getNumber());
      orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
      orderDetail.setDishId(shoppingCart.getDishId());
      orderDetail.setSetmealId(shoppingCart.getSetmealId());
      orderDetail.setName(shoppingCart.getName());
      orderDetail.setImage(shoppingCart.getImage());
      orderDetail.setAmount(shoppingCart.getAmount());
      // 填充商品价格
      amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
      return orderDetail;
    }).collect(Collectors.toList());

    // 填充订单表数据
    orders.setId(orderId);
    orders.setOrderTime(LocalDateTime.now());
    orders.setCheckoutTime(LocalDateTime.now());
    orders.setStatus(2); // 派送中状态
    orders.setAmount(new BigDecimal(amount.get()));//总金额
    orders.setUserId(userId);
    orders.setNumber(String.valueOf(orderId));
    if (user.getName() == null)
      orders.setUserName("demo");
    else
      orders.setUserName(user.getName());
    orders.setConsignee(addressBook.getConsignee());
    orders.setPhone(addressBook.getPhone());
    orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
            + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
            + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
            + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

    // 向订单表插入一条数据
    this.save(orders);

    // 向订单明细表插入多条数据
    orderDetailService.saveBatch(orderDetailList);

    // 清空购物车
    shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
  }
}
