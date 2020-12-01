package yk.shiroyk.lightword.db.converter;

import androidx.room.TypeConverter;

import yk.shiroyk.lightword.ui.managedata.OrderEnum;

public class OrderConverter {
    @TypeConverter
    public static OrderEnum toOrderEnum(String order) {
        return order == null ? null : OrderEnum.valueOf(order);
    }

    @TypeConverter
    public static Integer toInteger(OrderEnum order) {
        return order == null ? null : order.getOrderBy();
    }
}
