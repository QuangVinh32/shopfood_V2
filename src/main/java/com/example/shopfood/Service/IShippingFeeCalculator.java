package com.example.shopfood.Service;

public interface IShippingFeeCalculator {

    /**
     * Tính phí ship dựa trên giá tạm tính và tỉnh giao hàng.
     *
     * @param subtotal  tổng tiền trước phí ship (đồng)
     * @param province  tỉnh/thành phố giao hàng (có thể null)
     * @return phí ship (đồng)
     */
    int calculate(int subtotal, String province);
}
