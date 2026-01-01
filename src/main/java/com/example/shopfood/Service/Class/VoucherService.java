package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import com.example.shopfood.Repository.OrderRepository;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Repository.UserVoucherRepository;
import com.example.shopfood.Repository.VoucherRepository;
import com.example.shopfood.Service.IVoucherService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService implements IVoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public Voucher createVoucher(CreateVoucher request) {
        Voucher voucher = getVoucher(request);
        Voucher savedVoucher = voucherRepository.save(voucher);

        // Gán voucher cho danh sách user được chọn
        if (request.getUserIds() != null) {
            for (Integer userId : request.getUserIds()) {
                Users user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User không tồn tại ID: " + userId));

                UserVoucher uv = new UserVoucher();
                uv.setUser(user);
                uv.setVoucher(savedVoucher);
                uv.setAssignedAt(new Date());
                userVoucherRepository.save(uv);
            }
        }
        return savedVoucher;
    }

    private static Voucher getVoucher(CreateVoucher request) {
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setStatus(request.getStatus());
        voucher.setStartDate(new Date());

        Date now = new Date();
        voucher.setStartDate(now);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        voucher.setEndDate(calendar.getTime());
        return voucher;
    }

    // Mỗi người dùng chỉ có thể apply mot số voucher nhất định
    // Khi dùng voucher thì sẽ trừ đi số voucher mà đã sử dụng
    // Kiểm tra voucher đó còn hạn sử dụng nữa hay không

    @Override
    public List<Voucher> getAll() {
        return voucherRepository.findAll();
    }

    @Override
    @Transactional
    public void applyVoucher(String code, Order order) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại."));

        // Kiểm tra hết hạn
        if (new Date().after(voucher.getEndDate())) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            voucherRepository.save(voucher);
            throw new RuntimeException("Voucher đã hết hạn.");
        }

        // Kiểm tra trạng thái
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new RuntimeException("Voucher không khả dụng.");
        }

        // Kiểm tra lượt sử dụng
        if (voucher.getUsageLimit() <= 0) {
            voucher.setStatus(VoucherStatus.DISABLED);
            voucherRepository.save(voucher);
            throw new RuntimeException("Voucher đã hết lượt sử dụng.");
        }

        // Gán voucher vào đơn hàng
        order.setVoucher(voucher);

        // Trừ lượt sử dụng
        voucher.setUsageLimit(voucher.getUsageLimit() - 1);

        // Giảm giá tiền cho đơn hàng
        Integer originalAmount = order.getTotalAmount();
        int discountValue = voucher.getDiscountValue();

        int finalAmount = Math.max(0, originalAmount - discountValue);
        order.setTotalAmount(finalAmount);

        // Lưu lại cả voucher và order
        voucherRepository.save(voucher);
        orderRepository.save(order);

    }

    @Override
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    @Override
    public void deleteVoucher(Integer id) {
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Voucher không tồn tại.");
        }
        voucherRepository.deleteById(id);
    }
}


