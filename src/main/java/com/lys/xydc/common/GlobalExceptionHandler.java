package com.lys.xydc.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author 陆玉升
 * date: 2023/04/2505/11
 * Description:
 * 全局异常处理
 */

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

  /**
   * 异常处理的方法
   * @param ex
   * @return
   */
  @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
  public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
    log.error(ex.getMessage());

    if (ex.getMessage().contains("Duplicate entry")) {
      String[] split = ex.getMessage().split(" ");
      String msg = split[2] + "已存在";
      return R.error(msg);
    }

    return R.error("未知错误");
  }


  /**
   * 业务层异常方法
   * @param ex
   * @return
   */
  @ExceptionHandler(CustomServiceException.class)
  public R<String> exceptionHandler(CustomServiceException ex) {
    log.error(ex.getMessage());

    return R.error(ex.getMessage());
  }


}
