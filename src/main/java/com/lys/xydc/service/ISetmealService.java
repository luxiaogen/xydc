package com.lys.xydc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lys.xydc.dto.SetmealDto;
import com.lys.xydc.entity.Setmeal;

import java.util.List;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 */

public interface ISetmealService extends IService<Setmeal> {

  /**
   * 新增套餐，同时需要保存套餐和菜品的关联信息
   * @param setmealDto
   */
  void saveWithDish(SetmealDto setmealDto);

  /**
   * 删除套餐并删除套餐关联的数据
   * @param ids
   */
  void removeWithDish(List<Long> ids);

  /**
   * 根据id获取套餐和菜品信息
   * @param id
   * @return
   */
  SetmealDto getByIdWithDish(Long id);

  /**
   * 修改套餐和对应的菜品信息
   * @param setmealDto
   */
  void updateWithDishes(SetmealDto setmealDto);

}
