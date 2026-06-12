package vn.anyen.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lichsukhachhang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuKhachHang {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "MaLichSu")
        private Integer maLichSu;

        @ManyToOne
        @JoinColumn(name = "MaKhachHang")
        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
        private KhachHang khachHang;

        @Column(name = "GiaiDoan")
        private String giaiDoan;

        @Column(name = "TrangThai")
        private String trangThai;

        @Column(name = "NoiDung")
        private String noiDung;

        @Column(name = "GhiChu")
        private String ghiChu;

        @Column(name = "ThoiGian")
        private LocalDateTime thoiGian;
}