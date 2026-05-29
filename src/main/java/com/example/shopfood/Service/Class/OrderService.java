package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.DTO.OrderDetailDTO;
import com.example.shopfood.Model.DTO.OrderGetDTO;
import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Model.Request.Order.UpdateOrder;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import com.example.shopfood.Repository.*;
import com.example.shopfood.Service.IEmailService;
import com.example.shopfood.Service.IOrderService;
import com.example.shopfood.Service.IShippingFeeCalculator;
import com.example.shopfood.Specification.OrderSpecification;
import com.example.shopfood.Utils.CurrentUserUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private com.example.shopfood.Service.IVoucherService voucherService;

    @Autowired
    private  ProductSizeRepository productSizeRepository;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private IShippingFeeCalculator shippingFeeCalculator;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private PaymentRepository paymentRepository;

    private void createCodPayment(Order order) {
        Payment p = new Payment();
        p.setOrder(order);
        p.setProvider("COD");
        p.setStatus(PaymentStatus.PENDING);
        p.setAmount(order.getTotalAmount());
        paymentRepository.save(p);
    }

    private void markCodPaymentSuccess(Order order) {
        paymentRepository.findByOrder(order).stream()
                .filter(p -> "COD".equalsIgnoreCase(p.getProvider())
                        && p.getStatus() == PaymentStatus.PENDING)
                .forEach(p -> {
                    p.setStatus(PaymentStatus.SUCCESS);
                    p.setPaidAt(new Date());
                    p.setAmount(order.getTotalAmount());
                    paymentRepository.save(p);
                });
    }

    private void sendOrderConfirmation(Order order) {
        try {
            java.util.Map<String, Object> vars = new java.util.HashMap<>();
            vars.put("orderId", order.getOrderId());
            vars.put("fullName", order.getUser().getFullName());
            vars.put("originalAmount", order.getOriginalAmount());
            vars.put("discountAmount", order.getDiscountAmount());
            vars.put("shippingFee", order.getShippingFee());
            vars.put("totalAmount", order.getTotalAmount());
            emailService.sendHtml(order.getUser().getEmail(),
                    "Xác nhận đơn hàng #" + order.getOrderId(),
                    "order-confirmation", vars);
        } catch (Exception ignored) {
            // log handled in EmailService; không block luồng order
        }
    }

    private void recordStatusChange(Order order, OrderStatus from, OrderStatus to, String note) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setChangedAt(new Date());
        h.setNote(note);
        try {
            h.setChangedBy(currentUserUtil.currentUser());
        } catch (Exception ignored) { /* system change */ }
        orderStatusHistoryRepository.save(h);
    }

    private void assertOwnerOrAdmin(Order order) {
        Users me = currentUserUtil.currentUser();
        boolean isAdmin = currentUserUtil.hasRole("ADMIN");
        if (!isAdmin && !order.getUser().getUserId().equals(me.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập đơn hàng này");
        }
    }

//    @Override
//    public Page<Order> getAllOrdersPage(Pageable pageable, FilterOrder filterOrder) {
//        Specification<Order> spec = OrderSpecification.buildSpec(filterOrder);
//        return orderRepository.findAll(spec, pageable);
//    }

    @Override
    public Page<OrderGetDTO> getAllOrdersPage(Pageable pageable, FilterOrder filterOrder) {
        Specification<Order> spec = OrderSpecification.buildSpec(filterOrder);
        Page<Order> page = orderRepository.findAll(spec, pageable);
        return page.map(this::toDTO);
    }


    @Override
    public OrderDTO getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        assertOwnerOrAdmin(order);

        OrderDTO dto = new OrderDTO();
        dto.setCreatedAt(order.getCreatedAt());
        dto.setStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());

        List<OrderDetailDTO> detailDTOs = order.getOrderDetails().stream().map(detail -> {
            OrderDetailDTO detailDTO = new OrderDetailDTO();
            detailDTO.setProductName(detail.getProduct().getProductName());
            detailDTO.setQuantity(detail.getQuantity());
            detailDTO.setPrice(detail.getPrice());
            return detailDTO;
        }).collect(Collectors.toList());
        dto.setOrderDetails(detailDTOs);
        return dto;
    }

    // app voucher có hoặc không
    @Transactional
    @Override
    public void createOrder(String voucherCode) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getCartDetails().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }

        Order order = new Order();
        order.setCreatedAt(new Date());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setUser(user);
        orderRepository.save(order);
        double totalAmount = 0.0;

        // Lưu danh sách cart details để xóa sau
        List<CartDetail> cartDetails = new ArrayList<>(cart.getCartDetails());
        for (CartDetail cartDetail : cartDetails) {
            Product product = cartDetail.getProduct();
            ProductSize productSize = cartDetail.getProductSize();
            if (productSize == null) {
                throw new IllegalArgumentException(
                        "Thiếu thông tin size cho sản phẩm: " + product.getProductName()
                );
            }

            // KIỂM TRA TỒN KHO THEO SIZE
//            if (productSize.getQuantity() < cartDetail.getQuantity()) {
//                throw new IllegalArgumentException(
//                        "Không đủ số lượng sản phẩm: " + product.getProductName() +
//                                " - Size: " + productSize.getSizeName() +
//                                " (Còn lại: " + productSize.getQuantity() + ")"
//                );
//            }
//
//            // TRỪ SỐ LƯỢNG TỒN KHO THEO SIZE
//            int newQuantity = productSize.getQuantity() - cartDetail.getQuantity();
//            productSize.setQuantity(newQuantity);
//            productSizeRepository.save(productSize);

            // TRỪ TỒN KHO THEO SIZE - ATOMIC
            int updated = productSizeRepository.decreaseStock(
                    productSize.getProductSizeId(),
                    cartDetail.getQuantity()
            );

            if (updated == 0) {
                throw new IllegalArgumentException(
                        "Không đủ số lượng sản phẩm: " + product.getProductName()
                                + " - Size: " + productSize.getSizeName()
                );
            }

            // TÍNH GIÁ THEO SIZE
            double itemPrice = productSize.getPrice();
            int discount = productSize.getDiscount() != null ? productSize.getDiscount() : 0;
            double discountedPrice = itemPrice * (100 - discount) / 100.0;
            double itemTotal = discountedPrice * cartDetail.getQuantity();

            totalAmount += itemTotal;
            // TẠO ORDER DETAIL VỚI THÔNG TIN SIZE
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setProductSize(productSize);
            orderDetail.setSizeName(productSize.getSizeName().toString());
            orderDetail.setQuantity(cartDetail.getQuantity());
            orderDetail.setPrice(discountedPrice);
            orderDetail.setDiscountApplied(discount);
            orderDetailRepository.save(orderDetail);
        }

        int roundedTotal = (int) Math.round(totalAmount);
        order.setOriginalAmount(roundedTotal);
        order.setDiscountAmount(0);
        order.setTotalAmount(roundedTotal);
        orderRepository.save(order);

        // ÁP VOUCHER NẾU CÓ
        if (voucherCode != null && !voucherCode.isBlank()) {
            voucherService.applyVoucher(order.getOrderId(), voucherCode);
        }

        recordStatusChange(order, null, OrderStatus.PENDING, "Order created");

        // XÓA GIỎ HÀNG - nếu fail, rollback cả transaction (trừ kho + tạo order)
        cartDetailRepository.deleteByCartIdNative(cart.getCartId());
        cartRepository.deleteCartByIdNative(cart.getCartId());

        sendOrderConfirmation(order);
    }

    @Transactional
    @Override
    public Integer createOrderFull(Integer shippingAddressId, String voucherCode, String note,
                                   PaymentMethod paymentMethod) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ShippingAddress addr = shippingAddressRepository.findByIdAndUser(shippingAddressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Địa chỉ giao hàng không hợp lệ"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getCartDetails().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống");
        }

        PaymentMethod method = (paymentMethod != null) ? paymentMethod : PaymentMethod.COD;

        Order order = new Order();
        order.setCreatedAt(new Date());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setReceiverName(addr.getReceiverName());
        order.setReceiverPhone(addr.getReceiverPhone());
        order.setShippingAddress(buildFullAddress(addr));
        order.setNote(note);
        order.setPaymentMethod(method);
        orderRepository.save(order);

        double subtotal = 0.0;
        List<CartDetail> cartDetails = new ArrayList<>(cart.getCartDetails());
        for (CartDetail cartDetail : cartDetails) {
            Product product = cartDetail.getProduct();
            ProductSize productSize = cartDetail.getProductSize();
            if (productSize == null) {
                throw new IllegalArgumentException("Thiếu thông tin size cho sản phẩm: " + product.getProductName());
            }

            int updated = productSizeRepository.decreaseStock(
                    productSize.getProductSizeId(), cartDetail.getQuantity());
            if (updated == 0) {
                throw new IllegalArgumentException("Không đủ tồn kho: " + product.getProductName()
                        + " - " + productSize.getSizeName());
            }

            double itemPrice = productSize.getPrice();
            int discount = productSize.getDiscount() != null ? productSize.getDiscount() : 0;
            double discountedPrice = itemPrice * (100 - discount) / 100.0;
            double itemTotal = discountedPrice * cartDetail.getQuantity();
            subtotal += itemTotal;

            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setProduct(product);
            od.setProductSize(productSize);
            od.setSizeName(productSize.getSizeName().toString());
            od.setQuantity(cartDetail.getQuantity());
            od.setPrice(discountedPrice);
            od.setDiscountApplied(discount);
            orderDetailRepository.save(od);
        }

        int roundedSubtotal = (int) Math.round(subtotal);
        int shippingFee = shippingFeeCalculator.calculate(roundedSubtotal, addr.getProvince());

        order.setOriginalAmount(roundedSubtotal);
        order.setDiscountAmount(0);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(roundedSubtotal + shippingFee);
        orderRepository.save(order);

        if (voucherCode != null && !voucherCode.isBlank()) {
            // applyVoucher tự tính total = subtotal - discount + shippingFee
            voucherService.applyVoucher(order.getOrderId(), voucherCode);
        }

        recordStatusChange(order, null, OrderStatus.PENDING, "Order created");

        cartDetailRepository.deleteByCartIdNative(cart.getCartId());
        cartRepository.deleteCartByIdNative(cart.getCartId());

        if (method == PaymentMethod.COD) {
            createCodPayment(order);
        }

        sendOrderConfirmation(order);

        return order.getOrderId();
    }

    private String buildFullAddress(ShippingAddress a) {
        StringBuilder sb = new StringBuilder(a.getAddressLine());
        if (a.getWard() != null && !a.getWard().isBlank()) sb.append(", ").append(a.getWard());
        if (a.getDistrict() != null && !a.getDistrict().isBlank()) sb.append(", ").append(a.getDistrict());
        if (a.getProvince() != null && !a.getProvince().isBlank()) sb.append(", ").append(a.getProvince());
        return sb.toString();
    }




    @Override
    @Transactional
    public OrderDTO updateOrder(int orderID, UpdateOrder updateOrder) throws Exception {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new Exception("Order not found"));

        OrderStatus oldStatus = order.getOrderStatus();
        OrderStatus newStatus = updateOrder.getStatus();

        boolean isAdmin = currentUserUtil.hasRole("ADMIN");
        boolean isOwner = order.getUser().getUserId()
                .equals(currentUserUtil.currentUser().getUserId());

        // ===== STATE MACHINE — phân quyền chặt =====
        validateTransition(oldStatus, newStatus, isAdmin, isOwner);

        // Hủy đơn → hoàn kho + hoàn voucher (chỉ từ PENDING/CONFIRMED, SHIPPING admin cancel cũng OK)
        boolean restoreStock = newStatus == OrderStatus.CANCELED
                && oldStatus != OrderStatus.CANCELED
                && oldStatus != OrderStatus.COMPLETED;

        if (restoreStock) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                ProductSize productSize = orderDetail.getProductSize();
                if (productSize != null) {
                    productSize.setQuantity(productSize.getQuantity() + orderDetail.getQuantity());
                    productSizeRepository.save(productSize);
                }
            }
            if (order.getVoucher() != null) {
                voucherService.rollbackVoucher(order);
            }
        }

        // Khi COMPLETED đơn COD → đánh dấu Payment thu tiền thành công
        if (newStatus == OrderStatus.COMPLETED && order.getPaymentMethod() == PaymentMethod.COD) {
            markCodPaymentSuccess(order);
        }

        // Khi CANCELED đơn đã có Payment SUCCESS (Momo) → đánh dấu REFUNDED
        if (newStatus == OrderStatus.CANCELED) {
            paymentRepository.findByOrder(order).stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                    .forEach(p -> {
                        p.setStatus(PaymentStatus.REFUNDED);
                        paymentRepository.save(p);
                    });
        }

        order.setOrderStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        recordStatusChange(savedOrder, oldStatus, newStatus, null);

        List<OrderDetailDTO> detailDTOs = savedOrder.getOrderDetails()
                .stream()
                .map(detail -> {
                    Product product = detail.getProduct();
                    ProductSize size = detail.getProductSize();
                    OrderDetailDTO dto = new OrderDetailDTO();
                    dto.setProductName(product.getProductName());
                    dto.setQuantity(detail.getQuantity());
                    dto.setPrice(detail.getPrice());
                    if (size != null) {
                        dto.setSizeName(size.getSizeName().toString());
                        dto.setProductSizeId(size.getProductSizeId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
        return new OrderDTO(
                savedOrder.getOrderId(),
                savedOrder.getTotalAmount(),
                savedOrder.getOrderStatus(),
                savedOrder.getCreatedAt(),
                savedOrder.getUser().getFullName(),
                detailDTOs
        );
    }

    public OrderGetDTO toDTO(Order order) {
        Users u = order.getUser();
        OrderGetDTO dto = new OrderGetDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOriginalAmount(order.getOriginalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getOrderStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setFullName(u.getFullName());
        dto.setPhone(u.getPhone());
        dto.setAddress(u.getAddress());
        dto.setOrderDetails(
                order.getOrderDetails()
                        .stream()
                        .map(this::toOrderDetailDTO)
                        .toList()
        );
        return dto;
    }

    private OrderDetailDTO toOrderDetailDTO(OrderDetail od) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setProductName(od.getProduct().getProductName());
        dto.setQuantity(od.getQuantity());
        dto.setPrice(od.getPrice());
        return dto;
    }




    @Override
    public void deleteOrder(int id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        // Chỉ admin được xóa hẳn đơn (user nên dùng CANCEL thay vì delete)
        if (!currentUserUtil.hasRole("ADMIN")) {
            throw new AccessDeniedException("Chỉ admin được xóa đơn hàng");
        }
        orderRepository.delete(order);
    }

    @Override
    public Page<OrderGetDTO> getMyOrders(Pageable pageable) {
        Users me = currentUserUtil.currentUser();
        FilterOrder f = new FilterOrder();
        f.setUserId(me.getUserId());
        Specification<Order> spec = OrderSpecification.buildSpec(f);
        return orderRepository.findAll(spec, pageable).map(this::toDTO);
    }

    // ===== STATE MACHINE — phân quyền chuyển status =====
    //   PENDING   → CONFIRMED (admin / Momo IPN)
    //   PENDING   → CANCELED  (owner / admin)
    //   CONFIRMED → SHIPPING  (admin)
    //   CONFIRMED → CANCELED  (admin)
    //   SHIPPING  → COMPLETED (admin/shipper, gộp giao + thu tiền)
    //   SHIPPING  → CANCELED  (admin)
    //   COMPLETED → (terminal)
    //   CANCELED  → (terminal)
    private void validateTransition(OrderStatus from, OrderStatus to,
                                    boolean isAdmin, boolean isOwner) {
        if (from == OrderStatus.COMPLETED || from == OrderStatus.CANCELED) {
            throw new AccessDeniedException("Đơn đã ở trạng thái cuối, không sửa được");
        }
        switch (to) {
            case CANCELED:
                // user chỉ được CANCEL khi PENDING (chưa duyệt); sau đó admin only
                if (!isAdmin) {
                    if (!isOwner) throw new AccessDeniedException("Không có quyền hủy đơn này");
                    if (from != OrderStatus.PENDING) {
                        throw new AccessDeniedException("Đơn đã được xử lý, chỉ admin mới hủy được");
                    }
                }
                break;
            case CONFIRMED:
                if (!isAdmin) throw new AccessDeniedException("Chỉ admin được xác nhận đơn");
                if (from != OrderStatus.PENDING) throw new AccessDeniedException("Chỉ confirm được đơn PENDING");
                break;
            case SHIPPING:
                if (!isAdmin) throw new AccessDeniedException("Chỉ admin được đánh dấu đang giao");
                if (from != OrderStatus.CONFIRMED) throw new AccessDeniedException("Phải CONFIRMED trước khi SHIPPING");
                break;
            case COMPLETED:
                if (!isAdmin) throw new AccessDeniedException("Chỉ admin/shipper được hoàn tất đơn");
                if (from != OrderStatus.SHIPPING) throw new AccessDeniedException("Phải SHIPPING trước khi COMPLETED");
                break;
            case PENDING:
                throw new AccessDeniedException("Không thể chuyển ngược về PENDING");
            default:
                throw new AccessDeniedException("Status không hợp lệ");
        }
    }

    // ===== DOANH THU — chỉ tính đơn COMPLETED =====
    private static final java.util.Set<OrderStatus> REVENUE_STATUSES =
            java.util.EnumSet.of(OrderStatus.COMPLETED);

    @Override
    public Long getTotalRevenue() {
        return orderRepository.sumTotalAmountByStatuses(REVENUE_STATUSES);
    }

    @Override
    public Long getTotalOriginalRevenue() {
        return orderRepository.sumOriginalAmountByStatuses(REVENUE_STATUSES);
    }

    @Override
    public Long getTotalDiscount() {
        return orderRepository.sumDiscountAmountByStatuses(REVENUE_STATUSES);
    }
}
