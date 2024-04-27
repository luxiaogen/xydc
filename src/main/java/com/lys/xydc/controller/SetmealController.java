package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lys.xydc.common.R;
import com.lys.xydc.dto.SetmealDto;
import com.lys.xydc.entity.Category;
import com.lys.xydc.entity.Setmeal;
import com.lys.xydc.service.ICategoryService;
import com.lys.xydc.service.ISetmealDishService;
import com.lys.xydc.service.ISetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 陆玉升
 * date: 2023/04/2505/13
 * Description:
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

  @Autowired
  private ISetmealService setmealService;

  @Autowired
  private ISetmealDishService setmealDishService;

  @Autowired
  private ICategoryService categoryService;


  /**
   * 保存套餐
   * @param setmealDto
   * @return
   */
  @PostMapping
  @CacheEvict(value = "setmealCache", allEntries = true)
  public R<String> save(@RequestBody SetmealDto setmealDto) {

    log.info("套餐信息: {}", setmealDto);

    setmealService.saveWithDish(setmealDto);

    return R.success("新增套餐信息成功");
  }

  /**
   * 分页查询套餐
   * @param page
   * @param pageSize
   * @param name
   * @return
   */
  @GetMapping("/page")
  public R<Page> page(int page, int pageSize, String name) {
    // 分页构造器
    Page<Setmeal> pageInfo = new Page<>(page, pageSize);

    // 条件构造器
    LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
    // 添加查询条件，根据name进行模块查询
    wrapper.like(StringUtils.isNotBlank(name),Setmeal::getName, name);
    // 按照更新时间降序排序
    wrapper.orderByDesc(Setmeal::getUpdateTime);

    // 执行分页操作
    setmealService.page(pageInfo, wrapper);

    // Dto分页构造器
    Page<SetmealDto> setmealDtoPage = new Page<>();
    // 对象拷贝，对records数据进行处理, 这时候setmealDtoPage里面有除了record属性以后的所有属性
    BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
    List<Setmeal> records = pageInfo.getRecords();
    List<SetmealDto> list = records.stream().map(item -> {
      SetmealDto setmealDto = new SetmealDto(); // dto 对象

      // 对象拷贝
      BeanUtils.copyProperties(item, setmealDto);

      // 分类id
      Long categoryId = item.getCategoryId();
      // 根据id查询分类对象
      Category category = categoryService.getById(categoryId);
      if (category != null) {
        setmealDto.setCategoryName(category.getName());
      }
      return setmealDto;
    }).collect(Collectors.toList());

    setmealDtoPage.setRecords(list);

    return R.success(setmealDtoPage);
  }

  @DeleteMapping
  @CacheEvict(value = "setmealCache", allEntries = true)
  public R<String> delete(@RequestParam List<Long> ids) {
    log.info("ids: {}", ids);
    setmealService.removeWithDish(ids);
    return R.success("删除套餐数据成功");
  }

  /**
   * 修改状态 停售 起售  --- 批量
   * @param status
   * @param ids
   * @return
   */
  @PostMapping("/status/{status}")
  @CacheEvict(value = "setmealCache", allEntries = true)
  public R<String> updStatus(@PathVariable("status")Integer status, @RequestParam("ids") List<Long> ids) {
    log.info("status:{},ids:{}", status, ids);

    LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(Setmeal::getId,ids);
    List<Setmeal> dishList = setmealService.list(wrapper);
    dishList.forEach(dish -> {
      dish.setStatus(status);
      setmealService.updateById(dish);
    });

    return R.success("修改状态成功");
  }

  /**
   * 获取套餐信息和菜品信息
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R<SetmealDto> getSetmeal(@PathVariable Long id) {

    log.info("id:{}", id);

    SetmealDto dto = setmealService.getByIdWithDish(id);

    return R.success(dto);
  }


  /**
   * 修改套餐信息和对应的菜品信息
   * @param setmealDto
   * @return
   */
  @CacheEvict(value = "setmealCache", allEntries = true)
  @PutMapping
  public R<String> update(@RequestBody SetmealDto setmealDto) {
    log.info("dto:{}", setmealDto.toString());

    setmealService.updateWithDishes(setmealDto);

    return R.success("修改成功");
  }

  /**
   * 根据条件查询套餐数据
   * @param setmeal
   * @return
   */
  @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
  @GetMapping("/list")
  public R<List<Setmeal>> list(Setmeal setmeal) {
    // 条件构造器
    LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
    wrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
    wrapper.orderByDesc(Setmeal::getUpdateTime);

    // 根据条件进行查询
    List<Setmeal> list = setmealService.list(wrapper);
    return R.success(list);
  }

}
