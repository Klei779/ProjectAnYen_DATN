<template>
  <main class="cart-page">
    <!-- HERO -->
    <section class="cart-hero">
      <div class="cart-container">
        <div class="cart-hero-content">
          <span class="cart-eyebrow">AN YÊN</span>

          <h1>Sản phẩm đã lưu</h1>

          <p>
            Danh sách những sản phẩm quý khách đang quan tâm.
            Quý khách có thể lưu lại dưới dạng file Excel để tiện tham khảo.
          </p>
        </div>
      </div>
    </section>

    <!-- BREADCRUMB -->
    <div class="breadcrumb-bar">
      <div class="cart-container">
        <nav class="breadcrumb">
          <RouterLink to="/">
            Trang chủ
          </RouterLink>

          <i class="fa-solid fa-chevron-right"></i>

          <span>Sản phẩm đã lưu</span>
        </nav>
      </div>
    </div>

    <!-- CONTENT -->
    <section class="cart-content-section">
      <div class="cart-container">
        <!-- EMPTY -->
        <div
            v-if="cartItems.length === 0"
            class="empty-cart"
        >
          <div class="empty-cart-icon">
            <i class="fa-regular fa-heart"></i>
          </div>

          <h2>Chưa có sản phẩm nào được lưu</h2>

          <p>
            Nhấn vào biểu tượng trái tim trên sản phẩm để thêm sản phẩm
            vào danh sách quan tâm.
          </p>

          <RouterLink
              to="/san-pham"
              class="view-products-button"
          >
            <i class="fa-solid fa-arrow-left"></i>
            Xem danh sách sản phẩm
          </RouterLink>
        </div>

        <!-- CART -->
        <template v-else>
          <div class="cart-toolbar">
            <div class="cart-summary">
              <span class="summary-icon">
                <i class="fa-solid fa-heart"></i>
              </span>

              <div>
                <small>Sản phẩm đã lưu</small>

                <strong>
                  {{ cartCount }} sản phẩm
                </strong>
              </div>
            </div>

            <div class="cart-actions">
              <button
                  type="button"
                  class="export-button"
                  :disabled="exporting"
                  @click="exportToExcel"
              >
                <i
                    :class="
                      exporting
                        ? 'fa-solid fa-spinner fa-spin'
                        : 'fa-solid fa-file-excel'
                    "
                ></i>

                {{ exporting ? "Đang xuất..." : "Xuất Excel" }}
              </button>

              <button
                  type="button"
                  class="clear-button"
                  @click="confirmClearCart"
              >
                <i class="fa-solid fa-trash-can"></i>
                Xóa tất cả
              </button>
            </div>
          </div>

          <div class="cart-layout">
            <!-- PRODUCT LIST -->
            <div class="cart-list">
              <article
                  v-for="item in cartItems"
                  :key="item.id"
                  class="cart-item"
              >
                <RouterLink
                    :to="`/san-pham/${item.id}`"
                    class="cart-item-image"
                >
                  <img
                      :src="item.image || noImage"
                      :alt="item.name || 'Sản phẩm An Yên'"
                      @error="handleImageError"
                  />
                </RouterLink>

                <div class="cart-item-content">
                  <span class="product-code">
                    Mã sản phẩm: {{ item.id }}
                  </span>

                  <RouterLink
                      :to="`/san-pham/${item.id}`"
                      class="product-name"
                  >
                    {{ item.name }}
                  </RouterLink>

                  <p
                      v-if="item.subname"
                      class="product-subname"
                  >
                    {{ item.subname }}
                  </p>

                  <div class="product-information">
                    <span v-if="item.material">
                      <i class="fa-solid fa-layer-group"></i>
                      {{ item.material }}
                    </span>

                    <span v-if="item.religion">
                      <i class="fa-solid fa-place-of-worship"></i>
                      {{ item.religion }}
                    </span>
                  </div>

                  <div class="product-price-row">
                    <strong>
                      {{ formatPrice(item.price) }}
                    </strong>

                    <del v-if="item.oldPrice">
                      {{ formatPrice(item.oldPrice) }}
                    </del>
                  </div>
                </div>

                <div class="cart-item-actions">
                  <RouterLink
                      :to="`/san-pham/${item.id}`"
                      class="detail-button"
                  >
                    Xem chi tiết
                  </RouterLink>

                  <button
                      type="button"
                      class="remove-button"
                      aria-label="Xóa khỏi danh sách"
                      @click="removeProduct(item.id)"
                  >
                    <i class="fa-solid fa-trash-can"></i>
                  </button>
                </div>
              </article>
            </div>

            <!-- SIDEBAR -->
            <aside class="cart-sidebar">
              <div class="cart-sidebar-card">
                <div class="sidebar-logo">
                  <img
                      :src="logoAnYen"
                      alt="Logo An Yên"
                  />
                </div>

                <h3>Danh sách quan tâm</h3>

                <div class="sidebar-row">
                  <span>Số sản phẩm</span>
                  <strong>{{ cartCount }}</strong>
                </div>

                <div class="sidebar-row">
                  <span>Tổng giá tham khảo</span>
                  <strong>{{ formatPrice(totalPrice) }}</strong>
                </div>

                <p class="price-note">
                  Giá sản phẩm chỉ mang tính tham khảo tại thời điểm lưu.
                  Vui lòng liên hệ An Yên để được tư vấn chính xác.
                </p>

                <button
                    type="button"
                    class="sidebar-export-button"
                    :disabled="exporting"
                    @click="exportToExcel"
                >
                  <i class="fa-solid fa-download"></i>
                  Tải danh sách Excel
                </button>

                <RouterLink
                    to="/san-pham"
                    class="continue-button"
                >
                  Tiếp tục xem sản phẩm
                </RouterLink>
              </div>
            </aside>
          </div>
        </template>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import ExcelJS from "exceljs";

import { useCart } from "../../services/useCart.js";

import noImage from "../../assets/images/noimage.jpg";
import logoAnYen from "../../assets/images/icon/logoAnYen.png";

const {
  cartItems,
  cartCount,
  removeFromCart,
  clearCart
} = useCart();

const exporting = ref(false);

const totalPrice = computed(() => {
  return cartItems.value.reduce((total, product) => {
    const price = Number(product.price);

    return total + (
        Number.isFinite(price)
            ? price
            : 0
    );
  }, 0);
});

function formatPrice(value) {
  if (
      value === null
      || value === undefined
      || value === ""
  ) {
    return "Liên hệ";
  }

  const numberValue = Number(value);

  if (!Number.isFinite(numberValue)) {
    return "Liên hệ";
  }

  return numberValue.toLocaleString("vi-VN") + " đ";
}

function handleImageError(event) {
  const image = event.currentTarget;

  if (image.dataset.fallbackApplied === "true") {
    return;
  }

  image.dataset.fallbackApplied = "true";
  image.src = noImage;
}

function removeProduct(productId) {
  removeFromCart(productId);

  ElMessage.success(
      "Đã xóa sản phẩm khỏi danh sách"
  );
}

async function confirmClearCart() {
  try {
    await ElMessageBox.confirm(
        "Bạn có chắc muốn xóa toàn bộ sản phẩm đã lưu không?",
        "Xóa danh sách",
        {
          confirmButtonText: "Xóa tất cả",
          cancelButtonText: "Hủy",
          type: "warning"
        }
    );

    clearCart();

    ElMessage.success(
        "Đã xóa toàn bộ sản phẩm"
    );
  } catch {
    // Người dùng bấm hủy
  }
}

function formatSavedDate(dateValue) {
  if (!dateValue) {
    return "";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toLocaleString("vi-VN");
}

function createExcelFileName() {
  const date = new Date();

  const day = String(
      date.getDate()
  ).padStart(2, "0");

  const month = String(
      date.getMonth() + 1
  ).padStart(2, "0");

  const year = date.getFullYear();

  const hours = String(
      date.getHours()
  ).padStart(2, "0");

  const minutes = String(
      date.getMinutes()
  ).padStart(2, "0");

  return `Danh-sach-san-pham-An-Yen-${day}-${month}-${year}-${hours}-${minutes}.xlsx`;
}

function downloadExcelFile(buffer, fileName) {
  const blob = new Blob(
      [buffer],
      {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      }
  );

  const downloadUrl = URL.createObjectURL(blob);

  const link = document.createElement("a");

  link.href = downloadUrl;
  link.download = fileName;

  document.body.appendChild(link);

  link.click();
  link.remove();

  URL.revokeObjectURL(downloadUrl);
}

async function loadLogoBuffer() {
  const response = await fetch(logoAnYen);

  if (!response.ok) {
    throw new Error(
        "Không thể tải logo An Yên"
    );
  }

  return await response.arrayBuffer();
}

async function exportToExcel() {
  if (cartItems.value.length === 0) {
    ElMessage.warning(
        "Chưa có sản phẩm để xuất Excel"
    );

    return;
  }

  exporting.value = true;

  try {
    const workbook = new ExcelJS.Workbook();

    workbook.creator = "Hệ thống An Yên";
    workbook.company = "An Yên";
    workbook.subject = "Danh sách sản phẩm khách hàng quan tâm";
    workbook.title = "Danh sách sản phẩm An Yên";
    workbook.created = new Date();

    const worksheet = workbook.addWorksheet(
        "Sản phẩm đã lưu",
        {
          views: [
            {
              showGridLines: false,
              state: "frozen",
              ySplit: 7
            }
          ]
        }
    );

    worksheet.columns = [
      {
        key: "index",
        width: 8
      },
      {
        key: "code",
        width: 18
      },
      {
        key: "name",
        width: 40
      },
      {
        key: "category",
        width: 22
      },
      {
        key: "material",
        width: 22
      },
      {
        key: "religion",
        width: 22
      },
      {
        key: "price",
        width: 20
      },
      {
        key: "savedAt",
        width: 24
      }
    ];

    /*
     * LOGO
     */
    try {
      const logoBuffer = await loadLogoBuffer();

      const logoId = workbook.addImage({
        buffer: logoBuffer,
        extension: "png"
      });

      worksheet.addImage(
          logoId,
          {
            tl: {
              col: 0.2,
              row: 0.2
            },
            ext: {
              width: 120,
              height: 70
            }
          }
      );
    } catch (logoError) {
      console.warn(
          "Không thể thêm logo vào Excel:",
          logoError
      );
    }

    /*
     * TIÊU ĐỀ
     */
    worksheet.mergeCells("C1:H2");

    const titleCell = worksheet.getCell("C1");

    titleCell.value = "DANH SÁCH SẢN PHẨM AN YÊN";
    titleCell.font = {
      name: "Arial",
      size: 18,
      bold: true,
      color: {
        argb: "FF1B7896"
      }
    };
    titleCell.alignment = {
      vertical: "middle",
      horizontal: "center"
    };

    worksheet.mergeCells("C3:H3");

    const descriptionCell = worksheet.getCell("C3");

    descriptionCell.value =
        "Danh sách sản phẩm khách hàng đang quan tâm";
    descriptionCell.font = {
      name: "Arial",
      size: 11,
      italic: true,
      color: {
        argb: "FF666666"
      }
    };
    descriptionCell.alignment = {
      horizontal: "center"
    };

    worksheet.mergeCells("A5:H5");

    const dateCell = worksheet.getCell("A5");

    dateCell.value =
        `Thời điểm xuất: ${new Date().toLocaleString("vi-VN")}`;
    dateCell.font = {
      name: "Arial",
      size: 10,
      italic: true
    };
    dateCell.alignment = {
      horizontal: "right"
    };

    /*
     * HEADER BẢNG
     */
    const headerRow = worksheet.getRow(7);

    headerRow.values = [
      "STT",
      "Mã sản phẩm",
      "Tên sản phẩm",
      "Phân loại",
      "Chất liệu",
      "Tôn giáo",
      "Giá tham khảo",
      "Thời gian lưu"
    ];

    headerRow.height = 28;

    headerRow.eachCell((cell) => {
      cell.font = {
        name: "Arial",
        size: 11,
        bold: true,
        color: {
          argb: "FFFFFFFF"
        }
      };

      cell.fill = {
        type: "pattern",
        pattern: "solid",
        fgColor: {
          argb: "FF1B7896"
        }
      };

      cell.alignment = {
        vertical: "middle",
        horizontal: "center",
        wrapText: true
      };

      cell.border = {
        top: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        },
        left: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        },
        bottom: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        },
        right: {
          style: "thin",
          color: {
            argb: "FFD9E3E8"
          }
        }
      };
    });

    /*
     * DỮ LIỆU SẢN PHẨM
     */
    cartItems.value.forEach(
        (product, index) => {
          const row = worksheet.addRow({
            index: index + 1,
            code: product.id,
            name: product.name || "",
            category: product.subname || "",
            material: product.material || "",
            religion: product.religion || "",
            price: Number(product.price) || 0,
            savedAt: formatSavedDate(
                product.addedAt
            )
          });

          row.height = 25;

          row.eachCell((cell) => {
            cell.font = {
              name: "Arial",
              size: 10
            };

            cell.alignment = {
              vertical: "middle",
              wrapText: true
            };

            cell.border = {
              top: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              },
              left: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              },
              bottom: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              },
              right: {
                style: "thin",
                color: {
                  argb: "FFE3E9EC"
                }
              }
            };
          });

          row.getCell(1).alignment = {
            vertical: "middle",
            horizontal: "center"
          };

          row.getCell(2).alignment = {
            vertical: "middle",
            horizontal: "center"
          };

          row.getCell(7).numFmt =
              '#,##0 "đ"';

          if (index % 2 !== 0) {
            row.eachCell((cell) => {
              cell.fill = {
                type: "pattern",
                pattern: "solid",
                fgColor: {
                  argb: "FFF3F8FA"
                }
              };
            });
          }
        }
    );

    /*
     * TỔNG KẾT
     */
    const summaryRowNumber =
        worksheet.rowCount + 2;

    worksheet.mergeCells(
        `A${summaryRowNumber}:F${summaryRowNumber}`
    );

    const summaryLabel = worksheet.getCell(
        `A${summaryRowNumber}`
    );

    summaryLabel.value =
        `Tổng cộng: ${cartCount.value} sản phẩm`;

    summaryLabel.font = {
      name: "Arial",
      bold: true,
      size: 11
    };

    summaryLabel.alignment = {
      horizontal: "right"
    };

    const totalCell = worksheet.getCell(
        `G${summaryRowNumber}`
    );

    totalCell.value = totalPrice.value;
    totalCell.numFmt = '#,##0 "đ"';
    totalCell.font = {
      name: "Arial",
      bold: true,
      size: 11,
      color: {
        argb: "FF1B7896"
      }
    };

    worksheet.getCell(
        `H${summaryRowNumber}`
    ).value = "";

    /*
     * GHI CHÚ
     */
    const noteRowNumber =
        summaryRowNumber + 3;

    worksheet.mergeCells(
        `A${noteRowNumber}:H${noteRowNumber + 1}`
    );

    const noteCell = worksheet.getCell(
        `A${noteRowNumber}`
    );

    noteCell.value =
        "Lưu ý: Giá sản phẩm trong danh sách chỉ mang tính tham khảo tại thời điểm xuất. Vui lòng liên hệ An Yên để được tư vấn và xác nhận giá chính xác.";

    noteCell.font = {
      name: "Arial",
      size: 10,
      italic: true,
      color: {
        argb: "FF666666"
      }
    };

    noteCell.alignment = {
      vertical: "middle",
      horizontal: "left",
      wrapText: true
    };

    /*
     * XUẤT FILE
     */
    const buffer =
        await workbook.xlsx.writeBuffer();

    downloadExcelFile(
        buffer,
        createExcelFileName()
    );

    ElMessage.success(
        "Xuất file Excel thành công"
    );
  } catch (error) {
    console.error(
        "Lỗi xuất file Excel:",
        error
    );

    ElMessage.error(
        "Không thể xuất file Excel"
    );
  } finally {
    exporting.value = false;
  }
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: #f6f8f9;
  color: #263238;
}

.cart-container {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
}

.cart-hero {
  padding: 150px 0 70px;
  background:
      linear-gradient(
          135deg,
          rgba(14, 76, 96, 0.96),
          rgba(27, 120, 150, 0.88)
      );
  color: #ffffff;
}

.cart-hero-content {
  max-width: 700px;
}

.cart-eyebrow {
  display: inline-block;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 4px;
}

.cart-hero h1 {
  margin: 0 0 16px;
  font-family: "Faustina", serif;
  font-size: clamp(36px, 5vw, 58px);
  font-weight: 600;
}

.cart-hero p {
  max-width: 640px;
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 16px;
  line-height: 1.8;
}

.breadcrumb-bar {
  background: #ffffff;
  border-bottom: 1px solid #e7ecef;
}

.breadcrumb {
  min-height: 56px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.breadcrumb a {
  color: #1b7896;
  text-decoration: none;
}

.breadcrumb i {
  color: #aab5ba;
  font-size: 10px;
}

.breadcrumb span {
  color: #68777e;
}

.cart-content-section {
  padding: 50px 0 80px;
}

.cart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
  padding: 18px 22px;
  background: #ffffff;
  border: 1px solid #e5eaed;
  border-radius: 14px;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 13px;
}

.summary-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(27, 120, 150, 0.1);
  color: #1b7896;
  border-radius: 50%;
}

.cart-summary div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.cart-summary small {
  color: #7a898f;
}

.cart-summary strong {
  font-size: 17px;
}

.cart-actions {
  display: flex;
  gap: 12px;
}

.export-button,
.clear-button,
.sidebar-export-button,
.continue-button,
.detail-button,
.view-products-button {
  border: none;
  border-radius: 8px;
  text-decoration: none;
  cursor: pointer;
  transition: 0.25s ease;
}

.export-button,
.clear-button {
  min-height: 42px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  font-size: 14px;
  font-weight: 600;
}

.export-button {
  background: #1b7896;
  color: #ffffff;
}

.export-button:hover {
  background: #146580;
}

.export-button:disabled,
.sidebar-export-button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.clear-button {
  background: #fff1f1;
  color: #c63c3c;
}

.clear-button:hover {
  background: #ffe3e3;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  gap: 28px;
  align-items: start;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr) auto;
  gap: 20px;
  align-items: center;
  padding: 18px;
  background: #ffffff;
  border: 1px solid #e5eaed;
  border-radius: 14px;
  transition: 0.25s ease;
}

.cart-item:hover {
  border-color: rgba(27, 120, 150, 0.35);
  box-shadow: 0 10px 30px rgba(28, 69, 82, 0.08);
}

.cart-item-image {
  width: 150px;
  height: 120px;
  overflow: hidden;
  border-radius: 10px;
  background: #f0f3f4;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.cart-item:hover .cart-item-image img {
  transform: scale(1.04);
}

.cart-item-content {
  min-width: 0;
}

.product-code {
  display: block;
  margin-bottom: 7px;
  color: #879399;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-name {
  display: inline-block;
  margin-bottom: 7px;
  color: #20343c;
  font-family: "Faustina", serif;
  font-size: 22px;
  font-weight: 600;
  text-decoration: none;
}

.product-name:hover {
  color: #1b7896;
}

.product-subname {
  margin: 0 0 10px;
  color: #718087;
  font-size: 14px;
}

.product-information {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 12px;
}

.product-information span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #617178;
  font-size: 13px;
}

.product-information i {
  color: #1b7896;
}

.product-price-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-price-row strong {
  color: #1b7896;
  font-size: 19px;
}

.product-price-row del {
  color: #9aa5aa;
  font-size: 13px;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-button {
  min-height: 38px;
  padding: 0 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(27, 120, 150, 0.09);
  color: #1b7896;
  font-size: 13px;
  font-weight: 600;
}

.detail-button:hover {
  background: #1b7896;
  color: #ffffff;
}

.remove-button {
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 8px;
  background: #fff1f1;
  color: #c63c3c;
  cursor: pointer;
  transition: 0.25s ease;
}

.remove-button:hover {
  background: #c63c3c;
  color: #ffffff;
}

.cart-sidebar {
  position: sticky;
  top: 110px;
}

.cart-sidebar-card {
  padding: 26px;
  background: #ffffff;
  border: 1px solid #e5eaed;
  border-radius: 14px;
}

.sidebar-logo {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.sidebar-logo img {
  max-width: 150px;
  max-height: 70px;
  object-fit: contain;
}

.cart-sidebar-card h3 {
  margin: 0 0 22px;
  color: #20343c;
  font-family: "Faustina", serif;
  font-size: 24px;
  text-align: center;
}

.sidebar-row {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 13px 0;
  border-bottom: 1px solid #edf0f2;
  font-size: 14px;
}

.sidebar-row span {
  color: #718087;
}

.sidebar-row strong {
  color: #20343c;
  text-align: right;
}

.price-note {
  margin: 18px 0;
  padding: 13px;
  background: #f4f8fa;
  color: #64757c;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.7;
}

.sidebar-export-button,
.continue-button {
  width: 100%;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  font-size: 14px;
  font-weight: 600;
}

.sidebar-export-button {
  margin-bottom: 10px;
  background: #1b7896;
  color: #ffffff;
}

.sidebar-export-button:hover {
  background: #146580;
}

.continue-button {
  background: #eef4f6;
  color: #1b7896;
}

.continue-button:hover {
  background: #dfecef;
}

.empty-cart {
  padding: 80px 30px;
  background: #ffffff;
  border: 1px solid #e5eaed;
  border-radius: 16px;
  text-align: center;
}

.empty-cart-icon {
  width: 90px;
  height: 90px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 22px;
  background: rgba(27, 120, 150, 0.1);
  color: #1b7896;
  border-radius: 50%;
  font-size: 36px;
}

.empty-cart h2 {
  margin: 0 0 10px;
  color: #20343c;
  font-family: "Faustina", serif;
  font-size: 30px;
}

.empty-cart p {
  max-width: 520px;
  margin: 0 auto 26px;
  color: #74838a;
  line-height: 1.7;
}

.view-products-button {
  min-height: 46px;
  padding: 0 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #1b7896;
  color: #ffffff;
  font-weight: 600;
}

.view-products-button:hover {
  background: #146580;
}

@media (max-width: 1050px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .cart-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .cart-container {
    width: min(100% - 28px, 1180px);
  }

  .cart-hero {
    padding: 120px 0 52px;
  }

  .cart-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .cart-actions {
    width: 100%;
  }

  .export-button,
  .clear-button {
    flex: 1;
    padding: 0 12px;
  }

  .cart-item {
    grid-template-columns: 105px minmax(0, 1fr);
    gap: 14px;
    padding: 14px;
  }

  .cart-item-image {
    width: 105px;
    height: 105px;
  }

  .cart-item-actions {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }

  .product-name {
    font-size: 18px;
  }
}

@media (max-width: 500px) {
  .cart-content-section {
    padding: 32px 0 60px;
  }

  .cart-actions {
    flex-direction: column;
  }

  .cart-item {
    grid-template-columns: 1fr;
  }

  .cart-item-image {
    width: 100%;
    height: 210px;
  }

  .cart-item-actions {
    grid-column: auto;
  }

  .detail-button {
    flex: 1;
  }
}
</style>