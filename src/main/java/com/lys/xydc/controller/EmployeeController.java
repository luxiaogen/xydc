package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lys.xydc.common.R;
import com.lys.xydc.entity.Employee;
import com.lys.xydc.service.IEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author 陆玉升
 * date: 2023/04/2505/10
 * Description:
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

  @Autowired
  private IEmployeeService employeeService;

  /**
   * 员工登录方法
   * @return
   */
  @PostMapping("/login")
  public R<Employee> login(HttpSession session, @RequestBody Employee employee) {
    // 1.将页面提交的密码password进行md5加密处理
    String password = employee.getPassword();
    password = DigestUtils.md5DigestAsHex(password.getBytes());
    // 2.根据页面提交的用户名username查询数据库
    LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
    lqw.eq(Employee::getUsername, employee.getUsername());
    Employee emp = employeeService.getOne(lqw);

    // 3.如果没有查询到则返回登录失败结果
    if (emp == null) {
      return R.error("登录失败");
    }

    // 4.密码比对,如果不一致则返回登录失败结果
    if (!password.equals(emp.getPassword())) {
      return R.error("登录失败");
    }

    // 5.查看员工状态,如果为已禁用状态,则返回员工已禁用结果
    if (emp.getStatus() == 0) {
      return R.error("账号已禁用");
    }

    // 6.登录成功,将员工id存放Session并返回登录成功结果
    session.setAttribute("employee", emp.getId());
    return R.success(emp);
  }

  /**
   * 员工退出
   * @return
   */
  @PostMapping("/logout")
  public R<String> logout(HttpSession session) {
    // 清理Session中保存的当前登录员工的id
    session.removeAttribute("employee");
    return R.success("退出成功");
  }

  @PostMapping
  public R<String> save(HttpSession session, @RequestBody Employee employee) {
    // 设置初始密码123456,需要进行md5加密处理
    employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

    /*employee.setCreateTime(LocalDateTime.now());
    employee.setUpdateTime(LocalDateTime.now());*/

    // 获取当前登录用户的id
    // Long empId = (Long) session.getAttribute("employee");

    /*employee.setCreateUser(empId);
    employee.setUpdateUser(empId);*/

    employeeService.save(employee);

    return R.success("新增员工成功");
  }

  @GetMapping("/page")
  public R<Page> page(Integer page, Integer pageSize, String name) {
    log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

    // 构造分页构造器
    Page pageInfo = new Page(page, pageSize);

    // 构造条件构造器
    LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
    // 添加过滤条件
    lqw.like(StringUtils.isNotBlank(name), Employee::getName,name);
    // 添加排序条件
    lqw.orderByDesc(Employee::getUpdateTime);
    // 执行查询
    employeeService.page(pageInfo, lqw);
    return R.success(pageInfo);
  }


  @PutMapping
  public R<String> update(HttpSession session, @RequestBody Employee employee) {
    log.info("员工信息: {}", employee.toString());

    Long empId = (Long) session.getAttribute("employee");
    /*employee.setUpdateTime(LocalDateTime.now());
    employee.setUpdateUser(empId);*/

    employeeService.updateById(employee);
    return R.success("员工信息修改成功");
  }

  /**
   * 根据id查询员工信息
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R<Employee> getById(@PathVariable("id") Long id) {
    log.info("根据id查询员工信息:{}", id);
    Employee employee = employeeService.getById(id);
    if (employee != null) {
      return R.success(employee);
    }
    return R.error("没有查询到对应的员工信息");
  }

}
