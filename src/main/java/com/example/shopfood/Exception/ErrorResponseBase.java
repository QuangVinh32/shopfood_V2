package com.example.shopfood.Exception;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Getter

public enum ErrorResponseBase {
    NOT_FOUND(HttpStatus.NOT_FOUND, "ClassRoom không tồn tại "),
    USERNAME_Not(HttpStatus.INTERNAL_SERVER_ERROR, "Account không tồn tại"),
    USERNAME_EXISTS(HttpStatus.INTERNAL_SERVER_ERROR, "user đã tồn tại"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    Id_not(HttpStatus.BAD_REQUEST, "không có dữ liệu trong database"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"),
    LOGIN_ERR(HttpStatus.UNAUTHORIZED, "người dùng không tồn tại"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Không được phép truy cập"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi máy chủ"),
    INVALID_Room(HttpStatus.BAD_REQUEST, "không tìm thấy class room"),
    INVALID_ACCOUNT(HttpStatus.BAD_REQUEST, "Tài khoản không hợp lệ"),
    Min_Max(HttpStatus.BAD_REQUEST, "số min phải nhỏ hơn max"),
    Login_username(HttpStatus.BAD_REQUEST, "username không hợp lệ"),
    Login_password(HttpStatus.BAD_REQUEST, "password không hợp lệ"),
    DOUBLE_EMAIL_EX(HttpStatus.BAD_REQUEST, "email đăng kí  tồn tại hãy chọn email đăng kí khác "),
    DOUBLE_USERNAME_EX(HttpStatus.BAD_REQUEST, "USERNAME đăng kí  tồn tại hãy chọn USERNAME đăng kí khác "),
    Login_locked(HttpStatus.LOCKED, "nhập sai quá nhiều lần yêu cầu đăng nhập của bạn bị tạm khóa ");

    public final HttpStatus status;
    public final String message;

    private ErrorResponseBase(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
