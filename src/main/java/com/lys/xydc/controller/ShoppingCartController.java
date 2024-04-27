package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lys.xydc.common.BaseContext;
import com.lys.xydc.common.R;
import com.lys.xydc.entity.ShoppingCart;
import com.lys.xydc.service.IShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

  private final IShoppingCartService shoppingCartService;

  @Autowired
  public ShoppingCartController(IShoppingCartService shoppingCartService) {
    this.shoppingCartService = shoppingCartService;
  }

  /**
   * 添加购物车
   * @param shoppingCart
   * @return
   */
  @PostMapping("/add")
  public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
    log.info("shoppingCart:{}", shoppingCart);

    // 设置用户id，指定当前是哪个用户的购物车数据
    Long userId = BaseContext.getCurrentId();
    shoppingCart.setUserId(userId);

    // 查询菜品或者是套餐是否在购物车中
    Long dishId = shoppingCart.getDishId();
    LambdaQueryWrapper<ShoppingCart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
    cartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);

    if (dishId != null) {
      // 说明选择是菜品
      cartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
    } else {
      // 说明选择是套餐
      cartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
    }


    // 查询当前菜品或者是套餐是否存在于购物车
    // SQL: select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
    ShoppingCart cart = shoppingCartService.getOne(cartLambdaQueryWrapper);
    if (cart == null) {
      // 如果不存在，就添加到购物车，数量默认是1
      shoppingCart.setNumber(1);
      shoppingCart.setCreateTime(LocalDateTime.now());
      shoppingCartService.save(shoppingCart);
      cart = shoppingCart;
    } else {
      // 如果已经存在，就在原来的数量基础上+1
      Integer number = cart.getNumber();
      cart.setNumber(number + 1);
      shoppingCartService.updateById(cart);
    }


    return R.success(cart);
  }

  /**
   * 减少菜品或套餐方法
   * @param shoppingCart
   * @return
   */
  @PostMapping("/sub")
  public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

    // 设置用户id，指定当前是哪个用户的购物车数据
    Long userId = BaseContext.getCurrentId();
    shoppingCart.setUserId(userId);

    // 查询菜品或者是套餐是否在购物车中
    Long dishId = shoppingCart.getDishId();
    LambdaQueryWrapper<ShoppingCart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
    cartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);

    if (dishId != null) {
      // 说明选择是菜品
      cartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
    } else {
      // 说明选择是套餐
      cartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
    }


    // 查询当前菜品或者是套餐是否存在于购物车
    // SQL: select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
    ShoppingCart cart = shoppingCartService.getOne(cartLambdaQueryWrapper);

    // 如果数量是1点-号数据库中直接删除即可
    if (cart.getNumber() == 1) {
      // 如果只点了一份还点了减少一份的功能 就直接在购物车中删除即可
      shoppingCartService.removeById(shoppingCart);
    } else {
      cart.setNumber(cart.getNumber() - 1);
      shoppingCartService.updateById(cart);
    }


    return R.success(cart);
  }


  /**
   * 查看购物车
   * @return
   */
  @GetMapping("/list")
  public R<List<ShoppingCart>> list() {
    log.info("查看购物车..");

    LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
    wrapper.orderByAsc(ShoppingCart::getCreateTime);

    List<ShoppingCart> list = shoppingCartService.list(wrapper);

    return R.success(list);
  }


  /**
   * 清空购物车
   * @return
   */
  @DeleteMapping("/clean")
  public R<String> clean() {
    // SQL:delete from shopping_cart where user_id = ?
    LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
    shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

    shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    return R.success("清空购物车成功");
  }

}
