<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const token = ref('');
const formData = ref({
    tenDoiTac: '',
    tenDoanhNghiep: '',
    maSoThue: '',
    tenDangNhap: '',
    matKhau: '',
    soDienThoai: '',
    diaChi: ''
});

// For demonstration, email might come from token decoding, but we display a placeholder or empty if we don't decode it here.
const displayEmail = ref('');

const isTermsRead = ref(false);
const isAcceptedTerms = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const isSubmitting = ref(false);
const showPassword = ref(false);

const termsContent = ref(null);

onMounted(() => {
    token.value = route.query.token;
    if (!token.value) {
        errorMessage.value = 'Liên kết không hợp lệ hoặc thiếu Token. Vui lòng kiểm tra lại email của bạn.';
    }
});

const handleTermsScroll = () => {
    const el = termsContent.value;
    if (!el) return;
    
    // Check if scrolled to bottom (allow 5px margin of error)
    if (el.scrollHeight - el.scrollTop - el.clientHeight <= 5) {
        isTermsRead.value = true;
    }
};

const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value;
};

const submitRegistration = async () => {
    if (!token.value) return;

    if (!isTermsRead.value) {
        errorMessage.value = 'Vui lòng đọc hết điều khoản trước khi đồng ý.';
        return;
    }

    if (!isAcceptedTerms.value) {
        errorMessage.value = 'Vui lòng đồng ý với Điều khoản và Điều kiện.';
        return;
    }

    if (!formData.value.tenDoiTac || !formData.value.tenDangNhap || !formData.value.matKhau) {
        errorMessage.value = 'Vui lòng nhập đầy đủ các thông tin bắt buộc.';
        return;
    }

    try {
        isSubmitting.value = true;
        errorMessage.value = '';
        
        await axios.post('http://localhost:8080/api/quanly-doitac/register', {
            token: token.value,
            ...formData.value
        });
        
        successMessage.value = 'Hồ sơ đối tác đã được đăng ký thành công! Đang chuyển hướng...';
        setTimeout(() => {
            router.push('/'); 
        }, 3000);
    } catch (error) {
        if (error.response?.data && typeof error.response.data === 'string') {
            errorMessage.value = error.response.data;
        } else if (error.response?.data?.message) {
            errorMessage.value = error.response.data.message;
        } else {
            errorMessage.value = 'Có lỗi xảy ra khi đăng ký. Vui lòng kiểm tra lại thông tin.';
        }
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<template>
    <div class="page-wrapper">
        <!-- Header -->
        <header class="header bg-white py-3 shadow-sm">
            <div class="container d-flex justify-content-between align-items-center">
                <div class="logo">
                    <!-- Temporary logo text if image is not accessible -->
                    <div class="brand-logo">
                        <span class="an text-danger">An</span><span class="yen">Yên</span>
                    </div>
                    <div class="brand-slogan text-primary">NƠI GỬI TRỌN NIỀM TIN</div>
                </div>
                
                <div class="header-features d-none d-lg-flex gap-4">
                    <div class="feature-item text-center">
                        <div class="feature-icon text-danger mb-1"><i class="bi bi-handshake"></i></div>
                        <div class="feature-text">Hợp tác lâu dài<br>cùng phát triển</div>
                    </div>
                    <div class="feature-item text-center">
                        <div class="feature-icon text-danger mb-1"><i class="bi bi-megaphone"></i></div>
                        <div class="feature-text">Hỗ trợ truyền thông<br>quảng bá thương hiệu</div>
                    </div>
                    <div class="feature-item text-center">
                        <div class="feature-icon text-danger mb-1"><i class="bi bi-box-seam"></i></div>
                        <div class="feature-text">Sản phẩm đa dạng<br>chất lượng</div>
                    </div>
                    <div class="feature-item text-center">
                        <div class="feature-icon text-danger mb-1"><i class="bi bi-headset"></i></div>
                        <div class="feature-text">Chính sách ưu đãi<br>hỗ trợ tận tâm</div>
                    </div>
                </div>
            </div>
        </header>

        <main class="main-content py-5">
            <div class="container">
                <div class="text-center mb-5">
                    <p class="text-danger fw-bold mb-1 subtitle-small">CỔNG ĐĂNG KÝ ĐỐI TÁC</p>
                    <h1 class="main-title fw-bold">Chào mừng <span class="text-danger">Quý Đối tác!</span></h1>
                    <p class="text-muted">Vui lòng điền đầy đủ thông tin để hoàn tất hồ sơ đối tác<br>và cùng An Yên hợp tác kiến tạo giá trị bền vững.</p>
                </div>

                <!-- Stepper -->
                <div class="stepper-wrapper mb-5 d-none d-md-flex">
                    <div class="stepper-item active">
                        <div class="step-counter">1</div>
                        <div class="step-name">Thông tin doanh nghiệp</div>
                    </div>
                    <div class="stepper-item">
                        <div class="step-counter">2</div>
                        <div class="step-name">Thông tin liên hệ</div>
                    </div>
                    <div class="stepper-item">
                        <div class="step-counter">3</div>
                        <div class="step-name">Điều khoản & cam kết</div>
                    </div>
                    <div class="stepper-item">
                        <div class="step-counter">4</div>
                        <div class="step-name">Hoàn tất đăng ký</div>
                    </div>
                </div>

                <div class="form-wrapper bg-white p-4 p-md-5 rounded-4 shadow-sm border mx-auto" style="max-width: 900px;">
                    <div v-if="errorMessage" class="alert alert-danger d-flex align-items-center mb-4">
                        <i class="bi bi-exclamation-circle-fill me-2"></i> {{ errorMessage }}
                    </div>
                    <div v-if="successMessage" class="alert alert-success d-flex align-items-center mb-4">
                        <i class="bi bi-check-circle-fill me-2"></i> {{ successMessage }}
                    </div>

                    <form @submit.prevent="submitRegistration" v-if="!successMessage && token">
                        <!-- THÔNG TIN DOANH NGHIỆP -->
                        <h5 class="section-title text-danger fw-bold mb-4">THÔNG TIN DOANH NGHIỆP</h5>
                        <div class="row g-4 mb-5">
                            <div class="col-md-6">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-person"></i></div>
                                    <div class="input-content">
                                        <label>Người đại diện <span class="text-danger">*</span></label>
                                        <input type="text" v-model="formData.tenDoiTac" class="form-control" required placeholder="Nhập họ và tên người đại diện" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-telephone"></i></div>
                                    <div class="input-content">
                                        <label>Số điện thoại liên hệ</label>
                                        <input type="text" v-model="formData.soDienThoai" class="form-control" placeholder="Nhập số điện thoại" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-building"></i></div>
                                    <div class="input-content">
                                        <label>Tên doanh nghiệp (nếu có)</label>
                                        <input type="text" v-model="formData.tenDoanhNghiep" class="form-control" placeholder="Nhập tên doanh nghiệp" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-journal-text"></i></div>
                                    <div class="input-content">
                                        <label>Mã số thuế</label>
                                        <input type="text" v-model="formData.maSoThue" class="form-control" placeholder="Nhập mã số thuế" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-geo-alt"></i></div>
                                    <div class="input-content">
                                        <label>Địa chỉ kinh doanh</label>
                                        <input type="text" v-model="formData.diaChi" class="form-control" placeholder="Nhập địa chỉ kinh doanh" />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- THÔNG TIN ĐĂNG NHẬP -->
                        <h5 class="section-title text-danger fw-bold mb-4">THÔNG TIN ĐĂNG NHẬP</h5>
                        <div class="row g-4 mb-5">
                            <div class="col-md-6">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-person-badge"></i></div>
                                    <div class="input-content">
                                        <label>Tên đăng nhập <span class="text-danger">*</span></label>
                                        <input type="text" v-model="formData.tenDangNhap" class="form-control" required placeholder="Nhập tên đăng nhập" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="custom-input-group">
                                    <div class="input-icon text-danger"><i class="bi bi-lock"></i></div>
                                    <div class="input-content pe-5">
                                        <label>Mật khẩu <span class="text-danger">*</span></label>
                                        <input :type="showPassword ? 'text' : 'password'" v-model="formData.matKhau" class="form-control" required placeholder="Nhập mật khẩu" />
                                        <button type="button" class="btn-eye" @click="togglePasswordVisibility">
                                            <i class="bi text-muted" :class="showPassword ? 'bi-eye-slash' : 'bi-eye'"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- ĐIỀU KHOẢN -->
                        <h5 class="section-title text-danger fw-bold mb-3">ĐIỀU KHOẢN DỊCH VỤ VÀ CHÍNH SÁCH BẢO MẬT</h5>
                        <div class="terms-box mb-4 p-3 border rounded bg-light" ref="termsContent" @scroll="handleTermsScroll">
                            <h6 class="fw-bold">1. Điều khoản dịch vụ</h6>
                            <p>Khi đăng ký trở thành Đối tác của An Yên, Quý Đối tác đồng ý tuân thủ các điều khoản và điều kiện sau đây:</p>
                            <p><strong>1.1.</strong> Quý Đối tác cam kết cung cấp thông tin chính xác, đầy đủ và cập nhật trong suốt quá trình hợp tác.</p>
                            <p><strong>1.2.</strong> Đối tác có trách nhiệm bảo mật thông tin tài khoản và không chia sẻ cho bất kỳ bên thứ ba nào.</p>
                            <p><strong>1.3.</strong> An Yên bảo lưu quyền từ chối hoặc chấm dứt hợp tác nếu phát hiện các hành vi gian lận, vi phạm đạo đức kinh doanh hoặc gây ảnh hưởng xấu đến thương hiệu.</p>
                            
                            <h6 class="fw-bold mt-4">2. Chính sách bảo mật</h6>
                            <p><strong>2.1.</strong> An Yên cam kết bảo mật mọi thông tin cá nhân và doanh nghiệp mà Quý Đối tác cung cấp.</p>
                            <p><strong>2.2.</strong> Thông tin sẽ chỉ được sử dụng cho mục đích xác thực, hỗ trợ kỹ thuật và quản lý các giao dịch trên hệ thống.</p>
                            <p><strong>2.3.</strong> Chúng tôi không bán, trao đổi hoặc cho thuê thông tin của Quý Đối tác dưới mọi hình thức, trừ khi có yêu cầu từ cơ quan pháp luật có thẩm quyền.</p>

                            <h6 class="fw-bold mt-4">3. Trách nhiệm của các bên</h6>
                            <p><strong>3.1.</strong> An Yên sẽ hỗ trợ Đối tác trong việc sử dụng hệ thống và quảng bá sản phẩm/dịch vụ (nếu có thỏa thuận).</p>
                            <p><strong>3.2.</strong> Đối tác chịu trách nhiệm hoàn toàn về chất lượng hàng hóa/dịch vụ cung cấp cho khách hàng thông qua hệ thống An Yên.</p>
                            <p><em>Quý Đối tác vui lòng đọc kỹ trước khi đồng ý tham gia hệ sinh thái của chúng tôi.</em></p>
                        </div>

                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                            <div class="form-check custom-check d-flex align-items-center">
                                <input class="form-check-input" type="checkbox" v-model="isAcceptedTerms" id="agreeTerms" :disabled="!isTermsRead">
                                <label class="form-check-label ms-2" for="agreeTerms" :class="{'text-muted': !isTermsRead}">
                                    Tôi đã đọc, hiểu và đồng ý với <a href="#" class="text-primary text-decoration-none fw-medium">Điều khoản Dịch vụ</a> và <a href="#" class="text-primary text-decoration-none fw-medium">Chính sách Bảo mật</a>
                                </label>
                            </div>
                            <div v-if="!isTermsRead" class="alert-hint bg-danger-subtle text-danger px-3 py-2 rounded-pill small">
                                <i class="bi bi-arrow-down-up me-1"></i> Vui lòng kéo xuống cuối nội dung để có thể xác nhận đã đọc
                            </div>
                        </div>

                        <div class="text-center mt-5">
                            <button type="submit" class="btn btn-danger btn-lg px-5 rounded-pill fw-bold" :disabled="isSubmitting || !isAcceptedTerms">
                                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                                {{ isSubmitting ? 'ĐANG XỬ LÝ...' : 'TIẾP TỤC' }} <i v-if="!isSubmitting" class="bi bi-arrow-right ms-2"></i>
                            </button>
                        </div>
                    </form>

                    <div v-if="!token" class="text-center py-5">
                        <i class="bi bi-x-circle text-danger" style="font-size: 4rem;"></i>
                        <h4 class="mt-4">Không thể truy cập</h4>
                        <p class="text-muted">Đường dẫn đăng ký không hợp lệ hoặc đã hết hạn.</p>
                    </div>
                </div>
            </div>
        </main>

        <!-- Footer -->
        <footer class="footer bg-danger text-white py-4 mt-auto">
            <div class="container">
                <div class="row align-items-center">
                    <div class="col-md-3 mb-4 mb-md-0 text-center text-md-start">
                        <div class="brand-logo footer-logo mb-2">
                            <span class="an text-white">An</span><span class="yen text-white">Yên</span>
                        </div>
                        <div class="small opacity-75">NƠI GỬI TRỌN NIỀM TIN</div>
                    </div>
                    <div class="col-md-6 mb-4 mb-md-0 text-center text-md-start">
                        <h6 class="fw-bold mb-3">CÔNG TY CỔ PHẦN DỊCH VỤ AN YÊN</h6>
                        <ul class="list-unstyled mb-0">
                            <li class="mb-2"><i class="bi bi-geo-alt me-2"></i> 123 Đường An Lạc, Phường Yên Hòa, Quận Cầu Giấy, Hà Nội</li>
                            <li class="mb-2 d-inline-block me-4"><i class="bi bi-telephone me-2"></i> 1900 1234</li>
                            <li class="mb-2 d-inline-block me-4"><i class="bi bi-envelope me-2"></i> info@anyen.vn</li>
                            <li class="mb-2 d-inline-block"><i class="bi bi-globe me-2"></i> www.anyen.vn</li>
                        </ul>
                    </div>
                    <div class="col-md-3 text-center text-md-end">
                        <h6 class="fw-bold mb-3">KẾT NỐI VỚI CHÚNG TỐI</h6>
                        <div class="social-links d-flex justify-content-center justify-content-md-end gap-2">
                            <a href="#" class="social-btn"><i class="bi bi-facebook"></i></a>
                            <a href="#" class="social-btn"><span>Zalo</span></a>
                            <a href="#" class="social-btn"><i class="bi bi-youtube"></i></a>
                            <a href="#" class="social-btn"><i class="bi bi-tiktok"></i></a>
                        </div>
                    </div>
                </div>
                <div class="text-center mt-4 pt-3 border-top border-white-50 opacity-75 small">
                    &copy; 2026 An Yên. All rights reserved.
                </div>
            </div>
        </footer>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap');

.page-wrapper {
    font-family: 'Be Vietnam Pro', sans-serif;
    background-color: #fafafa;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

/* Header */
.brand-logo {
    font-family: 'Playball', cursive; /* Optional, assuming a script font for logo */
    font-size: 2.5rem;
    font-weight: 700;
    line-height: 1;
}
.brand-logo .an { font-style: italic; }
.brand-slogan {
    font-size: 0.75rem;
    font-weight: 600;
    letter-spacing: 1px;
}
.feature-text {
    font-size: 0.8rem;
    color: #555;
    line-height: 1.4;
}
.feature-icon {
    font-size: 1.5rem;
}
.subtitle-small {
    font-size: 0.9rem;
    letter-spacing: 1.5px;
}

/* Stepper */
.stepper-wrapper {
    display: flex;
    justify-content: space-between;
    position: relative;
    max-width: 800px;
    margin: 0 auto;
}
.stepper-wrapper::before {
    content: "";
    position: absolute;
    top: 15px;
    left: 10%;
    right: 10%;
    height: 2px;
    background: #e0e0e0;
    z-index: 1;
}
.stepper-item {
    position: relative;
    z-index: 2;
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
}
.step-counter {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #e0e0e0;
    color: #757575;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    margin-bottom: 8px;
    border: 3px solid #fafafa;
}
.step-name {
    font-size: 0.85rem;
    color: #757575;
}
.stepper-item.active .step-counter {
    background: #d32f2f;
    color: white;
}
.stepper-item.active .step-name {
    color: #d32f2f;
    font-weight: 600;
}

/* Custom Input Groups */
.custom-input-group {
    display: flex;
    align-items: center;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 8px 15px;
    background: #fff;
    transition: all 0.2s;
    position: relative;
}
.custom-input-group:focus-within {
    border-color: #d32f2f;
    box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.1);
}
.input-icon {
    font-size: 1.25rem;
    margin-right: 15px;
    width: 24px;
    text-align: center;
}
.input-content {
    flex: 1;
}
.input-content label {
    display: block;
    font-size: 0.75rem;
    color: #757575;
    margin-bottom: 2px;
}
.input-content input {
    border: none;
    padding: 0;
    font-size: 0.95rem;
    font-weight: 500;
    background: transparent;
}
.input-content input:focus {
    outline: none;
    box-shadow: none;
}
.btn-eye {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    padding: 0;
}

/* Terms Box */
.terms-box {
    max-height: 200px;
    overflow-y: auto;
    font-size: 0.9rem;
    color: #424242;
}
.terms-box::-webkit-scrollbar {
    width: 6px;
}
.terms-box::-webkit-scrollbar-thumb {
    background: #bdbdbd;
    border-radius: 10px;
}

.custom-check .form-check-input {
    width: 20px;
    height: 20px;
    cursor: pointer;
}
.custom-check .form-check-input:checked {
    background-color: #d32f2f;
    border-color: #d32f2f;
}

.btn-danger {
    background-color: #e57373;
    border: none;
    transition: all 0.3s;
}
.btn-danger:not(:disabled) {
    background-color: #d32f2f;
    box-shadow: 0 4px 10px rgba(211, 47, 47, 0.3);
}
.btn-danger:hover:not(:disabled) {
    background-color: #c62828;
    transform: translateY(-2px);
}

/* Footer */
.footer {
    background-color: #b71c1c !important; /* Deep Red */
}
.social-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 1px solid rgba(255,255,255,0.5);
    color: white;
    text-decoration: none;
    transition: all 0.3s;
    font-size: 0.9rem;
}
.social-btn span {
    font-size: 0.7rem;
    font-weight: bold;
}
.social-btn:hover {
    background: white;
    color: #b71c1c;
}
</style>
