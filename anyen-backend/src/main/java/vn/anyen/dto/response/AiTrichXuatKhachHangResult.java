package vn.anyen.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AiTrichXuatKhachHangResult {

    private String reply;
    private CustomerInfo customerInfo;
    private List<String> missingFields = new ArrayList<>();
    private Boolean readyForHotline = false;
    private Boolean customerConfirmed = false;
    private Boolean humanTakeover = false;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields == null
                ? new ArrayList<>()
                : missingFields;
    }

    public Boolean getReadyForHotline() {
        return readyForHotline;
    }

    public void setReadyForHotline(Boolean readyForHotline) {
        this.readyForHotline = readyForHotline;
    }

    public Boolean getCustomerConfirmed() {
        return customerConfirmed;
    }

    public void setCustomerConfirmed(Boolean customerConfirmed) {
        this.customerConfirmed = customerConfirmed;
    }

    public Boolean getHumanTakeover() {
        return humanTakeover;
    }

    public void setHumanTakeover(Boolean humanTakeover) {
        this.humanTakeover = humanTakeover;
    }

    public static class CustomerInfo {

        private String hoTen;
        private String soDienThoai;
        private String diaChi;
        private String nhuCau;
        private String thoiGianMongMuon;
        private BigDecimal nganSachDuKien;
        private String ghiChu;

        public String getHoTen() {
            return hoTen;
        }

        public void setHoTen(String hoTen) {
            this.hoTen = hoTen;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }

        public String getNhuCau() {
            return nhuCau;
        }

        public void setNhuCau(String nhuCau) {
            this.nhuCau = nhuCau;
        }

        public String getThoiGianMongMuon() {
            return thoiGianMongMuon;
        }

        public void setThoiGianMongMuon(String thoiGianMongMuon) {
            this.thoiGianMongMuon = thoiGianMongMuon;
        }

        public BigDecimal getNganSachDuKien() {
            return nganSachDuKien;
        }

        public void setNganSachDuKien(BigDecimal nganSachDuKien) {
            this.nganSachDuKien = nganSachDuKien;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }
    }
}
