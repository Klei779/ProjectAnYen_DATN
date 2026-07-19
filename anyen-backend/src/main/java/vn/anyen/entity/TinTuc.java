package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tintuc")
public class TinTuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTinTuc")
    private Integer maTinTuc;

    @Column(name = "TieuDe", nullable = false, length = 150)
    private String tieuDe;

    @Column(name = "TomTat", nullable = false, length = 500)
    private String tomTat;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "NoiDung", columnDefinition = "LONGTEXT")
    private String noiDung;

    @Column(name = "AnhDaiDien", length = 255)
    private String anhDaiDien;

    @Column(name = "LoaiTin", nullable = false)
    private Integer loaiTin;

    @Column(name = "TrangThai", nullable = false)
    private Integer trangThai;

    @Column(name = "NgayDang")
    private LocalDateTime ngayDang;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.ngayDang == null) {
            this.ngayDang = now;
        }

        if (this.trangThai == null) {
            this.trangThai = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}