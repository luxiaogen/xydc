package com.lys.xydc.dto;

import com.lys.xydc.entity.Setmeal;
import com.lys.xydc.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
