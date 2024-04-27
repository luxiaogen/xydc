package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lys.xydc.common.BaseContext;
import com.lys.xydc.common.R;
import com.lys.xydc.dto.OrdersDto;
import com.lys.xydc.entity.OrderDetail;
import com.lys.xydc.entity.Orders;
import com.lys.xydc.service.IOrderDetailService;
import com.lys.xydc.service.IOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

  private final IOrdersService ordersService;

  private final IOrderDetailService orderDetailService;


  @Autowired
  public OrdersController(IOrdersService ordersService, IOrderDetailService orderDetailService) {
    this.ordersService = ordersService;
    this.orderDetailService = orderDetailService;
  }

  @PostMapping("/submit")
  public R<String> submit(@RequestBody Orders orders) {

    log.info("订单数据: {}", orders);
    ordersService.submit(orders);
    return R.success("下单成功");
  }

  /**
   * 查询订单详细
   * @param page
   * @param pageSize
   * @return
   */
  @GetMapping("/userPage")
  public R<Page> userPage(int page, int pageSize) {
    // 分页构造器
    Page<Orders> ordersPage = new Page<>(page,pageSize);

    Long userId = BaseContext.getCurrentId();
    LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
    ordersLambdaQueryWrapper.eq(Orders::getUserId, userId);
    // 分页查询过后的数据
    ordersService.page(ordersPage,ordersLambdaQueryWrapper);

    Page<OrdersDto> ordersDtoPage = new Page<>();

    // 对象拷贝  拷贝了除了records之外的数据
    BeanUtils.copyProperties(ordersPage, ordersDtoPage, "records");

    List<Orders> records = ordersPage.getRecords();


    // 处理分页的数据
    List<OrdersDto> ordersDtoList = records.stream().map(order -> {
      OrdersDto ordersDto = new OrdersDto();
      // 进行对象拷贝
      BeanUtils.copyProperties(order, ordersDto);

      // 进行填充属性
      Long orderId = order.getId();
      // 查询订单相信
      LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(OrderDetail::getOrderId, orderId);
      ordersDto.setOrderDetails(orderDetailService.list(wrapper));
      return ordersDto;
    }).collect(Collectors.toList());

    ordersDtoPage.setRecords(ordersDtoList);

    return R.success(ordersDtoPage);
  }

  /**
   * 后台订单明细列表
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page,
                      int pageSize,
                      Long number,
                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
    log.info("分页数据 page:{},pageSize{},beginTime:{},endTime:{}", page, pageSize, beginTime, endTime);

    Page<Orders> ordersPage = new Page<>(page, pageSize);

    LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(number != null, Orders::getId, number);
    wrapper.gt(beginTime != null, Orders::getOrderTime, beginTime);
    wrapper.lt(endTime != null, Orders::getOrderTime, endTime);

    ordersService.page(ordersPage, wrapper);

    return R.success(ordersPage);
  }

  /**
   * 修改派送状态
   * @param orders
   * @return
   */
  @PutMapping
  public R<String> updateStatus(@RequestBody Orders orders) {

    log.info("orders:{}", orders);

    LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(Orders::getId, orders.getId());
    wrapper.set(Orders::getStatus, orders.getStatus());

    ordersService.update(wrapper);

    return R.success("修改状态成功");
  }

}

