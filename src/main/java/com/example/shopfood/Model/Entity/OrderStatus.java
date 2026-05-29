package com.example.shopfood.Model.Entity;

public enum OrderStatus {
    PENDING,        // Mới tạo, chưa xác nhận
    CONFIRMED,      // Đã xác nhận (đã thanh toán hoặc admin duyệt)
    SHIPPING,       // Đang giao
    DELIVERED,      // Đã giao thành công
    COMPLETED,      // Hoàn tất (tính doanh thu)
    CANCELED,       // Đã hủy
    RETURNED        // Đã trả hàng / refund
}
