package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "combochitiet_hinhanh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComBoChiTietHinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnh")
    private Integer maHinhAnh;

    @Column(name = "ComboChiTietId")
    private Integer comboChiTietId;

    @Column(name = "TenHinhAnh")
    private String tenHinhAnh;

    @Column(name = "HinhAnh")
    private String hinhAnh;

    @Column(name = "ThuTu")
    private Integer thuTu;
}