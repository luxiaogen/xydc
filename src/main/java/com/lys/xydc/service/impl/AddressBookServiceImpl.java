package com.lys.xydc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lys.xydc.entity.AddressBook;
import com.lys.xydc.mapper.AddressBookMapper;
import com.lys.xydc.service.IAddressBookService;
import org.springframework.stereotype.Service;

/**
 * @author 陆玉升
 * date: 2023/04/20
 * Description:
 */

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {
}
