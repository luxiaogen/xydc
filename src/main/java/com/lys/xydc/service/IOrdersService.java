package com.lys.xydc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lys.xydc.entity.Orders;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */
public interface IOrdersService extends IService<Orders> {
  /**
   * 用户下单
   * @param orders
   */
  void submit(Orders orders);
}
