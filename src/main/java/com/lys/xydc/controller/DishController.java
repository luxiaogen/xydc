package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lys.xydc.common.R;
import com.lys.xydc.dto.DishDto;
import com.lys.xydc.entity.Category;
import com.lys.xydc.entity.Dish;
import com.lys.xydc.entity.DishFlavor;
import com.lys.xydc.service.ICategoryService;
import com.lys.xydc.service.IDishFlavorService;
import com.lys.xydc.service.IDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 陆玉升
 * date: 2023/04/2505/13
 * Description:
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

  @Autowired
  private IDishService dishService;

  @Autowired
  private IDishFlavorService dishFlavorService;

  @Autowired
  private ICategoryService categoryService;

  @Autowired
  private RedisTemplate redisTemplate;

  /**
   * 保存菜品信息
   * @param dishDto
   * @return
   */
  @PostMapping
  public R<String> save(@RequestBody DishDto dishDto) {
    log.info(dishDto.toString());
    dishService.saveWithFlavor(dishDto);

    // 清理某个分类下面的菜品缓存数据
    String key = "dish_" + dishDto.getCategoryId() + "_1";
    redisTemplate.delete(key);
    return R.success("新增菜品成功");
  }

  /**
   * 菜品分页查询
   * @param page
   * @param pageSize
   * @param name
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    // 分页构造器
    Page<Dish> pageInfo = new Page<>(page,pageSize);
    Page<DishDto> dishDtoPage = new Page<>();

    // 条件构造器
    LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank(name), Dish::getName, name);
    wrapper.orderByDesc(Dish::getUpdateTime);

    // 执行分页查询
    dishService.page(pageInfo, wrapper);

    // 对象拷贝 分页数据  处理records数据
    BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

    // 处理一下分页的数据 因为需要分类的名称
    List<Dish> records = pageInfo.getRecords();

    List<DishDto> list = records.stream().map(item -> {

      DishDto dishDto = new DishDto();
      // 对象拷贝
      BeanUtils.copyProperties(item, dishDto);

      // 获取分页的id
      Long categoryId = item.getCategoryId();
      Category category = categoryService.getById(categoryId);
      if (category != null) {
        String categoryName = category.getName(); // 获取分页名称
        dishDto.setCategoryName(categoryName);
      }
      return dishDto;
    }).collect(Collectors.toList());

    dishDtoPage.setRecords(list);


    return R.success(dishDtoPage);
  }


  /**
   * 获取菜品信息和口味信息
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R<DishDto> getDish(@PathVariable("id") Long id) {

    DishDto dishDto = dishService.getByIdWithFlavor(id);

    return R.success(dishDto);
  }


  /**
   * 修改菜品信息和口味信息
   * @param dishDto
   * @return
   */
  @PutMapping
  public R<String> update(@RequestBody DishDto dishDto) {
    dishService.updateWithFlavor(dishDto);

    // 清理所有菜品的缓存数据
    //Set keys = redisTemplate.keys("dish_*");
    //redisTemplate.delete(keys);

    // 清理某个分类下面的菜品缓存数据
    String key = "dish_" + dishDto.getCategoryId() + "_1";
    redisTemplate.delete(key);

    return R.success("修改成功");
  }

  /**
   * 修改状态 停售 起售  --- 批量
   * @param status
   * @param ids
   * @return
   */
  @PostMapping("/status/{status}")
  public R<String> updStatus(@PathVariable("status")Integer status, @RequestParam("ids") List<Long> ids) {
      log.info("status:{},ids:{}", status, ids);

    LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(Dish::getId,ids);
    List<Dish> dishList = dishService.list(wrapper);
    dishList.forEach(dish -> {
      dish.setStatus(status);
      dishService.updateById(dish);
    });

    return R.success("修改状态成功");
  }


  /**
   * 删除  批量删除菜品和口味信息
   * @return
   */
  @DeleteMapping
  public R<String> remove(@RequestParam("ids") List<Long> ids) {

    log.info("ids:{}", ids);

    dishService.removeByIdsWithFlavor(ids);

    return R.success("删除成功");
  }


  /**
   * 根据条件查询对应的菜品数据
   * @return
   */
  /*@GetMapping("/list")
  public R<List<Dish>> list(Dish dish) {
    // 构造查询条件
    LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
    // 添加条件，查询状态为1(起售状态)的菜品
    wrapper.eq(Dish::getStatus, 1);
    // 添加排序条件
    wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

    List<Dish> list = dishService.list(wrapper);

    return R.success(list);
  }*/


  /**
   * 根据条件查询对应的菜品数据
   * @return
   */
  @GetMapping("/list")
  public R<List<DishDto>> list(Dish dish) {
    List<DishDto> dishDtoList = null;
    // 动态构造key
    String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus(); // dish_1231231231231231_1

    // 先从redis中获取数据
    dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

    // 如果存在，直接返回，无需查询数据库
    if (dishDtoList != null) return R.success(dishDtoList);

    // 构造查询条件
    LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
    // 添加条件，查询状态为1(起售状态)的菜品
    wrapper.eq(Dish::getStatus, 1);
    // 添加排序条件
    wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

    List<Dish> list = dishService.list(wrapper);

    // 对查询出来的数据进行处理
    dishDtoList = list.stream().map(item -> {
      DishDto dishDto = new DishDto();

      // 对象的拷贝
      BeanUtils.copyProperties(item, dishDto);

      Long categoryId = item.getCategoryId();
      Category category = categoryService.getById(categoryId);
      if (category != null) {
        String categoryName = category.getName(); // 获取分类的名称
        dishDto.setCategoryName(categoryName);
      }

      // 获取菜品id
      Long dishId = item.getId();

      LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
      dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
      List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

      dishDto.setFlavors(dishFlavorList);
      return dishDto;
    }).collect(Collectors.toList());

    // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis中
    redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

    return R.success(dishDtoList);
  }




}
