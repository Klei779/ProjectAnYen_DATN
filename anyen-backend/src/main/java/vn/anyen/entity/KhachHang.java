package vn.anyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KhachHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maKhachHang;

    private String tenKhachHang;

    private String cccd;

    private String diaChi;

    private String email;

    private String soDienThoai;
}