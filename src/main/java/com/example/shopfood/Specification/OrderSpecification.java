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
                                         @NotNull CriteriaBuilder cb) {

                List<Predicate> predicates = new ArrayList<>();
                List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();

                if (StringUtils.hasText(form.getSearch())) {
                    String like = "%" + form.getSearch() + "%";
                    predicates.add(cb.or(
                            cb.like(root.get("user").get("fullName"), like),
                            cb.like(root.get("user").get("address"), like)
                    ));
                }

                if (form.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("userId"), form.getUserId()));
                }

                if (form.getMinId() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("orderId"), form.getMinId()));
                }
                if (form.getMaxId() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("orderId"), form.getMaxId()));
                }

                if (form.getMinTotal() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), form.getMinTotal()));
                }
                if (form.getMaxTotal() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), form.getMaxTotal()));
                }

                if (Boolean.TRUE.equals(form.getIdAsc()))    orders.add(cb.asc(root.get("orderId")));
                if (Boolean.TRUE.equals(form.getIdDesc()))   orders.add(cb.desc(root.get("orderId")));
                if (Boolean.TRUE.equals(form.getTotalAsc()))  orders.add(cb.asc(root.get("totalAmount")));
                if (Boolean.TRUE.equals(form.getTotalDesc())) orders.add(cb.desc(root.get("totalAmount")));

                if (!orders.isEmpty()) {
                    query.orderBy(orders);
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
