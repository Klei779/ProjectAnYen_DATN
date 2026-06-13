package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "hopdong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHopDong")
    private Integer maHopDong;

    @OneToOne
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    @Column(name = "NgayKyHD")
    private LocalDate ngayKyHD;

    @Column(name = "NgayViet")
    private LocalDate ngayViet;

    @Column(name = "trangThai")
    private String trangThai;
}