package com.lys.xydc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lys.xydc.common.BaseContext;
import com.lys.xydc.common.R;
import com.lys.xydc.entity.AddressBook;
import com.lys.xydc.service.IAddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 * 地址簿管理
 */
@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

  private final IAddressBookService addressBookService;

  @Autowired
  public AddressBookController(IAddressBookService addressBookService) {
    this.addressBookService = addressBookService;
  }

  /**
   * 新增地址簿方法
   *
   * @return
   */
  @PostMapping
  public R<AddressBook> save(@RequestBody AddressBook addressBook) {
    log.info("addressBook: {}", addressBook);
    addressBook.setUserId(BaseContext.getCurrentId());

    addressBookService.save(addressBook);
    return R.success(addressBook);
  }

  @PutMapping("/default")
  public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
    // 思路
    // 1.首先将该用户的所有地址设为非默认的地址 因为默认地址只能有一个
    log.info("addressBook: {}", addressBook);

    LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
    wrapper.set(AddressBook::getIsDefault, 0);
    // SQL:update address_book set is_default = 0 where userId = ?
    addressBookService.update(wrapper);

    // 2.再将选中的地址设置为默认地址
    addressBook.setIsDefault(1);
    // SQL:update address_book set is_default = 1 where id = ?
    addressBookService.updateById(addressBook);
    return R.success(addressBook);
  }


  /**
   * 根据id查询地址
   *
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public R<AddressBook> get(@PathVariable("id") Long id) {
    AddressBook addressBook = addressBookService.getById(id);

    if (addressBook != null) {
      return R.success(addressBook);
    }
    return R.error("该地址不存在");
  }


  /**
   * 查询默认地址
   *
   * @return
   */
  @GetMapping("/default")
  public R<AddressBook> getDefault() {
    LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
    wrapper.eq(AddressBook::getIsDefault, 1); // 1表示默认地址

    //SQL:select * from address_book where user_id = ? and is_default = 1
    AddressBook addressBook = addressBookService.getOne(wrapper);

    if (addressBook != null) {
      return R.success(addressBook);
    }
    return R.error("没有找到该地址");
  }

  /**
   * 查询指定用户的所有地址
   * @return
   */
  @GetMapping("/list")
  public R<List<AddressBook>> list(AddressBook addressBook) {
    addressBook.setUserId(BaseContext.getCurrentId());
    log.info("addressBook:{}", addressBook);
    LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
    wrapper.orderByDesc(AddressBook::getUpdateTime);

    //SQL:select * from address_book where user_id = ? order by update_time desc
    List<AddressBook> addressBooks = addressBookService.list(wrapper);

    return R.success(addressBooks);
  }


  @PutMapping
  public R<AddressBook> update(@RequestBody AddressBook addressBook) {
    log.info("地址修改信息: {}", addressBook);

    addressBookService.updateById(addressBook);

    return R.success(addressBook);
  }


}
