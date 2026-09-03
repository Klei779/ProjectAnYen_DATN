package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LichSuGiaoDichDoiTac")
@Data
public class LichSuGiaoDichDoiTac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiaoDich")
    private Integer maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDoiTac", nullable = false)
    private DoiTac doiTac;

    // "VI" hoặc "QUY"
    @Column(name = "LoaiVi", length = 50, nullable = false)
    private String loaiVi;

    // "+" hoặc "-"
    @Column(name = "LoaiGiaoDich", length = 50, nullable = false)
    private String loaiGiaoDich;

    @Column(name = "SoTien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "NoiDung", columnDefinition = "TEXT")
    private String noiDung;

    @CreationTimestamp
    @Column(name = "ThoiGian", nullable = false, updatable = false)
    private LocalDateTime thoiGian;
}
