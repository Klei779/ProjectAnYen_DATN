package vn.anyen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "combochitiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComBoChiTiet {

    @Id
    @Column(name = "ComboChiTietId")
    private Integer comboChiTietId;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @Column(name = "ComboID")
    private Integer comboId;

    @Column(name = "Loai")
    private String loai;

    @Column(name = "NoiDung")
    private String noiDung;
}
