package com.example.shopfood.Specification;

import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> buildSpec(final FilterOrder form) {
        return (form == null) ? null : new Specification<Order>() {
            @Override
            public Predicate toPredicate(@NotNull Root<Order> root,
                                         @NotNull CriteriaQuery<?> query,
                                         @NotNull CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();

                // Tìm kiếm theo tên hoặc địa chỉ
                if (StringUtils.hasText(form.getSearch())) {
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(root.get("fullName"), "%" + form.getSearch() + "%"),
                            criteriaBuilder.like(root.get("address"), "%" + form.getSearch() + "%")
                    ));
                }

                // Lọc theo ID
                if (form.getMinId() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderId"), form.getMinId()));
                }
                if (form.getMaxId() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderId"), form.getMaxId()));
                }

                // Lọc theo tổng tiền
                if (form.getMinTotal() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("total"), form.getMinTotal()));
                }
                if (form.getMaxTotal() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("total"), form.getMaxTotal()));
                }

                // Sắp xếp theo ID
                if (Boolean.TRUE.equals(form.getIdAsc())) {
                    orders.add(criteriaBuilder.asc(root.get("orderId")));
                }
                if (Boolean.TRUE.equals(form.getIdDesc())) {
                    orders.add(criteriaBuilder.desc(root.get("orderId")));
                }

                // Sắp xếp theo tổng tiền
                if (Boolean.TRUE.equals(form.getTotalAsc())) {
                    orders.add(criteriaBuilder.asc(root.get("total")));
                }
                if (Boolean.TRUE.equals(form.getTotalDesc())) {
                    orders.add(criteriaBuilder.desc(root.get("total")));
                }

                // Thêm sắp xếp vào query nếu có
                if (!orders.isEmpty()) {
                    query.orderBy(orders);
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
