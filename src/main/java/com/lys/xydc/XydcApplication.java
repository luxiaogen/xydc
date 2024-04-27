package com.lys.xydc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j // lombok 提供的日志功能   --> 直接使用log变量
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement // 开启事务
@EnableCaching // 开启Spring Cache注解方式是缓存功能
public class XydcApplication {

  public static void main(String[] args) {
    SpringApplication.run(XydcApplication.class, args);
    log.info("项目启动成功");
  }

}
