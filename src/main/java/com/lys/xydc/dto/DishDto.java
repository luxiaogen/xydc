package com.lys.xydc.dto;

import com.lys.xydc.entity.Dish;
import com.lys.xydc.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陆玉升
 * date: 2023/04/2505/13
 * Description:
 * 菜品数据传输对象
 */
@Data
public class DishDto extends Dish {

  private List<DishFlavor> flavors = new ArrayList<>();

  private String categoryName;

  private Integer copies;
}

