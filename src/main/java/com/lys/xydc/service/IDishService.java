package com.lys.xydc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lys.xydc.dto.DishDto;
import com.lys.xydc.entity.Dish;

import java.util.List;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 */

public interface IDishService extends IService<Dish> {

  /**
   * 新增菜品，同时插入菜品对应的口味数据，需要操作两张表:dish dish_flavor
   * @param dishDto
   */
  void saveWithFlavor(DishDto dishDto);

  /**
   * 根据id查询菜品和相关的口味
   * @param id
   * @return
   */
  DishDto getByIdWithFlavor(Long id);


  /**
   * 更新菜品信息，同时更新对应的口味信息
   * @param dishDto
   */
  void updateWithFlavor(DishDto dishDto);

  /**
   * 删除-批量删除菜品信息和口味信息
   * @param ids
   */
  void removeByIdsWithFlavor(List<Long> ids);
}
