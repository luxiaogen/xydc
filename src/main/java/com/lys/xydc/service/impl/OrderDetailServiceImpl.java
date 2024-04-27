package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.entity.OrderDetail;
import com.lys.xydc.mapper.OrderDetailMapper;
import com.lys.xydc.service.IOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */
@Service
@Slf4j
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {
}
