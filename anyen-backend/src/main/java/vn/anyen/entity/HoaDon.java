package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "HoaDon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maHoaDon;

    @OneToOne
    @JoinColumn(name = "MaDonHang")
    private DonHang donHang;

    private LocalDate ngayIn;
}