package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.common.CustomServiceException;
import com.lys.xydc.dto.DishDto;
import com.lys.xydc.entity.Dish;
import com.lys.xydc.entity.DishFlavor;
import com.lys.xydc.mapper.DishMapper;
import com.lys.xydc.service.IDishFlavorService;
import com.lys.xydc.service.IDishService;
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
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements IDishService {

  private final IDishFlavorService dishFlavorService;

  @Autowired
  public DishServiceImpl(IDishFlavorService dishFlavorService) {
    this.dishFlavorService = dishFlavorService;
  }

  /**
   * 新增菜品，同时把保存对应的口味数据
   * @param dishDto
   */
  @Override
  @Transactional // 涉及多张表 开启事务扫描
  public void saveWithFlavor(DishDto dishDto) {

    // 保存菜品信息到dish表中
    this.save(dishDto);

    Long dishId = dishDto.getId(); // 菜品Id

    // 获取口味信息  菜品口味
    List<DishFlavor> flavors = dishDto.getFlavors();
    flavors = flavors.stream().map(item -> {
      item.setDishId(dishId);
      return item;
    }).collect(Collectors.toList());

    // 保存口味信息  到 菜品口味dish_flavor中
    dishFlavorService.saveBatch(flavors); // 批量保存

  }

  /**
   * 根据id查询菜品信息和对应的口味
   * @param id
   * @return
   */
  @Override
  public DishDto getByIdWithFlavor(Long id) {

    // 获取菜品信息
    Dish dish = this.getById(id);

    DishDto dishDto = new DishDto();

    BeanUtils.copyProperties(dish, dishDto); // 复制

    // 查询当前菜品对应的口味信息，从dish_flavor查询
    LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(DishFlavor::getDishId, dish.getId());
    List<DishFlavor> flavors = dishFlavorService.list(wrapper);
    dishDto.setFlavors(flavors);
    return dishDto;
  }

  /**
   * 更新菜品信息的同时 更新口味
   * @param dishDto
   */
  @Override
  @Transactional
  public void updateWithFlavor(DishDto dishDto) {
    // 更新dish表基本信息
    this.updateById(dishDto);
    // 清理当前菜品对应口味数据--dish_flavor表的delete操作
    LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(DishFlavor::getDishId,dishDto.getId());
    dishFlavorService.remove(wrapper);

    // 添加当前提交过来的口味数据--dish_flavor表的insert操作
    List<DishFlavor> flavors = dishDto.getFlavors();
    flavors = flavors.stream().map(item -> {
      item.setDishId(dishDto.getId());
      return item;
    }).collect(Collectors.toList());

    dishFlavorService.saveBatch(flavors);
  }

  /**
   * 批量删除口味和菜品信息
   * @param ids
   */
  @Override
  @Transactional
  public void removeByIdsWithFlavor(List<Long> ids) {

    // 查询菜品是否可以删除
    LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
    dishLambdaQueryWrapper.in(Dish::getId,ids);
    dishLambdaQueryWrapper.eq(Dish::getStatus, 1); // 查看是否停售
    int count = this.count(dishLambdaQueryWrapper);

    if (count > 0) {
      throw new CustomServiceException("菜品正在售卖中,不能删除");
    }

    this.removeByIds(ids);

    // 获取相关的口味信息
    LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
    dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
    dishFlavorService.remove(dishFlavorLambdaQueryWrapper);

  }
}
