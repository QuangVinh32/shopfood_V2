package com.example.shopfood.Service;

import java.util.Map;

public interface IEmailService {

    /**
     * Gửi email HTML bằng template Thymeleaf. Trigger async.
     *
     * @param to          địa chỉ người nhận
     * @param subject     tiêu đề email
     * @param templateName tên template (không có prefix "email/")
     * @param variables   biến truyền vào template
     */
    void sendHtml(String to, String subject, String templateName, Map<String, Object> variables);
}
