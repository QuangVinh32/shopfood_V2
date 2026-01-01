package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.DTO.OrderDetailDTO;
import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Model.Request.Order.UpdateOrder;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import com.example.shopfood.Repository.*;
import com.example.shopfood.Service.IOrderService;
import com.example.shopfood.Specification.OrderSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

    @Override
    public Page<Order> getAllOrdersPage(Pageable pageable, FilterOrder filterOrder) {
        Specification<Order> spec = OrderSpecification.buildSpec(filterOrder);
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public OrderDTO getOrderById(int id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

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
    @Override
    @Transactional
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

        int totalAmount = 0;
        for (CartDetail cartDetail : cart.getCartDetails()) {
            Product product = cartDetail.getProduct();
            if (product.getQuantity() < cartDetail.getQuantity()) {
                throw new IllegalArgumentException("Không đủ số lượng sản phẩm: " + product.getProductName());
            }

            product.setQuantity(product.getQuantity() - cartDetail.getQuantity());
            productRepository.save(product);

            totalAmount += (int) (product.getPrice() * cartDetail.getQuantity());

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQuantity(cartDetail.getQuantity());
            orderDetail.setPrice(product.getPrice());
            orderDetailRepository.save(orderDetail);
        }

        order.setTotalAmount(totalAmount);

        // Áp dụng voucher nếu có
        if (voucherCode != null && !voucherCode.isBlank()) {
            voucherService.applyVoucher(voucherCode, order);
        }

        orderRepository.save(order);

        // Xóa giỏ hàng
        for (CartDetail detail : cart.getCartDetails()) {
            cartDetailRepository.delete(detail);
        }
        cart.getCartDetails().clear();
        cartRepository.delete(cart);
    }

    @Override
    public OrderDTO updateOrder(int orderID, UpdateOrder updateOrder) throws Exception {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new Exception("Order not found"));

        if (updateOrder.getStatus() == OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PENDING) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                Product product = orderDetail.getProduct();
                product.setQuantity(product.getQuantity() + orderDetail.getQuantity());
                productRepository.save(product);
            }
        }

        order.setOrderStatus(updateOrder.getStatus());
        Order savedOrder = orderRepository.save(order);

        List<OrderDetailDTO> detailDTOs = savedOrder.getOrderDetails()
                .stream()
                .map(detail -> {
                    Product product = detail.getProduct();
                    return new OrderDetailDTO(
                            product.getProductId(),
                            product.getProductName(),
                            detail.getQuantity(),
                            detail.getPrice()
                    );
                })
                .collect(Collectors.toList());

        return new OrderDTO(
                savedOrder.getOrderId(),
                savedOrder.getTotalAmount(),
                savedOrder.getOrderStatus(),
                savedOrder.getCreatedAt(),
                savedOrder.getUser().getUserId(),
                detailDTOs
        );
    }

    @Override
    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }
}
