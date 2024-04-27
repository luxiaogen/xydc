package com.lys.xydc.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 陆玉升
 * date: 2023/04/2505/10
 * Description:
 * 员工实体
 */

@Data
public class Employee implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String username;

  private String name;

  private String password;

  private String phone;

  private String sex;

  private String idNumber; // 身份证号码

  private Integer status;

  @TableField(fill = FieldFill.INSERT) // 插入时填充字段
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE) // 插入更新时填充字段
  private LocalDateTime updateTime;

  @TableField(fill = FieldFill.INSERT) // 插入时填充字段
  private Long createUser;

  @TableField(fill = FieldFill.INSERT_UPDATE) // 插入更新时填充字段
  private Long updateUser;

}