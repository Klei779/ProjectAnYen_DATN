package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pushsubscriptiondoitac",

        indexes = {
                @Index(
                        name = "idx_push_subscription_doitac",
                        columnList = "MaDoiTac"
                )
        },

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_push_endpoint_hash",
                        columnNames = "EndpointHash"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscriptionDoiTac {

    /*
     * =====================================================
     * PRIMARY KEY
     * =====================================================
     *
     * SQL:
     * MaPush INT AUTO_INCREMENT PRIMARY KEY
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPush")
    private Integer maPush;


    /*
     * =====================================================
     * ĐỐI TÁC SỞ HỮU PUSH SUBSCRIPTION
     * =====================================================
     *
     * SQL:
     * MaDoiTac INT NOT NULL
     *
     * FOREIGN KEY:
     * pushsubscriptiondoitac.MaDoiTac
     * -> doitac.MaDoiTac
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "MaDoiTac",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_pushsubscriptiondoitac_doitac"
            )
    )
    private DoiTac doiTac;


    /*
     * =====================================================
     * ENDPOINT
     * =====================================================
     *
     * Địa chỉ Push Service của browser.
     *
     * Có thể rất dài nên dùng TEXT.
     */
    @Lob
    @Column(
            name = "Endpoint",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String endpoint;


    /*
     * =====================================================
     * ENDPOINT HASH
     * =====================================================
     *
     * SHA-256 của endpoint.
     *
     * Dùng VARCHAR(64) + UNIQUE
     * để tránh lưu trùng subscription.
     *
     * UNIQUE đã khai báo trong @Table
     * nên KHÔNG cần unique = true ở đây.
     */
    @Column(
            name = "EndpointHash",
            nullable = false,
            length = 64
    )
    private String endpointHash;


    /*
     * =====================================================
     * PUBLIC KEY CỦA BROWSER
     * =====================================================
     */
    @Column(
            name = "P256dh",
            nullable = false,
            length = 255
    )
    private String p256dh;


    /*
     * =====================================================
     * AUTH SECRET CỦA BROWSER
     * =====================================================
     */
    @Column(
            name = "Auth",
            nullable = false,
            length = 255
    )
    private String auth;


    /*
     * =====================================================
     * THỜI GIAN TẠO
     * =====================================================
     */
    @Column(
            name = "CreatedAt",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    /*
     * =====================================================
     * THỜI GIAN CẬP NHẬT
     * =====================================================
     */
    @Column(
            name = "UpdatedAt",
            nullable = false
    )
    private LocalDateTime updatedAt;


    /*
     * =====================================================
     * TỰ SET THỜI GIAN KHI INSERT
     * =====================================================
     */
    @PrePersist
    public void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }


    /*
     * =====================================================
     * TỰ SET THỜI GIAN KHI UPDATE
     * =====================================================
     */
    @PreUpdate
    public void preUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}