import { test, expect } from "@playwright/test";
import { loginDoiTac } from "./helpers/auth";
import { fillProductForm } from "./helpers/fillProductForm";
import { productTestData } from "./fixtures/product-test-data";

test.describe("Tạo sản phẩm đối tác", () => {
  test.beforeEach(async ({ page, request }) => {
    await loginDoiTac(request, page);
    await page.goto("/doi-tac/tao-san-pham");
    await expect(page.locator(".tao-sp-section-title").first()).toHaveText(
      "1. Thông tin cơ bản"
    );
  });

  test("tự động điền form và đăng sản phẩm thành công", async ({ page }) => {
    const uniqueName = `${productTestData.tenSanPham} ${Date.now()}`;
    await fillProductForm(page, { tenSanPham: uniqueName });

    page.once("dialog", (dialog) => {
      expect(dialog.message()).toContain("thành công");
      dialog.accept();
    });

    await page.locator(".tao-sp-btn-publish").click();

    await expect(page).toHaveURL(/\/doi-tac\/quan-ly-san-pham/, {
      timeout: 30_000,
    });
  });

  test("tự động điền form và lưu nháp thành công", async ({ page }) => {
    const uniqueName = `Nháp test ${Date.now()}`;
    await fillProductForm(page, { tenSanPham: uniqueName });

    page.once("dialog", (dialog) => {
      expect(dialog.message()).toContain("nháp");
      dialog.accept();
    });

    await page.locator(".tao-sp-btn-draft").click();

    await expect(page).toHaveURL(/\/doi-tac\/quan-ly-san-pham/, {
      timeout: 30_000,
    });
  });

  test("hiển thị lỗi khi thiếu ảnh sản phẩm", async ({ page }) => {
    await fillProductForm(page);

    await page.evaluate(() => {
      const input = document.getElementById(
        "product-image-upload"
      ) as HTMLInputElement | null;
      if (input) input.value = "";
    });

    await page.locator(".tao-sp-btn-publish").click();

    await expect(
      page.locator(".tao-sp-error-text", {
        hasText: "Vui lòng tải lên ít nhất 1 hình ảnh",
      })
    ).toBeVisible();
  });
});
