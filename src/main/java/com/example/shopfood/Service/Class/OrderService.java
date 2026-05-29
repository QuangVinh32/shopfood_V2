package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.DTO.OrderDetailDTO;
import com.example.shopfood.Model.DTO.OrderGetDTO;
import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Model.Request.Order.UpdateOrder;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import com.example.shopfood.Repository.*;
import com.example.shopfood.Service.IOrderService;
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
    private VoucherService voucherService;

    @Autowired
    private  ProductSizeRepository productSizeRepository;

    @Autowired
    private CurrentUserUtil currentUserUtil;

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

        // XÓA GIỎ HÀNG - nếu fail, rollback cả transaction (trừ kho + tạo order)
        cartDetailRepository.deleteByCartIdNative(cart.getCartId());
        cartRepository.deleteCartByIdNative(cart.getCartId());
    }




    @Override
    @Transactional
    public OrderDTO updateOrder(int orderID, UpdateOrder updateOrder) throws Exception {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new Exception("Order not found"));

        boolean isAdmin = currentUserUtil.hasRole("ADMIN");
        boolean isOwner = order.getUser().getUserId()
                .equals(currentUserUtil.currentUser().getUserId());
        if (!isAdmin) {
            // User chỉ được tự cancel đơn của mình; các status khác yêu cầu admin
            if (!isOwner) {
                throw new AccessDeniedException("Không có quyền sửa đơn hàng này");
            }
            if (updateOrder.getStatus() != OrderStatus.CANCELED) {
                throw new AccessDeniedException("Chỉ admin được đổi sang trạng thái này");
            }
        }

        // Nếu hủy đơn hàng (từ PENDING -> CANCELLED), hoàn lại số lượng tồn kho THEO SIZE
        if (updateOrder.getStatus() == OrderStatus.CANCELED &&
                order.getOrderStatus() == OrderStatus.PENDING) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                ProductSize productSize = orderDetail.getProductSize();
                if (productSize != null) {
                    // HOÀN LẠI SỐ LƯỢNG THEO SIZE
                    productSize.setQuantity(productSize.getQuantity() + orderDetail.getQuantity());
                    productSizeRepository.save(productSize);
                }
                // KHÔNG CẦN XỬ LÝ Product vì không có quantity
            }

            // HOÀN LẠI VOUCHER (usedCount + userVoucher.usedCount)
            if (order.getVoucher() != null) {
                voucherService.rollbackVoucher(order);
            }
        }

        // Nếu từ trạng thái khác về PENDING, trừ lại số lượng THEO SIZE
        if (updateOrder.getStatus() == OrderStatus.PENDING &&
                order.getOrderStatus() != OrderStatus.PENDING) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                ProductSize productSize = orderDetail.getProductSize();
                if (productSize != null) {
                    if (productSize.getQuantity() < orderDetail.getQuantity()) {
                        throw new Exception("Không đủ số lượng tồn kho cho: " +
                                orderDetail.getProduct().getProductName() +
                                " - Size: " + productSize.getSizeName());
                    }
                    productSize.setQuantity(productSize.getQuantity() - orderDetail.getQuantity());
                    productSizeRepository.save(productSize);
                }
            }
        }

        order.setOrderStatus(updateOrder.getStatus());
        Order savedOrder = orderRepository.save(order);
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

    // ===== DOANH THU - chỉ tính đơn COMPLETED =====
    @Override
    public Long getTotalRevenue() {
        return orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);
    }

    @Override
    public Long getTotalOriginalRevenue() {
        return orderRepository.sumOriginalAmountByStatus(OrderStatus.COMPLETED);
    }

    @Override
    public Long getTotalDiscount() {
        return orderRepository.sumDiscountAmountByStatus(OrderStatus.COMPLETED);
    }
}
