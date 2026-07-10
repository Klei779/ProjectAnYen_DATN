import type { Page } from "@playwright/test";
import path from "path";
import { fileURLToPath } from "url";
import { productTestData } from "../fixtures/product-test-data";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const sampleImage = path.join(__dirname, "../fixtures/sample.png");

type ProductData = typeof productTestData;

async function fillField(page: Page, label: string, value: string) {
  const field = page.locator(".tao-sp-field").filter({
    has: page.locator("label", { hasText: label }),
  });
  const input = field.locator("input:not([disabled]), textarea").first();
  if (await input.count()) {
    await input.fill(value);
    return;
  }
  await field.locator("select").selectOption(value);
}

export async function fillProductForm(
  page: Page,
  data: Partial<ProductData> = {}
) {
  const form = { ...productTestData, ...data };

  await fillField(page, "Tên sản phẩm", form.tenSanPham);
  await fillField(page, "Loại", form.loai);
  await fillField(page, "Giá bán", form.giaTien);
  await fillField(page, "Số lượng", form.soLuong);
  await fillField(page, "Tôn giáo", form.tonGiao);
  await fillField(page, "Quy cách", form.quyCach);

  await fillField(page, "Nội thất", form.noiThat);
  await fillField(page, "Thiết kế", form.thietKe);
  await fillField(page, "Xuất xứ", form.xuatXu);

  await page.locator(".tao-sp-swatch").first().click();

  await fillField(page, "Vật liệu", form.vatLieu);
  await fillField(page, "Kích thước", form.kichThuoc);
  await fillField(page, "Trọng lượng", form.trongLuong);
  await fillField(page, "Công nghệ sản xuất", form.cnsx);

  const promoInput = page
    .locator(".tao-sp-field")
    .filter({ has: page.locator("label", { hasText: "Khuyến mãi" }) })
    .locator("input[type='number']");
  if (form.khuyenMai) {
    await promoInput.fill(form.khuyenMai);
  }

  if (form.ghiChu) {
    await fillField(page, "Ghi chú", form.ghiChu);
  }

  await page.locator("#product-image-upload").setInputFiles(sampleImage);

  const titleInput = page
    .locator(".tao-sp-detail-block")
    .first()
    .locator("input.tao-sp-input");
  await titleInput.fill(form.detailTitle);

  await page.locator("button", { hasText: "Thêm nội dung" }).first().click();
  await page.locator("#editor-1").fill(form.detailText);
}
