package com.lys.xydc.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 * 自定义元数据对象处理器
 */

@Component // 让Spring容器管理
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
  /**
   * 插入操作自动填充
   * @param metaObject
   */
  @Override
  public void insertFill(MetaObject metaObject) {
    log.info("公共字段自动填充[insert]...");
    log.info(metaObject.toString());
    metaObject.setValue("createTime", LocalDateTime.now());
    metaObject.setValue("updateTime", LocalDateTime.now());
    metaObject.setValue("createUser", BaseContext.getCurrentId());
    metaObject.setValue("updateUser", BaseContext.getCurrentId());
  }

  /**
   * 更新操作自动填充
   * @param metaObject
   */
  @Override
  public void updateFill(MetaObject metaObject) {
    log.info("公共字段自动填充[update]...");
    log.info(metaObject.toString());
    metaObject.setValue("updateTime", LocalDateTime.now());
    metaObject.setValue("updateUser", BaseContext.getCurrentId());
  }
}
