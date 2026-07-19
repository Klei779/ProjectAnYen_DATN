import api from "../api/api.js";

export const guiTinNhanAi = async (message) => {
    const noiDung = message?.trim();

    if (!noiDung) {
        throw new Error("Vui lòng nhập nội dung cần hỏi.");
    }

    try {
        const response = await api.post(
            "/api/ai/chat",
            {
                message: noiDung
            }
        );

        const data = response.data;

        if (!data?.success) {
            throw new Error(
                data?.answer ||
                "Trợ lý AI không thể trả lời."
            );
        }

        return data.answer;
    } catch (error) {
        console.error("Lỗi gọi AI:", error);

        const message =
            error.response?.data?.answer ||
            error.response?.data?.message ||
            error.message;

        throw new Error(message);
    }
};

export const kiemTraAi = async () => {
    const response = await api.get(
        "/api/ai/health"
    );

    return response.data;
};