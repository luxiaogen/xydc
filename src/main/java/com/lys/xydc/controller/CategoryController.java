package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lys.xydc.common.R;
import com.lys.xydc.entity.Category;
import com.lys.xydc.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

  @Autowired
  private ICategoryService categoryService;

  /**
   * 添加分类
   * @param category
   * @return
   */
  @PostMapping
  public R<String> save(@RequestBody Category category){
    log.info("category信息：{}", category);
    categoryService.save(category);
    return R.success("添加分类成功");
  }


  @GetMapping("/page")
  public R<Page> page(int page, int pageSize) {
    // 分页构造器
    Page pageInfo = new Page(page,pageSize);
    // 条件构造器
    LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
    lqw.orderByAsc(Category::getSort);

    // 进行分页查询
    categoryService.page(pageInfo, lqw);

    return R.success(pageInfo);
  }

  /**
   * 根据id删除分类信息
   * @param id
   * @return
   */
  @DeleteMapping
  public R<String> delete(Long id) {
    log.info("删除分类的id:{}", id);

    // categoryService.removeById(id);

    categoryService.remove(id);

    return R.success("分类信息删除成功");
  }


  /**
   * 修改分类信息
   * @param category
   * @return
   */
  @PutMapping
  public R<String> update(@RequestBody Category category) {
    log.info("修改分类的信息: {}", category);

    categoryService.updateById(category);

    return R.success("分类信息修改成功");
  }

  /**
   * 根据条件查询分类数据
   * @param category
   * @return
   */
  @GetMapping("/list")
  public R<List<Category>> list(Category category) {
    // 条件构造器
    LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
    // 添加条件
    lqw.eq(category.getType() != null, Category::getType, category.getType());
    // 添加排序条件
    lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

    List<Category> list = categoryService.list(lqw);

    return R.success(list);
  }




}
