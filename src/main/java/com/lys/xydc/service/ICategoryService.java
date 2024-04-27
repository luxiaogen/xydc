package com.lys.xydc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lys.xydc.entity.Category;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 */

public interface ICategoryService extends IService<Category> {

  void remove(Long id);

}
