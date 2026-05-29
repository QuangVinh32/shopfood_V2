package com.example.shopfood.Model.Entity;

public enum OrderStatus {
    PENDING,        // Vừa tạo, đang chuẩn bị
    CONFIRMED,      // Đã thanh toán (Momo) hoặc admin duyệt (COD)
    SHIPPING,       // Đang giao
    COMPLETED,      // Đã giao + thu tiền xong → tính doanh thu
    CANCELED        // Hủy
}
