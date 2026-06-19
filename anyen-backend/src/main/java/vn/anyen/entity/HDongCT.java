package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hdongct")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HDongCT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHDongCT")
    private Integer maHDongCT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHopDong", nullable = false)
    private HopDong hopDong;

    @Column(name = "HoTenNguoiMat")
    private String hoTenNguoiMat;

    @Column(name = "NgayMat")
    private LocalDate ngayMat;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh")
    private String gioiTinh;

    @Column(name = "SoGiayBaoTu")
    private String soGiayBaoTu;

    @Column(name = "NoiCapGiayBaoTu")
    private String noiCapGiayBaoTu;

    @Column(name = "CoSoMaiTang")
    private String coSoMaiTang;

    @Column(name = "KhuMo")
    private String khuMo;

    @Column(name = "SoMo")
    private String soMo;

    @Column(name = "NgayGioAnTang")
    private LocalDateTime ngayGioAnTang;
}