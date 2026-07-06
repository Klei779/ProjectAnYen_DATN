<template>
  <div class="xac-nhan-container">
    <div class="container-box">
      <div class="header">
        <h1>An Yên</h1>
      </div>

      <!-- VIEW 0: ĐANG XỬ LÝ -->
      <div v-if="isLoading" class="view-section active">
        <div class="icon-circle" style="background-color: #f59e0b;">⏳</div>
        <div class="title">Đang xác thực thông tin...</div>
        <div class="subtitle">Vui lòng đợi trong giây lát.</div>
      </div>

      <!-- VIEW 1: XÁC NHẬN THẤT BẠI -->
      <div v-else-if="errorMessage" class="view-section active">
        <div class="icon-circle" style="background-color: #dc2626;">❌</div>
        <div class="title" style="color: #dc2626;">Xác Nhận Thất Bại</div>
        <div class="subtitle">Không thể xử lý yêu cầu xác nhận</div>
        
        <div class="error-message-box">{{ errorMessage }}</div>

        <p class="message" style="margin-top: 15px;">
            Rất tiếc, liên kết xác nhận hợp tác không hợp lệ hoặc đã được sử dụng trước đó.
            Vui lòng liên hệ với bộ phận quản lý của <strong class="brand">An Yên</strong>
            để được hỗ trợ thêm.
        </p>
      </div>

      <!-- VIEW 2: XÁC NHẬN THÀNH CÔNG -->
      <div v-else-if="step === 2" class="view-section active">
        <div class="icon-circle">✓</div>
        <div class="title">Xác nhận hợp tác thành công!</div>
        <div class="subtitle">
            Cảm ơn Quý Đối tác <strong>{{ partnerData.tenDoiTac }}</strong> đã đồng ý hợp tác cùng An Yên.<br>
            Thông tin lời mời đã được ghi nhận.
        </div>

        <div class="steps-box">
            <h3>Các bước tiếp theo</h3>
            <div class="step-item"><div class="step-number">1</div> Xem và xác nhận hợp đồng hợp tác</div>
            <div class="step-item"><div class="step-number">2</div> Hoàn thiện thông tin doanh nghiệp</div>
            <div class="step-item"><div class="step-number">3</div> Chờ An Yên phê duyệt tài khoản</div>
            <div class="step-item"><div class="step-number">4</div> Bắt đầu đăng sản phẩm và nhận đơn hàng</div>
        </div>

        <button class="btn" @click="showModal = true">Xem hợp đồng</button>
      </div>

      <!-- VIEW 4: HOÀN TẤT KÝ KẾT -->
      <div v-else-if="step === 4" class="view-section active">
        <div class="icon-circle" style="background-color: #1e3a8a;">📄</div>
        <div class="title">Chúc mừng!</div>
        <div class="subtitle">
            Quý Đối tác đã hoàn tất thủ tục hợp tác với An Yên.
        </div>

        <div class="code-box">Mã đối tác: <span>{{ partnerData.maDoiTac }}</span></div>

        <div class="subtitle">
            Tài khoản của Quý Đối tác đang được xem xét.<br>
            Thời gian phê duyệt dự kiến: <strong>24 giờ</strong>.
        </div>

        <router-link to="/doi-tac" class="btn">Đăng nhập hệ thống</router-link>
        
        <div style="margin-top: 15px; font-size: 12px; color: #64748b;">
            Chưa có tài khoản? Liên hệ An Yên để được hỗ trợ.
        </div>
      </div>

      <!-- MODAL HỢP ĐỒNG -->
      <div v-if="showModal" class="modal-overlay active">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Hợp đồng hợp tác</h2>
                <p>GIỮA AN YÊN VÀ ĐỐI TÁC</p>
            </div>
            <div class="modal-body">
                <div class="contract-text">
                    <strong>Căn cứ pháp lý:</strong><br>
                    - Bộ luật Dân sự 2015;<br>
                    - Luật Thương mại 2005;<br>
                    - Các văn bản pháp luật có liên quan.<br><br>
                    <strong>Điều 1. Nội dung hợp tác</strong><br>
                    1. An Yên và Đối tác thống nhất hợp tác trên nguyên tắc bình đẳng, cùng có lợi và tuân thủ quy định pháp luật.<br>
                    2. Đối tác cung cấp sản phẩm, dịch vụ trong lĩnh vực tang lễ thông qua nền tảng An Yên.<br>
                    3. An Yên hỗ trợ quảng bá, kết nối khách hàng và hệ thống.<br><br>
                    <strong>Điều 2. Quyền và nghĩa vụ</strong><br>
                    - Cung cấp nền tảng công nghệ.<br>
                    - Hỗ trợ quảng bá tiếp cận khách hàng.
                </div>

                <div style="font-weight: bold; margin-bottom: 10px; color: #1e3a8a;">Tạo tài khoản đăng nhập hệ thống:</div>
                <div v-if="submitError" class="error-msg">{{ submitError }}</div>

                <div class="form-group">
                    <label>Tên đăng nhập <span style="color:red">*</span></label>
                    <input type="text" v-model="form.username" class="form-control" placeholder="Nhập tên đăng nhập">
                </div>
                <div class="form-group">
                    <label>Mật khẩu <span style="color:red">*</span></label>
                    <input type="password" v-model="form.password" class="form-control" placeholder="Nhập mật khẩu (từ 6 ký tự)">
                </div>

                <div class="checkbox-group">
                    <input type="checkbox" id="agree-checkbox" v-model="form.agree">
                    <label for="agree-checkbox">Tôi đã đọc và đồng ý với các điều khoản hợp tác</label>
                </div>

                <button class="btn" :disabled="!form.agree || isSubmitting" @click="submitContract">
                  {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN KÝ KẾT' }}
                </button>
                <button class="btn btn-close" @click="closeModal">Đóng lại</button>
            </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { verifyDoiTacToken, kyHopDongDoiTac } from '../services/QuanLyDoiTacService.js';
import { useRoute } from 'vue-router';

export default {
  name: 'XacNhanDoiTac',
  data() {
    return {
      isLoading: true,
      errorMessage: null,
      step: 0,
      showModal: false,
      token: '',
      partnerData: {
        tenDoiTac: '',
        tenDoanhNghiep: '',
        maDoiTac: ''
      },
      form: {
        username: '',
        password: '',
        agree: false
      },
      submitError: null,
      isSubmitting: false
    };
  },
  mounted() {
    const route = useRoute();
    this.token = this.$route.query.token || '';
    if (!this.token) {
      this.errorMessage = "Không tìm thấy mã xác nhận trong liên kết.";
      this.isLoading = false;
      return;
    }
    this.verifyToken();
  },
  methods: {
    async verifyToken() {
      try {
        const response = await verifyDoiTacToken(this.token);
        this.partnerData = response.data;
        this.step = 2;
      } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = "Có lỗi xảy ra khi xác thực liên kết.";
        }
      } finally {
        this.isLoading = false;
      }
    },
    closeModal() {
      this.showModal = false;
      this.submitError = null;
    },
    async submitContract() {
      if (!this.form.username || this.form.username.length < 4) {
        this.submitError = "Tên đăng nhập phải từ 4 ký tự.";
        return;
      }
      if (!this.form.password || this.form.password.length < 6) {
        this.submitError = "Mật khẩu phải từ 6 ký tự.";
        return;
      }

      this.isSubmitting = true;
      this.submitError = null;

      try {
        const payload = {
          token: this.token,
          tenDangNhap: this.form.username,
          matKhau: this.form.password
        };
        await kyHopDongDoiTac(payload);
        
        // Success
        this.closeModal();
        this.step = 4;
      } catch (error) {
        if (error.response && error.response.data) {
           let msg = "Có lỗi xảy ra.";
           const data = error.response.data;
           if (data.message) msg = data.message;
           else if (data.tenDangNhap) msg = data.tenDangNhap;
           else if (data.matKhau) msg = data.matKhau;
           this.submitError = msg;
        } else {
           this.submitError = "Lỗi kết nối máy chủ. Vui lòng thử lại sau.";
        }
      } finally {
        this.isSubmitting = false;
      }
    }
  }
};
</script>

<style scoped>
.xac-nhan-container {
    font-family: 'Segoe UI', Arial, sans-serif;
    background-color: #f8fafc;
    color: #334155;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    padding: 20px;
}
.container-box {
    background: #ffffff;
    width: 100%;
    max-width: 500px;
    min-height: 600px;
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0,0,0,0.05);
    padding: 40px 30px;
    position: relative;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}
.header {
    text-align: center;
    margin-bottom: 30px;
}
.header h1 {
    font-family: 'Times New Roman', serif;
    font-style: italic;
    color: #1e3a8a;
    font-size: 36px;
    margin-bottom: 5px;
}

/* Views */
.view-section { display: none; text-align: center; }
.view-section.active { display: block; animation: fadeIn 0.4s ease; }

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.icon-circle {
    width: 70px; height: 70px;
    background-color: #1e3a8a;
    color: white;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    margin-bottom: 20px;
    margin-left: auto;
    margin-right: auto;
}

.title { font-size: 22px; font-weight: bold; color: #1e3a8a; margin-bottom: 10px; }
.subtitle { font-size: 14px; color: #64748b; line-height: 1.5; margin-bottom: 25px; }

/* Step 2 specific */
.steps-box {
    background-color: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 20px;
    text-align: left;
    margin-bottom: 30px;
}
.steps-box h3 { font-size: 16px; color: #1e3a8a; text-align: center; margin-bottom: 15px; }
.step-item { display: flex; align-items: center; margin-bottom: 12px; font-size: 14px; color: #475569; }
.step-number {
    width: 24px; height: 24px;
    background-color: #1e3a8a; color: white;
    border-radius: 50%; display: flex; align-items: center; justify-content: center;
    font-size: 12px; font-weight: bold; margin-right: 12px; flex-shrink: 0;
}

/* Modal */
.modal-overlay {
    position: absolute; top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,0.4);
    display: none; align-items: center; justify-content: center;
    z-index: 10;
}
.modal-overlay.active { display: flex; animation: fadeIn 0.3s; }
.modal-content {
    background: white; width: 90%; max-height: 90%;
    border-radius: 8px; display: flex; flex-direction: column;
    overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}
.modal-header {
    padding: 15px 20px; border-bottom: 1px solid #e2e8f0; text-align: center;
}
.modal-header h2 { font-size: 18px; color: #b91c1c; text-transform: uppercase; margin-bottom: 5px; }
.modal-header p { font-size: 13px; color: #64748b; margin: 0; }

.modal-body {
    padding: 20px; overflow-y: auto; flex: 1;
    font-size: 13px; color: #334155; line-height: 1.6;
}
.contract-text {
    background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px;
    padding: 15px; height: 150px; overflow-y: auto; margin-bottom: 20px;
    text-align: left;
}

.form-group { margin-bottom: 15px; text-align: left; }
.form-group label { display: block; font-size: 13px; font-weight: bold; margin-bottom: 5px; color: #475569;}
.form-control {
    width: 100%; padding: 10px; border: 1px solid #cbd5e1;
    border-radius: 6px; font-size: 14px; outline: none; box-sizing: border-box;
}
.form-control:focus { border-color: #1e3a8a; }

.checkbox-group {
    display: flex; align-items: flex-start; margin-bottom: 20px; text-align: left;
}
.checkbox-group input { margin-top: 3px; margin-right: 10px; cursor: pointer; }
.checkbox-group label { font-size: 13px; cursor: pointer; color: #1e293b; font-weight: 500;}

.btn {
    display: block; width: 100%; padding: 14px;
    background-color: #1e3a8a; color: white;
    border: none; border-radius: 6px;
    font-size: 15px; font-weight: bold; cursor: pointer;
    text-transform: uppercase; transition: background 0.2s;
    text-decoration: none; text-align: center; box-sizing: border-box;
}
.btn:hover { background-color: #172554; }
.btn:disabled { background-color: #94a3b8; cursor: not-allowed; }
.btn-close { background: transparent; color: #64748b; margin-top: 10px; }
.btn-close:hover { background: #f1f5f9; color: #334155;}

/* Step 4 specific */
.code-box {
    display: inline-block; padding: 12px 30px;
    border: 2px dashed #cbd5e1; border-radius: 8px;
    font-size: 18px; font-weight: bold; color: #1e3a8a;
    margin-bottom: 20px;
}
.error-msg {
    color: #dc2626; font-size: 13px; text-align: left; margin-bottom: 15px; display: block;
}
.error-message-box {
    display: inline-block;
    padding: 12px 20px;
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 10px;
    color: #991b1b;
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 20px;
    line-height: 1.5;
}
.brand { font-weight: 600; color: #1a5632; }
</style>
