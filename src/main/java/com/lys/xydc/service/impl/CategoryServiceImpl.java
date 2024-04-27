package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.common.CustomServiceException;
import com.lys.xydc.entity.Category;
import com.lys.xydc.entity.Dish;
import com.lys.xydc.entity.Setmeal;
import com.lys.xydc.mapper.CategoryMapper;
import com.lys.xydc.service.ICategoryService;
import com.lys.xydc.service.IDishService;
import com.lys.xydc.service.ISetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

  // 业务层之间可以相互调用,但是绝对不允许业务层调用其它Dao层

  @Autowired
  private IDishService dishService;

  @Autowired
  private ISetmealService setmealService;

  /**
   * 根据id删除分类，删除之前需要进行判断
   * 分类下有没有菜品，有没有套餐
   * @param id
   */
  @Override
  public void remove(Long id) {
    LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
    // 添加查询条件，根据分类id进行查询
    dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
    int dishCount = dishService.count(dishLambdaQueryWrapper);

    // 查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
    if (dishCount > 0) {
      // 已经关联菜品，抛出一个业务异常
      throw new CustomServiceException("当前分类下关联了菜品,不能删除");
    }

    // 查询当前分类是否关联了套餐，如果已经关联了，抛出一个业务异常
    LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
    // 添加查询条件,根据分类id进行查询
    setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
    int setmealCount = setmealService.count(setmealLambdaQueryWrapper);

    if (setmealCount > 0) {
      // 已经关联了套餐，抛出一个业务异常
      throw new CustomServiceException("当前分类下关联了套餐,不能删除");
    }

    // 正常删除分类 --> 说明当前分类下没有任何菜品或套餐
    super.removeById(id);
  }
}
