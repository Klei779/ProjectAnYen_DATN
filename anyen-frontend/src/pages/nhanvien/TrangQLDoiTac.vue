<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const doiTacs = ref([]);
const isModalOpen = ref(false);
const inviteEmail = ref('');
const isInviting = ref(false);
const alertMsg = ref('');
const alertType = ref('');

const fetchDoiTacs = async () => {
    try {
        const response = await axios.get('http://localhost:8080/api/quanly-doitac/list', {
            headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
            }
        });
        doiTacs.value = response.data;
    } catch (error) {
        console.error("Lỗi khi tải danh sách đối tác:", error);
    }
};

onMounted(() => {
    fetchDoiTacs();
});

const openInviteModal = () => {
    inviteEmail.value = '';
    alertMsg.value = '';
    isModalOpen.value = true;
};

const closeInviteModal = () => {
    isModalOpen.value = false;
};

const sendInvite = async () => {
    if (!inviteEmail.value) {
        alertMsg.value = 'Vui lòng nhập email!';
        alertType.value = 'danger';
        return;
    }

    try {
        isInviting.value = true;
        alertMsg.value = '';
        await axios.post('http://localhost:8080/api/quanly-doitac/invite', {
            email: inviteEmail.value
        }, {
            headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
            }
        });

        alertMsg.value = 'Đã gửi lời mời hợp tác thành công!';
        alertType.value = 'success';
        fetchDoiTacs();
        
        setTimeout(() => {
            closeInviteModal();
        }, 2000);
    } catch (error) {
        alertMsg.value = error.response?.data || 'Có lỗi xảy ra khi gửi lời mời.';
        alertType.value = 'danger';
    } finally {
        isInviting.value = false;
    }
};

const getStatusBadge = (status) => {
    if (status === 1) return '<span class="badge bg-success">Hoạt động</span>';
    if (status === 2) return '<span class="badge bg-warning text-dark">Đang ký</span>';
    return '<span class="badge bg-secondary">Ngừng hoạt động</span>';
};
</script>

<template>
    <div class="container-fluid py-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="mb-0 text-gray-800">Quản Lý Đối Tác</h4>
            <button class="btn btn-primary shadow-sm" @click="openInviteModal">
                <i class="bi bi-plus-circle me-2"></i>Thêm đối tác
            </button>
        </div>

        <div class="card shadow-sm border-0">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Tên đối tác</th>
                                <th>Doanh nghiệp</th>
                                <th>Email</th>
                                <th>Số điện thoại</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="dt in doiTacs" :key="dt.maDoiTac">
                                <td>{{ dt.maDoiTac }}</td>
                                <td>{{ dt.tenDoiTac }}</td>
                                <td>{{ dt.tenDoanhNghiep || '—' }}</td>
                                <td>{{ dt.email || dt.tenDangNhap }}</td>
                                <td>{{ dt.soDienThoai || '—' }}</td>
                                <td v-html="getStatusBadge(dt.trangThai)"></td>
                            </tr>
                            <tr v-if="doiTacs.length === 0">
                                <td colspan="6" class="text-center py-4 text-muted">Chưa có dữ liệu đối tác</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Modal Invite Partner -->
        <div class="modal fade show" tabindex="-1" style="display: block; background: rgba(0,0,0,0.5);" v-if="isModalOpen">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content border-0 shadow">
                    <div class="modal-header border-bottom-0">
                        <h5 class="modal-title fw-bold">Mời Đối Tác Mới</h5>
                        <button type="button" class="btn-close" @click="closeInviteModal"></button>
                    </div>
                    <div class="modal-body">
                        <p class="text-muted mb-4">Hệ thống sẽ gửi một email chứa liên kết đăng ký đến địa chỉ này.</p>
                        
                        <div v-if="alertMsg" :class="`alert alert-${alertType} py-2 mb-3`">
                            {{ alertMsg }}
                        </div>

                        <div class="form-group">
                            <label class="form-label fw-medium">Email đối tác <span class="text-danger">*</span></label>
                            <input type="email" class="form-control form-control-lg" v-model="inviteEmail" 
                                placeholder="Ví dụ: partner@company.com" @keyup.enter="sendInvite" />
                        </div>
                    </div>
                    <div class="modal-footer border-top-0">
                        <button type="button" class="btn btn-light px-4" @click="closeInviteModal">Hủy</button>
                        <button type="button" class="btn btn-primary px-4" @click="sendInvite" :disabled="isInviting">
                            <span v-if="isInviting" class="spinner-border spinner-border-sm me-2"></span>
                            {{ isInviting ? 'Đang gửi...' : 'Gửi lời mời' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.table th {
    font-weight: 600;
    text-transform: uppercase;
    font-size: 0.85rem;
    letter-spacing: 0.5px;
    padding: 15px;
}
.table td {
    padding: 15px;
}
.badge {
    padding: 6px 10px;
    font-weight: 500;
}
</style>