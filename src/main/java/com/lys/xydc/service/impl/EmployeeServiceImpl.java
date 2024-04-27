package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.entity.Employee;
import com.lys.xydc.mapper.EmployeeMapper;
import com.lys.xydc.service.IEmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author 陆玉升
 * date: 2023/04/2505/10
 * Description:
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements IEmployeeService {
}
