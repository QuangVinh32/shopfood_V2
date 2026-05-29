package com.example.shopfood.Service.Class;

import com.example.shopfood.Service.IShippingFeeCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShippingFeeCalculator implements IShippingFeeCalculator {

    @Value("${app.shipping.default-fee:25000}")
    private int defaultFee;

    @Value("${app.shipping.free-threshold:300000}")
    private int freeThreshold;

    /**
     * Tính phí ship đơn giản:
     * - Đơn >= freeThreshold: miễn phí
     * - Còn lại: defaultFee
     * Sau này có thể swap implementation khác (theo GHN/GHTK API) qua interface.
     */
    @Override
    public int calculate(int subtotal, String province) {
        if (subtotal >= freeThreshold) return 0;
        return defaultFee;
    }
}
