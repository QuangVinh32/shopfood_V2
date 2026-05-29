# SHOPFOOD API GUIDE — Hướng dẫn test trên Postman

> **Base URL**: `http://localhost:8080`
> **Content-Type**: `application/json` (trừ upload file dùng `multipart/form-data`)
> **Authentication**: Bearer JWT token trong header `Authorization: Bearer <accessToken>`

## Mục lục
1. [Quy ước chung](#quy-ước-chung)
2. [Auth (đăng ký / đăng nhập / refresh / logout / email)](#1-auth)
3. [User](#2-user)
4. [Product](#3-product)
5. [Product Size](#4-product-size)
6. [Category](#5-category)
7. [Banner](#6-banner)
8. [Cart](#7-cart)
9. [Shipping Address](#8-shipping-address)
10. [Order](#9-order)
11. [Voucher](#10-voucher)
12. [Payment (Momo)](#11-payment)
13. [Notification](#12-notification)
14. [Favourite](#13-favourite)
15. [Review](#14-review)
16. [File](#15-file)

---

## Quy ước chung

### Phân quyền
| Tag | Ý nghĩa |
|---|---|
| 🟢 PUBLIC | Không cần đăng nhập |
| 🔵 USER | Cần Bearer token (USER/MANAGER/ADMIN) |
| 🔴 ADMIN | Cần Bearer token với role ADMIN |

### Setup Postman Environment
Tạo Environment `Shopfood Dev` với variable:
```
baseUrl = http://localhost:8080
accessToken = (để trống — set sau khi login)
refreshToken = (để trống — set sau khi login)
```

### Authorization tab trong mỗi request
- Type: `Bearer Token`
- Token: `{{accessToken}}`

### Sau khi login, lưu token tự động
Trong tab **Tests** của request Login, paste:
```javascript
const res = pm.response.json();
pm.environment.set("accessToken", res.token);
pm.environment.set("refreshToken", res.refreshToken);
```

---

## 1. Auth

### 1.1. Đăng ký 🟢
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/register`
- **Body**: `form-data` (vì có upload ảnh)

| Key | Type | Value |
|---|---|---|
| username | text | nguyenvana |
| password | text | password123 |
| email | text | a@gmail.com |
| fullName | text | Nguyễn Văn A |
| phone | text | 0901234567 |
| address | text | 123 Lê Lợi, Q1 |
| image | file | (chọn file ảnh) |

**Response 201**: `"Đăng ký thành công. Kiểm tra email để xác thực tài khoản."`

---

### 1.2. Đăng nhập 🟢
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/login`
- **Body**: `raw JSON`
```json
{
  "username": "nguyenvana",
  "password": "password123"
}
```

**Response 200**:
```json
{
  "userId": 1,
  "username": "nguyenvana",
  "fullName": "Nguyễn Văn A",
  "email": "a@gmail.com",
  "phone": "0901234567",
  "address": "123 Lê Lợi, Q1",
  "role": "USER",
  "image": "http://localhost:8080/files/image/abc.jpg",
  "token": "eyJhbGciOi...",
  "refreshToken": "abc123..."
}
```

**Lưu ý**: Sai 5 lần → bị khóa 10 phút theo IP+username.

---

### 1.3. Refresh access token 🟢
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/refresh`
- **Body**: `raw JSON`
```json
{ "refreshToken": "{{refreshToken}}" }
```

**Response 200**:
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "new_refresh_token...",
  "expiresInMs": 900000
}
```

---

### 1.4. Logout 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/logout`
- **Headers**: `Authorization: Bearer {{accessToken}}`
- **Body**: `raw JSON`
```json
{ "refreshToken": "{{refreshToken}}" }
```

**Response 200**: `"Đã đăng xuất"`

---

### 1.5. Verify email 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/auth/verify-email?token=<token_trong_email>`

**Response 200**: `"Xác thực email thành công"`

---

### 1.6. Quên mật khẩu 🟢
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/forgot-password?email=a@gmail.com`

**Response 200**: `"Nếu email tồn tại, link reset đã được gửi"` (luôn trả 200 để chống enumeration).

---

### 1.7. Reset password 🟢
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/auth/reset-password?token=<token_trong_email>&newPassword=newpass123`

**Response 200**: `"Đặt lại mật khẩu thành công"`

---

## 2. User

### 2.1. Xem profile của tôi 🔵
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/users/me`

**Response**:
```json
{
  "userId": 1,
  "username": "nguyenvana",
  "email": "a@gmail.com",
  "fullName": "Nguyễn Văn A",
  "role": "USER",
  "image": "http://localhost:8080/files/image/abc.jpg",
  "phone": "0901234567",
  "address": "123 Lê Lợi, Q1"
}
```

### 2.2. Đổi mật khẩu 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/users/me/change-password`
- **Body**: `raw JSON`
```json
{
  "oldPassword": "password123",
  "newPassword": "newpass456"
}
```

**Response 200**: `"Đổi mật khẩu thành công"`. Đồng thời revoke tất cả refresh token → buộc login lại các thiết bị khác.

### 2.3. Cập nhật profile 🔵 (chính chủ hoặc admin)
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/v1/users/edit/{userId}`
- **Body**: `raw JSON` (chỉ field cần update)
```json
{ "fullName": "Tên mới", "phone": "0907654321" }
```

### 2.4. Danh sách user 🔴
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/users/get-all`

### 2.5. Tạo user (admin) 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/users/create`
- **Body**: tương tự register, sẽ tạo với role MANAGER

### 2.6. Xem user theo ID 🔴
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/users/{userId}`

### 2.7. Xóa user 🔴
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/users/{userId}`

> Không xóa được user role ADMIN hoặc user còn lịch sử đơn hàng.

---

## 3. Product

### 3.1. Danh sách sản phẩm 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/products/get-all?page=0&size=10&sort=productId`

### 3.2. Chi tiết sản phẩm cho user 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/products/user/{id}`

### 3.3. Chi tiết sản phẩm cho admin 🔴
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/products/admin/{id}`

### 3.4. Tạo sản phẩm 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/products`
- **Body**: `form-data`

| Key | Type | Value |
|---|---|---|
| productName | text | Pizza Hải Sản |
| description | text | Mô tả |
| categoryId | text | 1 |
| productImages | file | (nhiều ảnh) |
| productSizes | text | JSON array |

### 3.5. Sửa sản phẩm 🔴
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/products/{id}`
- **Body**: `form-data` (như Create)

### 3.6. Xóa sản phẩm 🔴
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/products/{id}`

---

## 4. Product Size

### 4.1. Lấy size theo product 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/product_sizes/product/{productId}`

### 4.2. Lấy size theo ID 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/product_sizes/{id}`

### 4.3. Tạo size 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/product_sizes/product/{productId}`
- **Body**: `raw JSON`
```json
{
  "sizeName": "M",
  "price": 150000,
  "discount": 10,
  "quantity": 50
}
```

### 4.4. Sửa size 🔴
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/product_sizes/{sizeId}`
- **Body**: như Create

### 4.5. Bulk upsert size 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/product_sizes/bulk/{productId}`
- **Body**: `raw JSON` (array)
```json
[
  { "productSizeId": 1, "sizeName": "M", "price": 150000, "discount": 0, "quantity": 50 },
  { "sizeName": "L", "price": 200000, "discount": 5, "quantity": 30 }
]
```

---

## 5. Category

### 5.1. Danh sách 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/categories/get-all?page=0&size=10`

### 5.2. Chi tiết 🟢
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/categories/{id}`

### 5.3. Tạo 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/categories`
- **Body**: `form-data` (có upload ảnh)

### 5.4. Sửa 🔴
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/categories/{id}`

### 5.5. Xóa 🔴
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/categories/{id}`

---

## 6. Banner

### 6.1. Danh sách 🔴
- `GET {{baseUrl}}/api/banners?page=0&size=10`

### 6.2. Chi tiết 🔴
- `GET {{baseUrl}}/api/banners/{id}`

### 6.3. Tạo 🔴
- `POST {{baseUrl}}/api/banners` (form-data)

### 6.4. Sửa 🔴
- `PUT {{baseUrl}}/api/banners/{id}`

### 6.5. Xóa 🔴
- `DELETE {{baseUrl}}/api/banners/{id}`

---

## 7. Cart

### 7.1. Thêm sản phẩm vào giỏ (có quantity) 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/carts/add`
- **Body**: `raw JSON`
```json
{
  "productId": 1,
  "productSizeId": 2,
  "quantity": 3
}
```

### 7.2. Thêm 1 sản phẩm (qty +1) 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/carts/add/{productId}/{productSizeId}`

### 7.3. Giảm 1 đơn vị 🔵
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/carts/decrease/{productId}/{productSizeId}`

### 7.4. Xóa hẳn 1 sản phẩm khỏi giỏ 🔵
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/carts/remove/{productId}/{productSizeId}`

### 7.5. Xóa toàn bộ giỏ 🔵
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/carts/clear`

### 7.6. Xem giỏ 🔵
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/carts/items`

### 7.7. Xem tổng tiền 🔵
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/carts/total`

---

## 8. Shipping Address

### 8.1. Danh sách địa chỉ của tôi 🔵
- `GET {{baseUrl}}/api/shipping-addresses`

### 8.2. Thêm địa chỉ 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/shipping-addresses`
- **Body**: `raw JSON`
```json
{
  "receiverName": "Nguyễn Văn A",
  "receiverPhone": "0901234567",
  "addressLine": "123 Lê Lợi",
  "ward": "Bến Nghé",
  "district": "Quận 1",
  "province": "TP. HCM",
  "default": true
}
```

### 8.3. Sửa địa chỉ 🔵
- `PUT {{baseUrl}}/api/shipping-addresses/{id}` (body như Create)

### 8.4. Xóa địa chỉ 🔵
- `DELETE {{baseUrl}}/api/shipping-addresses/{id}`

---

## 9. Order

### 9.1. Checkout (luồng đầy đủ) 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/orders/checkout?shippingAddressId=1&voucherCode=GIAM10&note=Giao gấp`

**Response 200**:
```json
{ "orderId": 123 }
```

> Server tự: snapshot địa chỉ, tính ship, trừ kho atomic, áp voucher, gửi email confirm.

### 9.2. Tạo order legacy (deprecated, không có shipping) 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/orders?voucherCode=GIAM10`

### 9.3. Áp voucher cho order đã tạo 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/orders/{orderId}/apply-voucher?code=GIAM10`

**Response**:
```json
{
  "originalAmount": 100000,
  "discountAmount": 10000,
  "finalAmount": 90000
}
```

### 9.4. Xem chi tiết đơn 🔵
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/orders/{id}`

> Chỉ chủ đơn hoặc admin xem được. User khác → 403.

### 9.5. Đơn của tôi (phân trang) 🔵
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/orders/me?page=0&size=10&idDesc=true`

### 9.6. Cập nhật trạng thái 🔵 (CANCEL) / 🔴 (other)
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/v1/orders/{orderId}`
- **Body**: `raw JSON`
```json
{ "status": "CANCELED" }
```

> User chỉ được CANCEL đơn của mình. Admin được đổi mọi status.
> Status hợp lệ: `PENDING, CONFIRMED, SHIPPING, DELIVERED, COMPLETED, CANCELED, RETURNED`

### 9.7. Xóa đơn 🔴
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/orders/{id}`

### 9.8. Danh sách đơn cho admin 🔴
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/orders/admin/orders?page=0&size=10&search=A&userId=1`

### 9.9. Doanh thu 🔴
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/orders/admin/revenue`

**Response**:
```json
{
  "originalRevenue": 5000000,
  "totalDiscount": 500000,
  "netRevenue": 4500000
}
```

> Tính theo đơn `DELIVERED + COMPLETED`.

---

## 10. Voucher

### 10.1. Tạo voucher 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/vouchers`
- **Body**: `raw JSON`
```json
{
  "code": "GIAM10",
  "description": "Giảm 10% cho đơn từ 100k",
  "discountType": "PERCENT",
  "discountValue": 10,
  "maxDiscount": 50000,
  "minOrderValue": 100000,
  "usageLimitGlobal": 1000,
  "usageLimitPerUser": 3,
  "target": "ALL",
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-12-31T23:59:59"
}
```

> Validation:
> - `discountValue >= 1`
> - PERCENT → `discountValue <= 100`
> - `endDate > startDate`

### 10.2. Sửa voucher 🔴
- `PUT {{baseUrl}}/api/v1/vouchers/{id}`

### 10.3. Xóa voucher 🔴
- `DELETE {{baseUrl}}/api/v1/vouchers/{id}`

### 10.4. Danh sách voucher (admin) 🔴
- `GET {{baseUrl}}/api/v1/vouchers/admin`

### 10.5. Danh sách voucher khả dụng cho user 🔵
- `GET {{baseUrl}}/api/v1/vouchers/user`

### 10.6. Xem voucher theo code 🔵
- `GET {{baseUrl}}/api/v1/vouchers/{code}`

---

## 11. Payment

### 11.1. Tạo yêu cầu thanh toán Momo 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/payments/momo/create?orderId=123`

**Response 200**:
```json
{ "payUrl": "https://test-payment.momo.vn/v2/gateway/pay?token=..." }
```

> Redirect user sang `payUrl`. User trả tiền xong → Momo callback `/api/payments/momo/ipn` → order chuyển sang `CONFIRMED`.

### 11.2. Webhook IPN từ Momo 🟢 (server-to-server)
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/payments/momo/ipn`
- **Body**: payload Momo (đã verify HMAC-SHA256 signature)

> Không cần test thủ công, Momo tự gọi.

---

## 12. Notification

### 12.1. Xem notification của tôi 🔵
- `GET {{baseUrl}}/api/notifications`

### 12.2. Tạo notification (gửi mass) 🔴
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/notifications`
- **Body**: `raw JSON`
```json
{
  "title": "Khuyến mãi tháng 6",
  "description": "Giảm 30% toàn bộ Pizza",
  "redirectUrl": "/promotion/june",
  "notificationType": "ALL"
}
```

### 12.3. Đánh dấu đã đọc 🔵
- `PUT {{baseUrl}}/api/notifications/{id}/read`

### 12.4. Đánh dấu đã đọc tất cả 🔵
- `PUT {{baseUrl}}/api/notifications/read-all`

### 12.5. Xóa 🔵
- `DELETE {{baseUrl}}/api/notifications/{id}`

### 12.6. Danh sách notification (admin) 🔴
- `GET {{baseUrl}}/api/notifications/admin`

---

## 13. Favourite

### 13.1. Toggle yêu thích 🔵
- `POST {{baseUrl}}/api/favourites/{productId}` → trả `true` (đã thích) hoặc `false` (đã bỏ)

### 13.2. Danh sách yêu thích 🔵
- `GET {{baseUrl}}/api/favourites`

### 13.3. Check 1 sản phẩm có yêu thích không 🔵
- `GET {{baseUrl}}/api/favourites/{productId}/check`

---

## 14. Review

### 14.1. Danh sách review 🔵
- `GET {{baseUrl}}/api/reviews`

### 14.2. Chi tiết review 🔵
- `GET {{baseUrl}}/api/reviews/{id}`

### 14.3. Tạo review 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/reviews`
- **Body**:
```json
{
  "productId": 1,
  "rating": 5,
  "reviewText": "Ngon, giao nhanh"
}
```

> 1 user chỉ review 1 sản phẩm 1 lần.
> Rating phải `1-5`.

### 14.4. Sửa review 🔵 (chính chủ)
- `PUT {{baseUrl}}/api/reviews/{id}`

### 14.5. Xóa review 🔵 (chính chủ)
- `DELETE {{baseUrl}}/api/reviews/{id}`

---

## 15. File

### 15.1. Upload ảnh 🔵
- **Method**: `POST`
- **URL**: `{{baseUrl}}/files/image`
- **Body**: `form-data`

| Key | Type | Value |
|---|---|---|
| image | file | (chọn ảnh JPG/PNG/GIF/WEBP) |

**Response 200**: đường dẫn file đã lưu

> Validate bằng magic bytes, không tin Content-Type.
> File rename thành UUID để chống path traversal.

### 15.2. Xem ảnh 🟢
- `GET {{baseUrl}}/files/image/{fileName}`

---

## Phụ lục: HTTP Status Codes

| Code | Ý nghĩa | Khi nào |
|---|---|---|
| 200 | OK | Request thành công |
| 201 | Created | Tạo resource mới |
| 204 | No Content | Xóa thành công |
| 400 | Bad Request | Validation fail |
| 401 | Unauthorized | Token thiếu/sai/hết hạn |
| 403 | Forbidden | Token OK nhưng không đủ quyền |
| 404 | Not Found | Resource không tồn tại |
| 409 | Conflict | Vi phạm constraint (vd username trùng) |
| 413 | Payload Too Large | File upload > 10MB |
| 423 | Locked | Login bị khóa do brute force |
| 500 | Internal Server Error | Lỗi server |

## Error response format chuẩn

```json
{
  "timestamp": 1716988800000,
  "code": 400,
  "path": "/api/register",
  "message": "Dữ liệu không hợp lệ",
  "errors": {
    "email": "Invalid email format",
    "password": "Password phải từ 8 đến 100 ký tự"
  }
}
```

---

## Swagger UI

Truy cập: `http://localhost:8080/swagger-ui.html` để xem doc tự generate + test trực tiếp.
