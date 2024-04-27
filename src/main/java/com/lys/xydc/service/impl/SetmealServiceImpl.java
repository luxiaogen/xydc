package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.common.CustomServiceException;
import com.lys.xydc.dto.SetmealDto;
import com.lys.xydc.entity.Setmeal;
import com.lys.xydc.entity.SetmealDish;
import com.lys.xydc.mapper.SetmealMapper;
import com.lys.xydc.service.ISetmealDishService;
import com.lys.xydc.service.ISetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements ISetmealService {

  @Autowired
  private ISetmealDishService setmealDishService;

  /**
   * 新增套餐，同时需要保存套餐和菜品的关联关系
   * @param setmealDto
   */
  @Override
  @Transactional
  public void saveWithDish(SetmealDto setmealDto) {
    // 保存套餐的基本新，操作setmeal，执行insert操作
    this.save(setmealDto); // 这里保存之后 我们的setmeal对象就有了id

    // 可以发现这里的setmealDish还缺少setmealId
    List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
    setmealDishes = setmealDishes.stream().map(item -> {
      item.setSetmealId(setmealDto.getId());
      return item;
    }).collect(Collectors.toList());

    // 保存套餐和菜品的相关信息，操作setmeal_dish，执行insert操作
    setmealDishService.saveBatch(setmealDishes);
  }

  /**
   * 删除套餐并删除套餐关联的数据
   * @param ids
   */
  @Override
  @Transactional
  public void removeWithDish(List<Long> ids) {

    // select count(*) from setmeal where id in (1,3,4,5) and status = 1
    // 查询套餐状态，确定是否可以删除
    LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
    setmealWrapper.in(Setmeal::getId,ids);
    setmealWrapper.eq(Setmeal::getStatus, 1); // 未停售
    int count = this.count(setmealWrapper);

    if (count > 0) {
      // 如果不能删除,抛出一个业务异常
      throw new CustomServiceException("套餐正在售卖中,删除失败");
    }

    // 如果可以删除,先删除套餐表中的数据 setmeal
    this.removeByIds(ids);
    // 删除关系表中的数据 setmeal_dish
    LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
    setmealDishWrapper.in(SetmealDish::getSetmealId, ids);
    setmealDishService.remove(setmealDishWrapper);
  }

  /**
   * 获取套餐和对应的菜品信息
   * @param id
   * @return
   */
  @Override
  public SetmealDto getByIdWithDish(Long id) {
    Setmeal setmeal = this.getById(id);
    SetmealDto setmealDto = new SetmealDto();

    // 拷贝属性
    BeanUtils.copyProperties(setmeal, setmealDto);

    // 查询对应的菜品信息
    LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
    setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
    List<SetmealDish> dishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
    setmealDto.setSetmealDishes(dishes);
    return setmealDto;
  }

  /**
   * 修改套餐和对应的菜品信息
   * @param setmealDto
   */
  @Override
  @Transactional
  public void updateWithDishes(SetmealDto setmealDto) {
    // 首先更新菜品信息
    this.updateById(setmealDto);

    // 更新对应的菜品信息
    // 可以选择先删除  再插入
    LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
    setmealDishService.remove(wrapper);

    // 插入新的数据
    List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
    // 可以发现这里的对象没有setmealId
    setmealDishes = setmealDishes.stream().map(item -> {
      Long setmealId = setmealDto.getId();
      item.setSetmealId(setmealId);
      return item;
    }).collect(Collectors.toList());

    setmealDishService.saveBatch(setmealDishes);
  }
}
