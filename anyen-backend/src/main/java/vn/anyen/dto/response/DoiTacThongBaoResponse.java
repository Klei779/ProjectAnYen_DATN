package vn.anyen.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTacThongBaoResponse {

    private Integer id;
    private String category;
    private String type;
    private String icon;
    private String title;
    private String desc;
    private String actionText;
    private String time;
    private Boolean isNew;

    private String trangThaiThongBao;
    private String lyDoTuChoi;

    private OrderInfo order;
    private CustomerInfo customer;
    private ProductInfo product;

    private String note;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderInfo {
        private Integer id;
        private String code;
        private String date;
        private String status;
        private String payment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerInfo {
        private Integer id;
        private String name;
        private String phone;
        private String email;
        private String address;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductInfo {
        private Integer id;
        private String name;
        private String desc;
        private Integer quantity;
        private BigDecimal price;
        private String image;
    }
}