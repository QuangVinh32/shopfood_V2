package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.DTO.VoucherApplyResult;
import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import com.example.shopfood.Model.Request.Voucher.UpdateVoucher;
import com.example.shopfood.Repository.OrderRepository;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Repository.UserVoucherRepository;
import com.example.shopfood.Repository.VoucherRepository;
import com.example.shopfood.Service.IVoucherService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoucherService implements IVoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ================= CREATE =================
    @Override
    @Transactional
    public Voucher createVoucher(CreateVoucher request) {

        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setUsageLimitGlobal(request.getUsageLimitGlobal());
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        voucher.setTarget(request.getTarget());
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher = voucherRepository.save(voucher);

        if (request.getTarget() == VoucherTarget.USER && request.getUserIds() != null) {
            for (Integer userId : request.getUserIds()) {
                Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại"));

                if (!userVoucherRepository.existsByUserAndVoucher(user, voucher)) {
                    UserVoucher uv = new UserVoucher();
                    uv.setUser(user);
                    uv.setVoucher(voucher);
                    uv.setAssignedAt(new Date());
                    userVoucherRepository.save(uv);
                }
            }
        }
        return voucher;
    }


    @Override
    @Transactional
    public Voucher updateVoucher(Integer voucherId, UpdateVoucher request) {

        Voucher voucher = voucherRepository.findById(voucherId).orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // ===== UPDATE FIELD =====
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setUsageLimitGlobal(request.getUsageLimitGlobal());
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        voucher.setTarget(request.getTarget());
        voucher.setStatus(request.getStatus());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());

        // ===== HANDLE USER VOUCHER =====
        if (request.getTarget() == VoucherTarget.USER) {

            List<UserVoucher> existingUVs = userVoucherRepository.findAllByVoucher(voucher);

            // danh sách user mới
            List<Integer> newUserIds = request.getUserIds();

            // 1️⃣ XÓA user không còn
            for (UserVoucher uv : existingUVs) {
                if (!newUserIds.contains(uv.getUser().getUserId())) {
                    userVoucherRepository.delete(uv);
                }
            }

            // 2️⃣ THÊM user mới chưa có
            for (Integer userId : newUserIds) {

                Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại"));

                boolean exists = existingUVs.stream().anyMatch(uv -> uv.getUser().getUserId().equals(userId));

                if (!exists) {
                    UserVoucher uv = new UserVoucher();
                    uv.setUser(user);
                    uv.setVoucher(voucher);
                    uv.setAssignedAt(new Date());
                    uv.setUsedCount(0);
                    userVoucherRepository.save(uv);
                }
            }

        } else {
            // Nếu đổi USER → PUBLIC thì xóa hết user_voucher
            userVoucherRepository.deleteByVoucher(voucher);
        }

        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher getValidVoucher(String voucherCode) {
        return null;
    }


    // ================= APPLY =================
    @Override
    @Transactional
    public VoucherApplyResult applyVoucher(Integer orderId, String code) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByFullName(fullName).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!order.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Không có quyền áp voucher");

        if (order.getVoucher() != null) throw new RuntimeException("Order đã áp voucher");

        Voucher voucher = voucherRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        Date now = new Date();

        // ===== CHECK CƠ BẢN =====
        if (now.before(voucher.getStartDate()) || now.after(voucher.getEndDate()))
            throw new RuntimeException("Voucher hết hạn");

        if (voucher.getStatus() != VoucherStatus.ACTIVE) throw new RuntimeException("Voucher không khả dụng");

        if (voucher.getMinOrderValue() != null && order.getTotalAmount() < voucher.getMinOrderValue())
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu");

        if (voucher.getUsageLimitGlobal() != null && voucher.getUsedCount() >= voucher.getUsageLimitGlobal())
            throw new RuntimeException("Voucher đã hết lượt sử dụng");

        // ===== CHECK THEO USER (CHO CẢ USER + ALL) =====
        UserVoucher userVoucher = userVoucherRepository.findByUserAndVoucher(user, voucher).orElse(null);

        if (voucher.getUsageLimitPerUser() != null) {

            if (userVoucher != null && userVoucher.getUsedCount() >= voucher.getUsageLimitPerUser()) {
                throw new RuntimeException("Bạn đã dùng hết lượt voucher này");
            }
        }

        // ===== TÍNH GIẢM GIÁ =====
        int originalAmount = order.getTotalAmount();
        int discount;

        if (voucher.getDiscountType() == DiscountType.FIXED) {
            discount = voucher.getDiscountValue();
        } else {
            discount = originalAmount * voucher.getDiscountValue() / 100;
            if (voucher.getMaxDiscount() != null) discount = Math.min(discount, voucher.getMaxDiscount());
        }

        int finalAmount = Math.max(0, originalAmount - discount);

        // ===== UPDATE ORDER =====
        order.setVoucher(voucher);
        order.setTotalAmount(finalAmount);

        // ===== UPDATE VOUCHER =====
        voucher.setUsedCount(voucher.getUsedCount() + 1);

        // ===== UPDATE USER_VOUCHER =====
        if (userVoucher == null) {
            userVoucher = new UserVoucher();
            userVoucher.setUser(user);
            userVoucher.setVoucher(voucher);
            userVoucher.setAssignedAt(now);
            userVoucher.setUsedCount(1);
        } else {
            userVoucher.setUsedCount(userVoucher.getUsedCount() + 1);
        }

        userVoucher.setLastUsedAt(now);

        userVoucherRepository.save(userVoucher);
        orderRepository.save(order);
        voucherRepository.save(voucher);

        return new VoucherApplyResult(originalAmount, discount, finalAmount);
    }


    // ================= OTHERS =================
    @Override
    public List<Voucher> getAllForAdmin() {
        return voucherRepository.findAll();
    }

    @Override
    public List<Voucher> getAllForUsers() {

        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByFullName(fullName).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Date now = new Date();

        List<Voucher> vouchers = voucherRepository.findByStatus(VoucherStatus.ACTIVE);

        return vouchers.stream().filter(voucher -> {

            // 1️⃣ Check thời gian
            if (now.before(voucher.getStartDate()) || now.after(voucher.getEndDate())) return false;

            // 2️⃣ Check global usage
            if (voucher.getUsageLimitGlobal() != null && voucher.getUsedCount() >= voucher.getUsageLimitGlobal())
                return false;

            // 3️⃣ Nếu voucher cho USER
            if (voucher.getTarget() == VoucherTarget.USER) {

                Optional<UserVoucher> uvOpt = userVoucherRepository.findByUserAndVoucher(user, voucher);

                if (uvOpt.isEmpty()) return false;

                UserVoucher uv = uvOpt.get();

                if (voucher.getUsageLimitPerUser() != null && uv.getUsedCount() >= voucher.getUsageLimitPerUser())
                    return false;
            }
            return true;
        }).toList();
    }

    @Override
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    @Override
    public void deleteVoucher(Integer id) {

        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        if (voucher.getUsedCount() > 0) {
            throw new RuntimeException("Không thể xóa voucher đã được sử dụng");
        }

        userVoucherRepository.deleteByVoucher(voucher);
        voucherRepository.delete(voucher);
    }


}
