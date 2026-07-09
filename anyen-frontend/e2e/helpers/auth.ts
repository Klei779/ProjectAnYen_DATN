import type { APIRequestContext, Page } from "@playwright/test";

const API_BASE = process.env.API_URL ?? "http://localhost:8080";

export async function loginDoiTac(
  request: APIRequestContext,
  page: Page,
  username = process.env.DOI_TAC_USERNAME ?? "anphuc",
  password = process.env.DOI_TAC_PASSWORD ?? "123456"
) {
  const response = await request.post(`${API_BASE}/api/auth/login`, {
    data: {
      tenDangNhap: username,
      matKhau: password,
      loaiTaiKhoan: "DOI_TAC",
      captchaToken: "",
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Đăng nhập đối tác thất bại (${response.status()}): ${body}`);
  }

  const userData = await response.json();

  await page.goto("/");
  await page.evaluate((data) => {
    localStorage.setItem("user", JSON.stringify(data));
    localStorage.setItem("token", data.token);
    localStorage.setItem("loaiTaiKhoan", data.loaiTaiKhoan);
    localStorage.setItem("tenDangNhap", data.tenDangNhap);
    localStorage.setItem("id", String(data.id));
  }, userData);
}
