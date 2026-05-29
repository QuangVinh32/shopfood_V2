# NHẬT KÝ NÂNG CẤP — 2026-05-29

Tổng hợp toàn bộ thay đổi đã thực hiện trong ngày, chia theo 2 đợt:

- **Đợt 1**: Fix luồng giỏ hàng → đơn hàng → voucher → doanh thu (tính tiền cho chính xác)
- **Đợt 2**: Fix bảo mật toàn diện (auth, IDOR, file upload, config, cascade, validation)

---

## ĐỢT 1 — TÍNH TIỀN & VOUCHER

### Vấn đề đã sửa

| # | Mô tả | Mức độ |
|---|---|---|
| 1 | `OrderService.createOrder(voucherCode)` nhận `voucherCode` nhưng KHÔNG hề gọi `applyVoucher` → đơn vẫn lưu giá gốc | P0 |
| 2 | `int totalAmount += double itemTotal` bị truncate phần thập phân từng item → sai tổng đơn | P0 |
| 3 | Order chỉ có `totalAmount` (đã giảm), KHÔNG lưu giá gốc + tiền giảm → không phân tích được khuyến mãi | P1 |
| 4 | Cancel order không hoàn lại `voucher.usedCount` và `userVoucher.usedCount` → user mất lượt | P1 |
| 5 | Tính % voucher bằng phép chia `int` (`originalAmount * percent / 100`) → làm tròn xuống, mất tiền | P2 |
| 6 | Chưa có endpoint tính doanh thu chính xác (lọc theo `COMPLETED`) | P1 |

### File đã sửa (Đợt 1)

| File | Vị trí | Thay đổi |
|---|---|---|
| `Model/Entity/Order.java` | dòng 24-33 | Thêm 2 field `originalAmount` (Integer), `discountAmount` (Integer) |
| `Service/Class/VoucherService.java` | dòng 182-202 | Tính discount dùng `double + Math.round`, set `originalAmount` + `discountAmount` vào order |
| `Service/Class/VoucherService.java` | dòng 230-258 | Thêm method `rollbackVoucher(Order)` — giảm usedCount, clear voucher khỏi order, khôi phục total |
| `Service/IVoucherService.java` | dòng 23 | Thêm signature `void rollbackVoucher(Order order)` |
| `Service/Class/OrderService.java` | dòng 104 | Đổi `int totalAmount` → `double totalAmount` |
| `Service/Class/OrderService.java` | dòng 163-172 | Round 1 lần ở cuối + gọi `voucherService.applyVoucher` nếu có code |
| `Service/Class/OrderService.java` | dòng 207-210 | Khi cancel: gọi `rollbackVoucher` |
| `Service/IOrderService.java` | dòng 26-30 | Thêm `getTotalRevenue`, `getTotalOriginalRevenue`, `getTotalDiscount` |
| `Service/Class/OrderService.java` | cuối file | Implement 3 method doanh thu, lọc `OrderStatus.COMPLETED` |
| `Repository/OrderRepository.java` | dòng 20-27 | 3 query `sumTotalAmountByStatus`, `sumOriginalAmountByStatus`, `sumDiscountAmountByStatus` |
| `Controller/OrderController.java` | endpoint mới | `GET /api/v1/orders/admin/revenue` trả về `{originalRevenue, totalDiscount, netRevenue}` |
| `Model/DTO/OrderGetDTO.java` | dòng 13-14 | Thêm `originalAmount`, `discountAmount` để hiển thị |
| `Service/Class/OrderService.java` | `toDTO` | Set thêm 2 field trên |

---

## ĐỢT 2 — BẢO MẬT TOÀN DIỆN

### 🚨 CRITICAL đã fix

#### C1 + C8: JWT subject = `username` (thay vì `fullName` không unique)
**Vấn đề cũ**: 2 user trùng `fullName` → user B thấy giỏ hàng / đơn hàng / voucher của user A. Đổi mật khẩu cũng broken vì principal = fullName nhưng service tìm bằng username.

**File sửa**:
- `Config/JWT/JwtTokenUtils.java`:
  - `createAccessToken`: `setSubject(loginDTO.getUsername())` thay vì `getFullName()`
  - `parseAccessToken`: `loginDto.setUsername(...)` thay vì `setFullName(...)` + đọc `loginId` từ claims
  - JWT secret lấy từ `@Value("${app.jwt.secret}")`, kiểm tra ≥ 64 bytes lúc `@PostConstruct`
- `Config/JwtRequestFilter.java`:
  - Set `Authentication.principal = loginDto.getUsername()` (trước là fullName)
- `Repository/UserRepository.java`: xóa method `findByFullName`, thêm `existsByPhone`
- Tất cả services đổi `findByFullName(fullName)` → `findByUsername(username)`:
  - `Service/Class/CartService.java` (7 chỗ)
  - `Service/Class/OrderService.java`
  - `Service/Class/VoucherService.java`
  - `Service/Class/ReviewService.java` (3 chỗ)
  - `Service/Class/FavouriteService.java`
  - `Controller/NotificationController.java`
- `Utils/CurrentUserUtil.java` (file MỚI): helper `currentUsername()`, `currentUser()`, `hasRole(String)`

#### C2: Order endpoints check ownership (IDOR)
**Vấn đề cũ**: bất kỳ user đã login đều có thể đọc/sửa/xóa đơn của user khác bằng cách đoán id.

**File sửa**:
- `Service/Class/OrderService.java`:
  - Thêm `assertOwnerOrAdmin(Order)` helper
  - `getOrderById`: gọi `assertOwnerOrAdmin`
  - `updateOrder`: chỉ admin được đổi status ngoài CANCELED; user chỉ được CANCEL đơn của mình
  - `deleteOrder`: admin-only
  - Thêm `getMyOrders(Pageable)` để user xem đơn của mình
- `Controller/OrderController.java`:
  - `GET /api/v1/orders/me` (mới) cho user xem đơn của mình
  - `GET /api/v1/orders/admin/orders` + `DELETE` + `/admin/revenue`: `@PreAuthorize("hasAuthority('ADMIN')")`
- `Model/Request/Order/FilterOrder.java`: thêm field `userId`
- `Specification/OrderSpecification.java`: sửa toàn bộ path `fullName`→`user.fullName`, `address`→`user.address`, `total`→`totalAmount`, thêm filter `userId`

#### C3: UserController bảo vệ + bỏ password trong response
**Vấn đề cũ**: `GET /api/v1/users/{id}` trả về `Users` entity nguyên bản gồm cả **password hash bcrypt**. `POST /create` cho phép tạo MANAGER không cần quyền. `PUT /edit` không check ownership.

**File sửa**:
- `Controller/UserController.java` (viết lại toàn bộ):
  - `/get-all`, `/{userId}`, `/create`, `DELETE /{userId}`: `@PreAuthorize("hasAuthority('ADMIN')")`
  - `GET /me` (mới): xem profile bản thân
  - `POST /me/change-password` (thay `/user/change-password` cũ)
  - `PUT /edit/{userId}`: check ownership; admin mới sửa được role khác
- `Model/DTO/UserForAdmin.java`: **xóa field `password`**, thêm `address`
- `Service/Class/UserService.java`:
  - `createUser`: check `existsByPhone` trước (tránh lỗi DB)
  - `updateUser`: chỉ update field nếu không null/blank (trước đây gửi rỗng → reset password)
  - `deleteUser`: chặn xóa user còn lịch sử đơn hàng

#### C4: JwtRequestFilter whitelist bằng pattern matcher
**Vấn đề cũ**: dùng `StringUtils.containsAnyIgnoreCase(uri, "/api/login", ...)` → URL `/api/orders/?x=/api/login` cũng được bypass auth.

**File sửa**:
- `Config/JwtRequestFilter.java`: viết lại bằng `AntPathMatcher` + `Map<method, List<pattern>>` chính xác

#### C5: Move secret & DB password ra biến môi trường
**Vấn đề cũ**: JWT secret và DB password hard-code trong source, đã commit vào git.

**File sửa**:
- `Config/JWT/JwtTokenUtils.java`: secret lấy từ `${app.jwt.secret}` qua `@Value`
- `src/main/resources/application.properties`: viết lại toàn bộ, các giá trị nhạy cảm lấy từ env

```
APP_JWT_SECRET          → bắt buộc, ≥ 64 ký tự
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_UPLOAD_DIR          → default ./uploads/images
APP_PUBLIC_BASE_URL     → default http://localhost:8080
SPRING_JPA_DDL_AUTO     → default update (production nên đổi validate)
SPRING_JPA_SHOW_SQL     → default false
```

> ⚠️ **Phải rotate secrets cũ**: secret cũ + password `123456` ĐÃ commit git → coi như public.

#### C6: Fix `checkToken` expiration logic
**Vấn đề cũ**: `if (tokenEntity.getExpiration().after(new Date(now + 864000000L)))` → so sánh expiration > (now + 10 ngày), không bao giờ true → token expired vẫn được chấp nhận.

**File sửa**:
- `Config/JWT/JwtTokenUtils.java` dòng 91: đổi thành `expiration.before(new Date())`

#### C7: Sanitize file upload
**Vấn đề cũ**:
- Filename gốc của user → path traversal (`../../etc/passwd`)
- Chỉ check `Content-Type` header → spoof dễ
- UPLOAD_DIR hard-code `D:\Java Sping Boot\shopfood_V2\uploads\images` (typo "Sping", không deploy được)

**File sửa**:
- `Utils/FileManager.java`:
  - `isTypeFileImage`: check **magic bytes** thực sự (JPEG/PNG/GIF/WEBP)
  - `getFormatFile`: chỉ trả về extension trong whitelist
- `Service/Class/FileService.java` (viết lại):
  - Tên file = `UUID.randomUUID()` + ext → chống collision + path traversal
  - Thư mục lấy từ `${app.upload.dir}` qua `@Value`
  - Defense-in-depth: kiểm tra `target.startsWith(baseDir)`
  - Lưu `FileEntity` (trước đây có code nhưng comment ra)
- `Controller/FileController.java`:
  - `getImage`: chặn `/`, `\`, `..` trong tên file
  - Đường dẫn từ config, không hard-code

---

### ⚠️ HIGH đã fix

#### H1: WebSecurityConfiguration phân quyền lại
**File**: `Config/WebSecurityConfiguration.java` (viết lại toàn bộ)
- Nhóm rõ: **PUBLIC** (get-all/get-detail/login/register/files/image), **ADMIN** (products POST/PUT/DELETE, categories, banners, vouchers/admin, orders/admin), **AUTHENTICATED** (carts, orders, /me, notifications, favourites, reviews)
- CORS: bỏ origin pattern không cần, chỉ giữ localhost
- CORS allowed headers cụ thể thay vì `*`

#### H2: Login attempts thread-safe
**File**: `Controller/AuthController.java`
- Đổi `HashMap` → `ConcurrentHashMap`
- Dùng `merge(key, 1, Integer::sum)` atomic
- Khi hết lockout time → tự reset

#### H3: Login enumeration
**File**: `Controller/AuthController.java`
- Đếm fail attempts cả khi user không tồn tại (trước đây chỉ đếm khi sai password)
- Dùng generic `INVALID_CREDENTIAL` thay vì nhiều message khác nhau

#### H4: Validation đầy đủ
**File**:
- `Model/Request/User/LoginRequest.java`: thêm `@NotBlank`
- `Model/Request/User/UserRequest.java`: thêm `@Size(min=8)` password, `@Pattern` cho username + phone, `@Email`
- `Model/Request/User/ChangePasswordRequest.java`: thêm `@NotBlank` + `@Size(min=8)`
- `Controller/AuthController.java`: register thêm `@Valid @ModelAttribute` (trước thiếu `@Valid` nên validation không chạy)

#### H6: Bỏ cascade ALL trên Users
**File**: `Model/Entity/Users.java`
- `orders`, `reviews`: bỏ `cascade = ALL` → giữ lịch sử khi xóa user
- `cart`: giữ `cascade = ALL` + `orphanRemoval = true`
- Xóa unique trên `address` (chung cư, ký túc xá có thể chung địa chỉ)
- Thêm unique trên `email`

#### H7: UPLOAD_DIR từ config
Xem C7.

#### H8: deleteOrder xử lý NotFound
**File**: `Controller/OrderController.java`
- Wrap try-catch trả 404 nếu order không tồn tại

#### H9: Bỏ `@CrossOrigin("*")` trên controllers
**File** (5 file):
- `Controller/AuthController.java`
- `Controller/CartController.java`
- `Controller/FavouriteController.java`
- `Controller/NotificationController.java`
- `Controller/VoucherController.java`

Lý do: conflict với central CORS config trong `WebSecurityConfiguration`. Wildcard origin + credentials=true bị browser chặn.

---

### 🟡 MEDIUM đã fix

| # | Mô tả | File |
|---|---|---|
| M1 | Bỏ `unique=true` trên `address` | `Model/Entity/Users.java` |
| M2 | `updateUser` chỉ update field nếu không null/blank | `Service/Class/UserService.java` |
| M9 | `createOrder` không nuốt exception khi xóa cart (trước catch `Exception e` + `System.err`) → giờ throw để rollback transaction | `Service/Class/OrderService.java` |
| M10 | `register` thêm `@Valid @ModelAttribute` | `Controller/AuthController.java` |
| — | `OrderSpecification` fix path JPA (`fullName`/`address`/`total` không tồn tại → throw) | `Specification/OrderSpecification.java` |
| — | `updateOrder` thêm `@Transactional` | `Service/Class/OrderService.java` |
| — | `getRevenue` dùng `Map.of` thay double-brace init | `Controller/OrderController.java` |

---

## DANH SÁCH TẤT CẢ FILE THAY ĐỔI

### File MỚI tạo (3)
- `src/main/java/com/example/shopfood/Utils/CurrentUserUtil.java`
- `CHANGELOG_2026-05-29.md` (file này)
- `src/main/java/com/example/shopfood/Exception/ErrorResponseBase.java` (thêm 2 enum)

### File SỬA (30)

**Config**
- `Config/JWT/JwtTokenUtils.java`
- `Config/JwtRequestFilter.java`
- `Config/WebSecurityConfiguration.java`

**Controller**
- `Controller/AuthController.java`
- `Controller/CartController.java`
- `Controller/FavouriteController.java`
- `Controller/FileController.java`
- `Controller/NotificationController.java`
- `Controller/OrderController.java`
- `Controller/UserController.java`
- `Controller/VoucherController.java`

**Service**
- `Service/IOrderService.java`
- `Service/IVoucherService.java`
- `Service/Class/CartService.java`
- `Service/Class/FavouriteService.java`
- `Service/Class/FileService.java`
- `Service/Class/OrderService.java`
- `Service/Class/ReviewService.java`
- `Service/Class/UserService.java`
- `Service/Class/VoucherService.java`

**Model / DTO / Request**
- `Model/DTO/OrderGetDTO.java`
- `Model/DTO/UserForAdmin.java`
- `Model/Entity/Order.java`
- `Model/Entity/Users.java`
- `Model/Request/Order/FilterOrder.java`
- `Model/Request/User/ChangePasswordRequest.java`
- `Model/Request/User/LoginRequest.java`
- `Model/Request/User/UserRequest.java`

**Repository / Specification**
- `Repository/OrderRepository.java`
- `Repository/UserRepository.java`
- `Specification/OrderSpecification.java`

**Util / Resource**
- `Utils/FileManager.java`
- `src/main/resources/application.properties`

---

## HÀNH ĐỘNG BẮT BUỘC TRƯỚC KHI CHẠY

### 1. Set biến môi trường
```
APP_JWT_SECRET=<random ≥ 64 ký tự — KHÔNG dùng secret cũ>
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/shopfood
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=<password mới — KHÔNG dùng "123456">
APP_UPLOAD_DIR=./uploads/images
APP_PUBLIC_BASE_URL=http://localhost:8080
```

### 2. DB migration
```sql
-- Đợt 1 (tiền)
ALTER TABLE orders ADD COLUMN original_amount INT NULL;
ALTER TABLE orders ADD COLUMN discount_amount INT NULL DEFAULT 0;

-- Đợt 2 (bảo mật)
ALTER TABLE users DROP INDEX UK_address;   -- bỏ unique address nếu đã tạo
ALTER TABLE users ADD UNIQUE (email);      -- thêm unique email nếu chưa có
```
> Nếu `spring.jpa.hibernate.ddl-auto=update` thì Hibernate tự ALTER. Production khuyên đặt `validate` + dùng Flyway.

### 3. Rotate toàn bộ secret
- JWT secret cũ + DB password `123456` đã commit git → coi như công khai.
- Đổi password DB ngay.
- Generate JWT secret mới (`openssl rand -base64 64`).
- Cân nhắc xóa secret cũ khỏi git history.

### 4. Bắt user login lại
Token cũ có `subject = fullName` → không parse được sang `username` → tất cả user phải login lại.

### 5. Test các luồng then chốt
- Login với credential đúng → token hợp lệ
- User A không xem được order của user B (gọi `GET /api/v1/orders/{id_của_B}` → 403)
- User thường gọi `/admin/...` → 403
- Đổi mật khẩu hoạt động (trước đây luôn báo "User not found")
- Upload `.html` rename `.jpg` → bị reject (magic bytes)
- Tạo order với voucher code → totalAmount = originalAmount - discount, voucher.usedCount++
- Cancel order → kho hồi, voucher.usedCount--

---

---

# ĐỢT 3 — BỔ SUNG TÍNH NĂNG MỚI (2026-05-29, cùng ngày)

Bổ sung nền tảng + thanh toán + shipping + email theo roadmap đã đề xuất.

## 3.1 — pom.xml: deps mới + dọn deps trùng
**File**: `pom.xml`
- **Thêm**: `spring-boot-starter-mail`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-actuator`, `flyway-core`, `flyway-mysql`, `springdoc-openapi-starter-webmvc-ui:2.6.0`
- **Bỏ**: `mysql-connector-java:8.0.33` (deprecated, conflict với `mysql-connector-j`), `log4j-api`, `log4j-core` (conflict với logback mặc định của Spring Boot, nguy cơ Log4Shell), `httpclient` (không dùng), `jakarta.persistence-api` (Spring Boot tự manage)
- Lý do: tránh classpath conflict + lỗ hổng bảo mật cũ

## 3.2 — Flyway migration
**File mới**: `src/main/resources/db/migration/V2__shipping_payment_refresh_emailverify.sql`
**File sửa**: `application.properties` thêm `spring.flyway.baseline-on-migrate=true`, `baseline-version=1`
- DB hiện có không bị phá → Flyway baseline ở V1 (trạng thái hiện tại), V2 thêm:
  - `users` thêm 6 cột (email_verified, email_verify_token/expires, reset_password_token/expires, enabled)
  - bảng mới: `refresh_tokens`, `shipping_addresses`, `order_status_history`, `idempotency_keys`
  - `orders` thêm 5 cột (receiver_name, receiver_phone, shipping_address, shipping_fee, note)
  - `payments` thêm 6 cột (provider, status, amount, transaction_id, gateway_response, paid_at, order_id)

## 3.3 — ExceptionHandler nâng cấp
**File**: `Exception/ControllerExceptionHandler.java` (viết lại toàn bộ)
- Handle riêng: `AccessDeniedException`→403, `BadCredentialsException`→401, `EntityNotFoundException`→404, `DataIntegrityViolationException`→409, `MethodArgumentNotValidException`/`BindException`→400 với field errors map, `ConstraintViolationException`→400, `MaxUploadSizeExceededException`→413
- Generic `Exception`→500 với message thân thiện, **không leak stack** ra client (log đầy đủ trong server)
- Response body chuẩn `{timestamp, code, path, message, errors?}`

## 3.4 — Refresh token + Logout
**File mới**:
- `Model/Entity/RefreshToken.java` — token opaque random 64 byte, lưu user, userAgent, IP, expiresAt, revoked
- `Repository/RefreshTokenRepository.java` — findByToken, revokeAllByUser, deleteExpiredOrRevoked
- `Service/Class/RefreshTokenService.java` — issue, rotate (revoke cũ + cấp mới), revoke, validateAndGetUser
- `Model/Request/User/RefreshRequest.java`
- `Model/DTO/TokenPairDTO.java`

**File sửa**:
- `Config/JWT/JwtTokenUtils.java`:
  - Access token expiration đọc từ `app.jwt.access-token-expiration-ms` (default 15 phút thay vì 10 ngày)
- `Controller/AuthController.java`:
  - Login trả thêm `refreshToken` trong LoginDTO
  - `POST /api/auth/refresh` — rotate refresh, trả access mới
  - `POST /api/auth/logout` — revoke refresh
- `Model/DTO/LoginDTO.java`: thêm `refreshToken`
- `application.properties`: `app.jwt.access-token-expiration-ms=900000` (15p), `refresh-token-expiration-ms=2592000000` (30 ngày)

## 3.5 — Email (Gmail SMTP) + verify + forgot password
**File mới**:
- `Service/Class/EmailService.java` — `JavaMailSender` + Thymeleaf template engine, gửi async qua `@Async`
- `Service/Class/EmailVerificationService.java` — sendVerificationEmail, verifyEmail (token 24h), sendResetPasswordEmail (im lặng nếu email không tồn tại — chống enumeration), resetPassword (token 1h)
- `Config/AsyncConfig.java` — `@EnableAsync`
- `src/main/resources/templates/email/verify-email.html`
- `src/main/resources/templates/email/reset-password.html`
- `src/main/resources/templates/email/order-confirmation.html`

**File sửa**:
- `Model/Entity/Users.java`: thêm `emailVerified`, `emailVerifyToken`, `emailVerifyExpiresAt`, `resetPasswordToken`, `resetPasswordExpiresAt`, `enabled`
- `Repository/UserRepository.java`: thêm `findByEmailVerifyToken`, `findByResetPasswordToken`
- `Controller/AuthController.java`:
  - `register`: sau khi save user, gửi email verify (async, không block)
  - `GET /api/auth/verify-email?token=xxx` — public
  - `POST /api/auth/forgot-password?email=xxx` — luôn trả 200
  - `POST /api/auth/reset-password?token=xxx&newPassword=xxx`
  - `login`: chặn nếu `!user.isEnabled()` (`ACCOUNT_DISABLED`)
- `Exception/ErrorResponseBase.java`: thêm `ACCOUNT_DISABLED`, `EMAIL_NOT_VERIFIED`
- `application.properties`: cấu hình SMTP Gmail từ env (`MAIL_USERNAME`, `MAIL_PASSWORD` — phải dùng **App Password** của Gmail, không phải mật khẩu thường)

## 3.6 — Shipping address book + OrderStatus mở rộng
**File mới**:
- `Model/Entity/OrderStatus.java` — mở rộng từ 3 status thành 7: `PENDING, CONFIRMED, SHIPPING, DELIVERED, COMPLETED, CANCELED, RETURNED`
- `Model/Entity/ShippingAddress.java` — 1 user nhiều địa chỉ, có isDefault
- `Model/Entity/OrderStatusHistory.java` — audit log mọi lần đổi status
- `Repository/ShippingAddressRepository.java`
- `Repository/OrderStatusHistoryRepository.java`
- `Service/Class/ShippingAddressService.java`
- `Service/Class/ShippingFeeCalculator.java` — phí ship cơ bản, miễn phí khi đơn ≥ ngưỡng (config)
- `Controller/ShippingAddressController.java` — `/api/shipping-addresses` CRUD
- `Model/Request/Shipping/ShippingAddressRequest.java`

**File sửa**:
- `Model/Entity/Order.java`: thêm `receiverName`, `receiverPhone`, `shippingAddress` (snapshot), `shippingFee`, `note`
- `Service/IOrderService.java`: thêm `createOrderFull(shippingAddressId, voucherCode, note)`
- `Service/Class/OrderService.java`:
  - `createOrderFull`: snapshot địa chỉ vào order, tính shipping fee, áp voucher trên subtotal, total = subtotal + shippingFee - discount
  - `updateOrder`: hoàn kho cả khi CANCELED hoặc RETURNED, ghi `OrderStatusHistory`
  - Helper `recordStatusChange` ghi log mỗi lần đổi status
- `Controller/OrderController.java`:
  - `POST /api/v1/orders/checkout?shippingAddressId=&voucherCode=&note=` — luồng đặt hàng đầy đủ
  - Giữ `POST /api/v1/orders` cũ (backward-compat, không có shipping)

## 3.7 — Momo payment
**File mới**:
- `Model/Entity/Payment.java` (viết lại) — thêm provider, status, amount, transactionId, gatewayResponse, paidAt, order FK
- `Model/Entity/PaymentStatus.java` — PENDING/SUCCESS/FAILED/REFUNDED
- `Repository/PaymentRepository.java`
- `Service/Class/MomoPaymentService.java`:
  - `createPaymentRequest(orderId)`: tạo HMAC-SHA256 signature theo doc Momo, POST tới sandbox endpoint, trả về `payUrl`
  - `handleIpn(payload)`: verify signature, update Payment status, đổi Order → `CONFIRMED` nếu thành công
- `Controller/PaymentController.java`:
  - `POST /api/payments/momo/create?orderId=` (user khởi tạo)
  - `POST /api/payments/momo/ipn` (Momo server gọi, **public**, bảo mật bằng signature)

**File sửa**:
- `Config/WebSecurityConfiguration.java`: permitAll `/api/payments/momo/ipn`, `/api/auth/refresh|forgot-password|reset-password`, `/api/auth/verify-email`, Swagger, `/actuator/health`
- `Config/JwtRequestFilter.java`: whitelist tương ứng
- `application.properties`:
  - `app.momo.partner-code/access-key/secret-key/endpoint/return-url/ipn-url` từ env
  - Default endpoint = sandbox `https://test-payment.momo.vn/v2/gateway/api/create`

---

## CÁC FILE MỚI/SỬA TRONG ĐỢT 3

### File MỚI (Đợt 3)
**Entity** (4):
- `Model/Entity/RefreshToken.java`
- `Model/Entity/ShippingAddress.java`
- `Model/Entity/OrderStatusHistory.java`
- `Model/Entity/PaymentStatus.java`

**Repository** (4):
- `Repository/RefreshTokenRepository.java`
- `Repository/ShippingAddressRepository.java`
- `Repository/OrderStatusHistoryRepository.java`
- `Repository/PaymentRepository.java`

**Service** (5):
- `Service/Class/RefreshTokenService.java`
- `Service/Class/EmailService.java`
- `Service/Class/EmailVerificationService.java`
- `Service/Class/ShippingAddressService.java`
- `Service/Class/ShippingFeeCalculator.java`
- `Service/Class/MomoPaymentService.java`

**Controller** (2):
- `Controller/ShippingAddressController.java`
- `Controller/PaymentController.java`

**DTO / Request** (3):
- `Model/DTO/TokenPairDTO.java`
- `Model/Request/User/RefreshRequest.java`
- `Model/Request/Shipping/ShippingAddressRequest.java`

**Config** (1):
- `Config/AsyncConfig.java`

**Migration / Email template / Resources** (5):
- `src/main/resources/db/migration/V2__shipping_payment_refresh_emailverify.sql`
- `src/main/resources/templates/email/verify-email.html`
- `src/main/resources/templates/email/reset-password.html`
- `src/main/resources/templates/email/order-confirmation.html`

### File SỬA (Đợt 3)
- `pom.xml`
- `application.properties`
- `Config/JWT/JwtTokenUtils.java`
- `Config/JwtRequestFilter.java`
- `Config/WebSecurityConfiguration.java`
- `Controller/AuthController.java`
- `Controller/OrderController.java`
- `Exception/ControllerExceptionHandler.java`
- `Exception/ErrorResponseBase.java`
- `Model/DTO/LoginDTO.java`
- `Model/Entity/Order.java`
- `Model/Entity/OrderStatus.java`
- `Model/Entity/Payment.java`
- `Model/Entity/Users.java`
- `Repository/UserRepository.java`
- `Service/IOrderService.java`
- `Service/Class/OrderService.java`

---

## ENV VARS BỔ SUNG CẦN SET

```bash
# Mail (Gmail App Password, KHÔNG dùng password Gmail thường)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=<App Password 16 ký tự sinh từ myaccount.google.com>
MAIL_FROM=no-reply@shopfood.local
MAIL_VERIFY_LINK_BASE=http://localhost:8080/api/auth/verify-email
MAIL_RESET_LINK_BASE=http://localhost:3000/reset-password

# JWT (mới)
JWT_ACCESS_EXPIRATION=900000        # 15 phút (ms)
JWT_REFRESH_EXPIRATION=2592000000   # 30 ngày (ms)

# Momo (sandbox - lấy từ developers.momo.vn)
MOMO_PARTNER_CODE=MOMO
MOMO_ACCESS_KEY=<sandbox access key>
MOMO_SECRET_KEY=<sandbox secret key>
MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create
MOMO_RETURN_URL=http://localhost:3000/payment/result
MOMO_IPN_URL=https://<ngrok-tunnel>/api/payments/momo/ipn

# Shipping
SHIPPING_DEFAULT_FEE=25000
SHIPPING_FREE_THRESHOLD=300000

# Flyway
FLYWAY_ENABLED=true
```

## SETUP GMAIL APP PASSWORD
1. Vào https://myaccount.google.com/security
2. Bật 2-Step Verification (bắt buộc)
3. Vào "App passwords" → tạo mới → dán vào `MAIL_PASSWORD`
4. Tuyệt đối KHÔNG dùng password Gmail thường (sẽ bị reject với "Less secure app")

## SETUP MOMO SANDBOX
1. Đăng ký tại https://developer.momo.vn/
2. Tạo merchant test → lấy partnerCode/accessKey/secretKey
3. IPN URL phải public — dùng `ngrok http 8080` cho dev, lấy URL https đó set vào `MOMO_IPN_URL`

## API MỚI

### Auth
- `POST /api/auth/refresh` — body `{refreshToken}` → trả `{accessToken, refreshToken, expiresInMs}`
- `POST /api/auth/logout` — body `{refreshToken}`
- `GET /api/auth/verify-email?token=` — public
- `POST /api/auth/forgot-password?email=` — public
- `POST /api/auth/reset-password?token=&newPassword=` — public

### Shipping
- `GET /api/shipping-addresses` — danh sách địa chỉ của tôi
- `POST /api/shipping-addresses` — thêm
- `PUT /api/shipping-addresses/{id}` — sửa
- `DELETE /api/shipping-addresses/{id}` — xóa

### Order
- `POST /api/v1/orders/checkout?shippingAddressId=&voucherCode=&note=` — luồng đầy đủ (snapshot địa chỉ, tính ship, áp voucher)

### Payment
- `POST /api/payments/momo/create?orderId=` — user khởi tạo, trả `{payUrl}` để redirect
- `POST /api/payments/momo/ipn` — Momo gọi server-to-server (public, verify signature)

### Documentation
- `GET /swagger-ui.html` — Swagger UI (public)
- `GET /v3/api-docs` — OpenAPI JSON
- `GET /actuator/health` — health check (public)

## HÀNH ĐỘNG THÊM TRƯỚC KHI CHẠY ĐỢT 3

1. **Chạy Flyway**: bật `spring.flyway.enabled=true`, app sẽ tự apply V2 khi khởi động lần đầu
2. **Test gửi email**: login Gmail bật 2FA → tạo App Password → set env → restart app → test forgot password
3. **Test Momo**: dùng `ngrok` để expose webhook, gọi `POST /api/payments/momo/create?orderId=X` với user đã login → mở payUrl → thanh toán test → kiểm tra Order chuyển sang CONFIRMED
4. **Test refresh token**: login → access token hết hạn sau 15p → FE tự gọi `/auth/refresh` với refreshToken đã lưu → nhận token mới
5. **Test Swagger**: mở `http://localhost:8080/swagger-ui.html` xem toàn bộ API

## NHỮNG GÌ CHƯA LÀM (sprint sau)

- Test coverage (unit + integration)
- Rate limit toàn cục (hiện chỉ login)
- Redis cache cho product/category
- Idempotency thực sự (bảng `idempotency_keys` đã tạo nhưng chưa wire)
- Soft delete
- Inventory log
- Order email confirmation (template đã có, chưa wire vào createOrderFull)
- `getCartTotal` realtime
- Docker / docker-compose
- VNPay, ZaloPay (Momo đã có làm khuôn)
- Push notification (FCM)
- Webhook resend cơ chế retry nếu Momo gọi IPN fail

---

# ĐỢT 4 — REFACTOR INTERFACE PATTERN (Dependency Inversion)

## Lý do
6 service mới tạo ở Đợt 3 (Email, RefreshToken, ShippingAddress, ShippingFeeCalculator, EmailVerification, MomoPayment) **không có interface tương ứng** — vi phạm pattern hiện có của project (mọi service cũ đều có `IService` + `Class implements IService`). Đợt 4 fix vấn đề nhất quán này, đồng thời fix luôn 3 chỗ trong code cũ cũng inject class trực tiếp.

## Lợi ích
1. **Test dễ hơn**: mock interface (vd Mockito mock `IMomoPaymentService` trả `payUrl` giả) thay vì spawn cả Spring context
2. **Swap implementation không phá controller**: muốn đổi Momo → VNPay → ZaloPay, chỉ thêm `IPaymentService` cha, có nhiều impl, switch qua `@Qualifier`. Controller giữ nguyên
3. **`IShippingFeeCalculator`** đặc biệt hữu ích: sau này gắn GHN/GHTK API → viết `GhnShippingFeeCalculator implements IShippingFeeCalculator` rồi swap, OrderService không động vào

## 4.1 — 6 Interface MỚI

| Interface | File mới | Implementation đã có |
|---|---|---|
| `IRefreshTokenService` | `Service/IRefreshTokenService.java` | `RefreshTokenService` |
| `IEmailService` | `Service/IEmailService.java` | `EmailService` |
| `IEmailVerificationService` | `Service/IEmailVerificationService.java` | `EmailVerificationService` |
| `IShippingAddressService` | `Service/IShippingAddressService.java` | `ShippingAddressService` |
| `IShippingFeeCalculator` | `Service/IShippingFeeCalculator.java` | `ShippingFeeCalculator` |
| `IMomoPaymentService` | `Service/IMomoPaymentService.java` | `MomoPaymentService` |

## 4.2 — Service Class implements interface + thêm `@Override`

| File sửa | Thay đổi |
|---|---|
| `Service/Class/RefreshTokenService.java` | `implements IRefreshTokenService` + `@Override` trên 5 method |
| `Service/Class/EmailService.java` | `implements IEmailService` + `@Override` |
| `Service/Class/EmailVerificationService.java` | `implements IEmailVerificationService` + `@Override` trên 4 method, inject `IEmailService` thay `EmailService` |
| `Service/Class/ShippingAddressService.java` | `implements IShippingAddressService` + `@Override` trên 5 method |
| `Service/Class/ShippingFeeCalculator.java` | `implements IShippingFeeCalculator` + đổi `@Component` → `@Service` |
| `Service/Class/MomoPaymentService.java` | `implements IMomoPaymentService` + `@Override` trên 2 method |

## 4.3 — Controller / Service đổi sang inject Interface

| File sửa | Thay đổi |
|---|---|
| `Controller/AuthController.java` | `RefreshTokenService` → `IRefreshTokenService`, `EmailVerificationService` → `IEmailVerificationService` |
| `Controller/ShippingAddressController.java` | `ShippingAddressService` → `IShippingAddressService` |
| `Controller/PaymentController.java` | `MomoPaymentService` → `IMomoPaymentService` |
| `Service/Class/OrderService.java` | `VoucherService` → `IVoucherService`, `ShippingFeeCalculator` → `IShippingFeeCalculator` |
| `Service/Class/EmailVerificationService.java` | `EmailService` → `IEmailService` |

## 4.4 — Bonus: fix luôn code cũ vi phạm pattern

| File sửa | Thay đổi |
|---|---|
| `Controller/UserController.java` | `UserService` → `IUserService` |
| `Controller/ProductImageController.java` | `ProductImageService` → `IProductImageService` |
| `Config/WebSecurityConfiguration.java` | `UserService` → `UserDetailsService` (interface gốc của Spring Security; `DaoAuthenticationProvider` chỉ cần `UserDetailsService`) |

## 4.5 — Verify

```
grep -r "Service\.Class\." src/main/java
→ 0 kết quả
```

Không còn chỗ nào inject class trực tiếp. Toàn bộ controller/service phụ thuộc qua interface, đúng nguyên tắc **Dependency Inversion** của SOLID.

---

## TỔNG KẾT FILE THAY ĐỔI TRONG NGÀY 2026-05-29

### File MỚI tạo (35)

**Đợt 1 + 2** (3):
- `Utils/CurrentUserUtil.java`
- `CHANGELOG_2026-05-29.md`

**Đợt 3** (24):
- Entity: `RefreshToken`, `ShippingAddress`, `OrderStatusHistory`, `PaymentStatus`
- Repository: `RefreshTokenRepository`, `ShippingAddressRepository`, `OrderStatusHistoryRepository`, `PaymentRepository`
- Service Class: `RefreshTokenService`, `EmailService`, `EmailVerificationService`, `ShippingAddressService`, `ShippingFeeCalculator`, `MomoPaymentService`
- Controller: `ShippingAddressController`, `PaymentController`
- DTO/Request: `TokenPairDTO`, `RefreshRequest`, `ShippingAddressRequest`
- Config: `AsyncConfig`
- SQL migration: `V2__shipping_payment_refresh_emailverify.sql`
- Email template: `verify-email.html`, `reset-password.html`, `order-confirmation.html`

**Đợt 4** (6):
- `Service/IRefreshTokenService.java`
- `Service/IEmailService.java`
- `Service/IEmailVerificationService.java`
- `Service/IShippingAddressService.java`
- `Service/IShippingFeeCalculator.java`
- `Service/IMomoPaymentService.java`

### File SỬA (50+)

Tất cả file sửa qua 4 đợt, không liệt kê trùng. Xem chi tiết từng đợt ở trên.

Các file sửa **chỉ trong Đợt 4**:
- `Config/WebSecurityConfiguration.java` (UserService → UserDetailsService)
- `Controller/AuthController.java` (inject interface)
- `Controller/ShippingAddressController.java` (inject interface)
- `Controller/PaymentController.java` (inject interface)
- `Controller/UserController.java` (UserService → IUserService)
- `Controller/ProductImageController.java` (ProductImageService → IProductImageService)
- `Service/Class/OrderService.java` (VoucherService → IVoucherService, ShippingFeeCalculator → IShippingFeeCalculator)
- `Service/Class/RefreshTokenService.java` (implements + @Override)
- `Service/Class/EmailService.java` (implements + @Override)
- `Service/Class/EmailVerificationService.java` (implements + @Override + inject IEmailService)
- `Service/Class/ShippingAddressService.java` (implements + @Override)
- `Service/Class/ShippingFeeCalculator.java` (implements + @Override + @Service)
- `Service/Class/MomoPaymentService.java` (implements + @Override)

---

## TỔNG SỐ THAY ĐỔI TRONG NGÀY

| Đợt | Nội dung | File mới | File sửa |
|---|---|---|---|
| **1** | Fix tính tiền & voucher | 0 | ~10 |
| **2** | Bảo mật toàn diện (auth, IDOR, file upload, ...) | 1 | ~30 |
| **3** | Flyway + Email + Shipping + Momo + Refresh token | 24 | ~17 |
| **4** | Refactor Interface Pattern (Dependency Inversion) | 6 | ~13 |
| **Tổng** | | **31 file mới** | **~60 file sửa** |

---

## CHECKLIST CUỐI NGÀY

- [x] Đợt 1 — Tính tiền chính xác, voucher đúng, doanh thu lọc COMPLETED
- [x] Đợt 2 — Vá 7 critical + 9 high + 10 medium security/business issues
- [x] Đợt 3 — Flyway, ExceptionHandler, Refresh/Logout, Email, Shipping, Momo, Swagger, Actuator
- [x] Đợt 4 — Refactor 6 service mới + 3 chỗ cũ sang Interface pattern
- [x] CHANGELOG đầy đủ cho từng đợt

## VIỆC NGÀY MAI / SPRINT SAU

- Test coverage (unit + integration cho luồng tiền + payment)
- Rate limit toàn cục (Bucket4j hoặc Resilience4j)
- Redis cache product/category
- Idempotency thực sự cho `createOrderFull` (bảng `idempotency_keys` đã có schema)
- Soft delete cho user/product/order
- Inventory log (`StockMovement`)
- `getCartTotal` realtime (hiện đang trả cached)
- Docker + docker-compose (app + MySQL + Redis)
- VNPay, ZaloPay (Momo đã làm khuôn, copy + đổi signature)
- Push notification FCM
- Cơ chế retry IPN webhook nếu Momo gọi fail
- Báo cáo nâng cao (revenue theo ngày/tuần/tháng, top sản phẩm, top khách)
- 2FA (TOTP) cho admin
- Audit log cho admin action

---

# ĐỢT 5 — AUDIT BỔ SUNG: VÁ 5 CRITICAL + 6 HIGH + M-ITEMS

Sau khi rà tổng quát toàn project, phát hiện thêm 5 critical + 6 high lỗ hổng chưa được xử lý ở các đợt trước. Đợt 5 fix hết.

## 5.1 — CRITICAL (5 vấn đề)

### C1. VoucherController không có quyền admin → user thường tạo voucher 100% off
**File**: `Controller/VoucherController.java` (viết lại)
- Thêm `@PreAuthorize("hasAuthority('ADMIN')")` cho `POST`, `PUT`, `DELETE`, `GET /admin`
- Endpoint `POST /api/v1/vouchers` trước đây không match `/api/vouchers/admin/**` → rớt xuống `authenticated()` → ai login cũng tạo được

### C2. ProductSizeController không có quyền admin → user đổi giá tự do
**File**: `Controller/ProductSizeController.java`
- Thêm `@PreAuthorize("hasAuthority('ADMIN')")` cho `POST /product/{id}`, `PUT /{sizeId}`, `POST /bulk/{productId}`
- Tự đổi giá → mua giá 1đ → đây là lỗ hổng tài chính nghiêm trọng

### C3. NotificationController.createNotification spam mass
**File**: `Controller/NotificationController.java`
- Thêm `@PreAuthorize("hasAuthority('ADMIN')")` cho `POST` và `GET /admin`
- Trước đây user thường gọi với `NotificationType.ALL` → spam/phishing tool

### C4. ReviewController.getAllReviews trả full entity → leak password hash
**File**: `Controller/ReviewController.java` (viết lại)
- Đổi `List<Review>` → `List<ReviewDTO>` (DTO không có Users entity raw, chỉ có `UserDTO` với fullName + image)
- Trước: response chứa `review.user.password` (bcrypt hash của TẤT CẢ user)

### C5. ReviewService.updateReview thiếu ownership check
**File**: `Service/Class/ReviewService.java`
- Thêm check `review.user.userId == currentUser.userId`, throw `SecurityException` nếu không khớp
- Trước: user A sửa được review của user B

## 5.2 — HIGH (6 vấn đề)

### H1. Voucher race condition (TOCTOU)
**File mới sửa**: `Repository/VoucherRepository.java`, `Repository/UserVoucherRepository.java`, `Service/Class/VoucherService.java`
- Thêm 2 query atomic:
  ```sql
  UPDATE Voucher SET usedCount = usedCount + 1
   WHERE voucherId = ? AND (usageLimitGlobal IS NULL OR usedCount < usageLimitGlobal)
  ```
  Trả về 0 nếu đã hết → throw "hết lượt"
- Tương tự cho `UserVoucher.usedCount` với `usageLimitPerUser`
- `applyVoucher`: dùng atomic increment, nếu per-user fail thì rollback global
- `rollbackVoucher`: dùng atomic decrement
- Trước: 2 user cùng dùng voucher khi `usedCount = limit - 1` → cả 2 pass check → cả 2 tăng → vượt limit

### H2. changePassword không revoke refresh tokens
**File**: `Service/Class/UserService.java`
- Inject `IRefreshTokenService`
- Sau khi save password mới → `refreshTokenService.revokeAllForUser(user)`
- Trước: đổi password nghi ngờ bị lộ vẫn không khóa được attacker (refresh token còn 30 ngày)

### H3. Logout không invalidate access token
**File**: `Controller/AuthController.java`, `Repository/TokenRepository.java`
- `TokenRepository` thêm `deleteByTokenValue(String token)` + `deleteExpired()`
- `logout` endpoint nhận thêm header `Authorization`, xóa access token khỏi DB
- `JwtRequestFilter` đã check Token entity → sau logout → 401
- Trước: 15 phút window access token vẫn hoạt động

### H4. NotificationController admin endpoints không check role
Đã xử lý ở C3 (gộp chung).

### H5. ProductSizeService.bulkUpsert delete size còn được tham chiếu
**File**: `Repository/CartDetailRepository.java`, `Repository/OrderDetailRepository.java`, `Service/Class/ProductSizeService.java`
- Thêm `existsByProductSize(ProductSize)` cho cả 2 repo
- `bulkUpsert`: trước khi delete, check còn cart/order tham chiếu → nếu có thì **soft-disable** (set quantity=0) thay vì delete
- Trước: FK violation hoặc orphan reference

### H6. Token entity tích lũy vô hạn + không có scheduler dọn dẹp
**File mới**: `Config/SchedulingConfig.java`, `Service/Class/TokenCleanupJob.java`
- `@EnableScheduling` trong SchedulingConfig
- Job chạy 03:00 hàng ngày (`@Scheduled(cron = "0 0 3 * * *")`)
- Dọn cả `Token` (access expired) và `RefreshToken` (expired hoặc revoked)
- `TokenRepository` thêm `deleteExpired()` query

## 5.3 — MEDIUM được fix

### M1. Doanh thu chỉ tính COMPLETED, bỏ sót DELIVERED
**File**: `Repository/OrderRepository.java`, `Service/Class/OrderService.java`
- Thêm 3 query `sumXxxAmountByStatuses(Collection<OrderStatus>)`
- `OrderService.REVENUE_STATUSES = {DELIVERED, COMPLETED}` (cả 2 đều đã thu tiền)
- Trước: DELIVERED không tính → báo cáo doanh thu thiếu

### M2. Validation rating + reviewText
**File**: `Model/Request/Review/CreateReview.java`, `UpdateReview.java`
- `@NotNull @Min(1) @Max(5)` cho rating
- `@Size(max = 2000)` cho reviewText
- `@NotNull` cho productId
- ReviewController thêm `@Valid`

### M9. Order confirmation email wire vào createOrder + createOrderFull
**File**: `Service/Class/OrderService.java`
- Inject `IEmailService`
- Helper `sendOrderConfirmation(order)` build vars (orderId, fullName, original/discount/shipping/total)
- Gọi ở cuối cả `createOrder` và `createOrderFull` (sau khi xóa cart)
- Template `order-confirmation.html` đã có từ đợt 3 — bây giờ đã active

### H7. POST /api/v1/orders legacy deprecate
**File**: `Controller/OrderController.java`
- Thêm `@Deprecated` javadoc + sửa message gợi ý dùng `/checkout`
- Không xóa (giữ backward compat) nhưng người dùng FE sẽ thấy hint trong response

## 5.4 — WebSecurityConfiguration: thêm rule rõ ràng

**File**: `Config/WebSecurityConfiguration.java`
Thêm các matcher tường minh thay vì để fallback vào `anyRequest().authenticated()`:
- `POST/PUT/DELETE /api/product_sizes/**` → ADMIN
- `POST /api/v1/vouchers`, `PUT/DELETE /api/v1/vouchers/**` → ADMIN
- `POST /api/notifications`, `/api/notifications/admin/**` → ADMIN

Defense-in-depth: ngoài `@PreAuthorize` ở controller, security config cũng có rule. Nếu ai quên `@PreAuthorize` ở controller mới, security config vẫn chặn.

## 5.5 — File mới và sửa (Đợt 5)

### File MỚI (3)
- `Config/SchedulingConfig.java` — bật `@EnableScheduling`
- `Service/Class/TokenCleanupJob.java` — cron 03:00 dọn expired tokens

### File SỬA (14)
**Controller** (5):
- `Controller/AuthController.java` — logout xóa access token, inject TokenRepository
- `Controller/NotificationController.java` — @PreAuthorize ADMIN cho create + adminGetAll
- `Controller/OrderController.java` — deprecate POST /api/v1/orders, message hint
- `Controller/ProductSizeController.java` — @PreAuthorize ADMIN cho POST/PUT/Bulk
- `Controller/ReviewController.java` (viết lại) — getAllReviews trả ReviewDTO, @Valid
- `Controller/VoucherController.java` (viết lại) — @PreAuthorize ADMIN cho POST/PUT/DELETE

**Service** (3):
- `Service/Class/OrderService.java` — REVENUE_STATUSES có DELIVERED + COMPLETED, inject IEmailService, helper sendOrderConfirmation
- `Service/Class/ReviewService.java` — ownership check trong updateReview
- `Service/Class/UserService.java` — changePassword revoke all refresh tokens

**Repository** (5):
- `Repository/CartDetailRepository.java` — existsByProductSize
- `Repository/OrderDetailRepository.java` — existsByProductSize
- `Repository/OrderRepository.java` — 3 query sum by Collection<status>
- `Repository/TokenRepository.java` — deleteByTokenValue, deleteExpired
- `Repository/UserVoucherRepository.java` — incrementUsedCountIfAvailable, decrementUsedCount
- `Repository/VoucherRepository.java` — incrementUsedCountIfAvailable, decrementUsedCount

**Service Class** (1):
- `Service/Class/ProductSizeService.java` — bulkUpsert soft-disable khi còn cart/order ref
- `Service/Class/VoucherService.java` — atomic check & increment, rollback dùng atomic

**Model** (2):
- `Model/Request/Review/CreateReview.java` — @NotNull @Min @Max @Size
- `Model/Request/Review/UpdateReview.java` — @Min @Max @Size

**Config** (1):
- `Config/WebSecurityConfiguration.java` — rule rõ ràng cho ProductSize, Voucher, Notification

## 5.6 — Tổng kết toàn ngày (Đợt 1 → Đợt 5)

| Đợt | Nội dung | File mới | File sửa |
|---|---|---|---|
| **1** | Fix tính tiền & voucher | 0 | ~10 |
| **2** | Bảo mật toàn diện (Critical/High/Medium) | 1 | ~30 |
| **3** | Flyway + ExceptionHandler + Refresh/Logout + Email + Shipping + Momo | 24 | ~17 |
| **4** | Refactor Interface Pattern (Dependency Inversion) | 6 | ~13 |
| **5** | Audit bổ sung: 5C + 6H + M items | 3 | ~14 |
| **Tổng cộng** | | **~34 file mới** | **~75 file sửa** |

## 5.7 — Kiểm tra cuối ngày

- [x] Đợt 1: Tính tiền chính xác, doanh thu lọc đúng status
- [x] Đợt 2: 7C + 9H + 10M security/business
- [x] Đợt 3: Foundation + Payment + Email
- [x] Đợt 4: Interface pattern đồng nhất
- [x] Đợt 5: Vá 5C + 6H + M items còn sót
- [x] Voucher race condition đã atomic
- [x] Logout xóa cả access + refresh token
- [x] Token cleanup scheduler 03:00 hàng ngày
- [x] Order confirmation email wire xong

---

# ĐỢT 6 — DỌN DEAD CODE + VOUCHER VALIDATION + DEFENSE-IN-DEPTH

## 6.1 — Xóa `CheckExpToken` job hỏng (CRITICAL bug bị bỏ sót)
**File xóa**: `Config/CheckExpToken/CheckExpToken.java`
- Cron `0 0/1 * * * *` chạy mỗi PHÚT → spam log
- Logic SAI: `findAllByExpirationIsAfter(now + 10 ngày)` → tìm token có `expiration > now + 10 ngày`
  - Access token mới hạ xuống 15 phút → expiration luôn < now + 10 ngày → **không bao giờ xóa gì cả**
- Trùng chức năng với `TokenCleanupJob` mới (đợt 5)

## 6.2 — Voucher validation đầy đủ
**File**: `Model/Request/Voucher/CreateVoucher.java`, `UpdateVoucher.java`
- `@Min(1)` cho discountValue (trước có thể tạo voucher giảm âm)
- `@PositiveOrZero` cho maxDiscount, minOrderValue, usageLimitGlobal, usageLimitPerUser
- `@Size(max = 50)` code, `@Size(max = 255)` description
- `@AssertTrue isPercentValid`: PERCENT type → discountValue ≤ 100
- `@AssertTrue isDateRangeValid`: endDate > startDate

## 6.3 — `@JsonIgnore` trên `Users.password` (defense-in-depth)
**File**: `Model/Entity/Users.java`
- Nếu lỡ có chỗ trả `Users` entity trực tiếp ra response → Jackson sẽ skip password
- Layer thứ 2 bảo vệ ngoài việc đã thay bằng DTO

## 6.4 — Clean System.out.println → SLF4J
**File**:
- `Controller/ProductController.java` — bỏ 2 println
- `Service/Class/NotificationCleanupJob.java` — viết lại với Logger + clean dead comments

Grep verify: `printStackTrace|System.out|System.err` → **0 kết quả**

## 6.5 — File đợt 6

### File xóa (1)
- `Config/CheckExpToken/CheckExpToken.java`

### File sửa (4)
- `Model/Request/Voucher/CreateVoucher.java`
- `Model/Request/Voucher/UpdateVoucher.java`
- `Model/Entity/Users.java`
- `Controller/ProductController.java`
- `Service/Class/NotificationCleanupJob.java`

## 6.6 — 🚨 CRITICAL phát hiện trễ: OrderDetailPK thiếu productSize
**File**: `Model/Entity/OrderDetailPK.java`, `Model/Entity/OrderDetail.java`, `db/migration/V3__order_detail_pk_include_productsize.sql`

**Vấn đề**:
- `OrderDetailPK` chỉ có `(order, product)`, KHÔNG có `productSize`
- `CartDetailPK` thì đúng — gồm `(cart, product, productSize)`
- → User đặt 1 đơn có cùng Product với 2 size khác nhau (vd Pizza M + Pizza L) → 1 trong 2 OrderDetail bị Hibernate **MERGE đè lên cái kia** vì cùng PK → **mất dữ liệu order**

**Fix**:
- `OrderDetailPK`: thêm field `productSize` (Integer FK), `@AllArgsConstructor`, `@NoArgsConstructor`
- `OrderDetail.productSize`: thêm `@Id` annotation
- SQL migration V3: drop PK cũ, set `product_size_id NOT NULL`, add PK mới gồm 3 cột

**Lưu ý DB cũ**: nếu có data đã sai (đơn cũ chỉ có 1 size cho cùng product) thì giữ nguyên — V3 chỉ thay đổi cấu trúc PK, không động đến row data.

---

## 5.8 — Vẫn còn chưa làm (sprint thực sự sau)

- Unit + integration test cho luồng tiền và payment
- Rate limiter toàn cục (Bucket4j)
- Redis cache (product, category, active voucher)
- Idempotency wiring (bảng đã có schema)
- Soft delete tổng thể
- Inventory log (`StockMovement`)
- `getCartTotal` realtime (hiện cached)
- Docker + docker-compose
- VNPay, ZaloPay (Momo làm khuôn)
- Push notification FCM
- Retry IPN webhook
- Báo cáo nâng cao (revenue theo period, top N)
- 2FA TOTP cho admin
- Audit log cho admin action
- N+1 query trong OrderService.toDTO (cần `@EntityGraph` hoặc JOIN FETCH)
- Hardcoded `http://localhost:8080/files/image/` trong nhiều DTO/controller — chưa thay bằng `${app.public.base-url}`


