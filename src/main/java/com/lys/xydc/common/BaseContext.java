package com.lys.xydc.common;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */

public class BaseContext {

  private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

  public static void setCurrentId(Long empId) {
    threadLocal.set(empId);
  }

  public static Long getCurrentId() {
    return threadLocal.get();
  }

}
