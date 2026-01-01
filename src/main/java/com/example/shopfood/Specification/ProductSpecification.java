package com.example.shopfood.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Request.Product.FilterProduct;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> buildSpec(final FilterProduct form) {
        return form == null ? null : (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(form.getSearch())) {
                predicates.add(builder.like(root.get("productName"), "%" + form.getSearch() + "%"));
            }

            if (form.getPriceMin() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), form.getPriceMin()));
            }

            if (form.getPriceMax() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), form.getPriceMax()));
            }

            if (Boolean.TRUE.equals(form.getPriceAsc())) {
                query.orderBy(builder.asc(root.get("price")));
            }

            if (Boolean.TRUE.equals(form.getPriceDesc())) {
                query.orderBy(builder.desc(root.get("price")));
            }

            if (form.getCategoryId() != null) {
                predicates.add(builder.equal(root.get("category").get("categoryId"), form.getCategoryId()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
