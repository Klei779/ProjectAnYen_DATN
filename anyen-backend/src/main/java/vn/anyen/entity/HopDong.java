package vn.anyen.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "HopDong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HopDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maHopDong;

    @OneToOne
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    private LocalDate ngayKyHD;

    private LocalDate ngayViet;

    private String trangThai;
}