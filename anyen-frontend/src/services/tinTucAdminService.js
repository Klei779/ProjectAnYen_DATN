import api from "../api/api.js";

const API_URL = "/api/admin/tin-tuc";

export async function getTinTucAdmin(
    params = {}
) {
    const response = await api.get(
        API_URL,
        {
            params,
        }
    );

    return response.data || {
        items: [],
        total: 0,
        page: 1,
        pageSize: 10,
        totalPages: 0,
    };
}

export async function getTinTucAdminById(
    id
) {
    const response = await api.get(
        `${API_URL}/${id}`
    );

    return response.data;
}

export async function createTinTuc(
    payload
) {
    const response = await api.post(
        API_URL,
        payload
    );

    return response.data;
}

export async function updateTinTuc(
    id,
    payload
) {
    const response = await api.put(
        `${API_URL}/${id}`,
        payload
    );

    return response.data;
}

export async function changeTinTucStatus(
    id,
    trangThai
) {
    const response = await api.patch(
        `${API_URL}/${id}/trang-thai`,
        null,
        {
            params: {
                trangThai,
            },
        }
    );

    return response.data;
}

export async function deleteTinTuc(
    id
) {
    const response = await api.delete(
        `${API_URL}/${id}`
    );

    return response.data;
}

export async function uploadAnhTinTuc(
    file,
    onUploadProgress
) {
    const formData = new FormData();

    formData.append(
        "file",
        file
    );

    const response = await api.post(
        `${API_URL}/upload-anh`,
        formData,
        {
            onUploadProgress,
        }
    );

    return response.data;
}