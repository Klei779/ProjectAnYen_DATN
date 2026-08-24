<template>
  <div class="finance-page">

    <!-- =====================================================
         HEADER
    ====================================================== -->
    <div class="page-heading">
      <div>
        <span class="eyebrow">
          TÀI CHÍNH ĐỐI TÁC
        </span>

        <h2>Quỹ & Ví</h2>

        <p>
          Quản lý Quỹ bảo đảm, Ví đối tác.
        </p>
      </div>

      <div class="header-actions">
        <button
            class="refresh-btn"
            type="button"
            :disabled="loading"
            @click="loadData"
        >
          <i
              class="fa-solid fa-rotate"
              :class="{ 'fa-spin': loading }"
          ></i>

          Làm mới
        </button>
      </div>
    </div>


    <!-- =====================================================
         LOADING
    ====================================================== -->
    <div
        v-if="loading && !finance"
        class="loading-card"
    >
      <i class="fa-solid fa-spinner fa-spin"></i>

      <span>
        Đang tải thông tin...
      </span>
    </div>


    <!-- =====================================================
         CONTENT
    ====================================================== -->
    <div
        v-else
        class="finance-grid"
    >

      <!-- =================================================
           QUỸ
      ================================================== -->
      <section class="finance-card">

        <div class="card-head">

          <div class="icon-box">
            <i class="fa-solid fa-shield-halved"></i>
          </div>

          <div class="head-info">
            <span>QUỸ BẢO ĐẢM</span>

            <h3>
              {{
                finance?.daMoQuy
                    ? "Đã mở quỹ"
                    : "Chưa mở quỹ"
              }}
            </h3>
          </div>

          <span
              class="status-pill"
              :class="
              finance?.daMoQuy
                ? 'active'
                : 'inactive'
            "
          >
            {{
              finance?.daMoQuy
                  ? "Đang hoạt động"
                  : "Chưa kích hoạt"
            }}
          </span>

        </div>


        <!-- =================================================
             ĐÃ MỞ QUỸ
        ================================================== -->
        <template v-if="finance?.daMoQuy">

          <div class="main-balance">
            <span>
              Tổng số dư Quỹ
            </span>

            <strong>
              {{
                formatMoney(
                    finance?.soDuQuy
                )
              }}
            </strong>
          </div>


          <div class="balance-row">

            <div class="balance-box">
              <span>
                <i class="fa-solid fa-circle-check green"></i>
                Khả dụng
              </span>

              <strong>
                {{
                  formatMoney(
                      finance?.soDuQuyKhaDung
                  )
                }}
              </strong>
            </div>


            <div class="balance-box">
              <span>
                <i class="fa-solid fa-lock orange"></i>
                Đang khóa theo đơn
              </span>

              <strong>
                {{
                  formatMoney(
                      finance?.soDuQuyDangKhoa
                  )
                }}
              </strong>
            </div>

          </div>


          <div class="fund-note">
            <i class="fa-solid fa-circle-info"></i>

            <span>
              Khi nhận đơn, hệ thống khóa 100% giá trị phần đơn.
              Khi hoàn tất, 20% là phí sàn và 80% chuyển vào Ví.
            </span>
          </div>


          <div class="finance-actions">

            <button
                class="primary-btn"
                type="button"
                @click="openDepositDialog"
            >
              <i class="fa-solid fa-plus"></i>

              Nạp Quỹ
            </button>


            <button
                class="outline-btn"
                type="button"
                @click="openWithdrawFundDialog"
            >
              <i class="fa-solid fa-money-bill-transfer"></i>

              Rút Quỹ
            </button>

          </div>

        </template>


        <!-- =================================================
             CHƯA MỞ QUỸ
        ================================================== -->
        <template v-else>

          <div class="empty-fund">
            <i class="fa-solid fa-vault"></i>

            <h4>
              Mở Quỹ để bán trực tiếp trên website
            </h4>

            <p>
              Khi chưa mở Quỹ, sản phẩm của bạn
              sẽ hiển thị “Liên hệ” thay vì “Mua”.
            </p>
          </div>


          <button
              class="primary-btn"
              type="button"
              :disabled="submitting"
              @click="handleOpenFund"
          >
            <i
                :class="
                submitting
                  ? 'fa-solid fa-spinner fa-spin'
                  : 'fa-solid fa-lock-open'
              "
            ></i>

            {{
              submitting
                  ? "Đang xử lý..."
                  : "Mở Quỹ"
            }}
          </button>

        </template>

      </section>


      <!-- =================================================
           VÍ
      ================================================== -->
      <section class="finance-card">

        <div class="card-head">

          <div class="icon-box wallet-icon">
            <i class="fa-solid fa-wallet"></i>
          </div>


          <div class="head-info">
            <span>
              VÍ ĐỐI TÁC
            </span>

            <h3>
              Số dư được quyết toán
            </h3>
          </div>

        </div>


        <div class="main-balance wallet-balance">
          <span>
            Số dư Ví hiện tại
          </span>

          <strong>
            {{
              formatMoney(
                  finance?.soDuVi
              )
            }}
          </strong>
        </div>


        <div class="wallet-flow">

          <div>
            <i class="fa-solid fa-truck"></i>
            <span>
              Giao hàng & thu tiền khách
            </span>
          </div>

          <i class="fa-solid fa-arrow-down flow-arrow"></i>

          <div>
            <i class="fa-solid fa-circle-check"></i>
            <span>
              Nhân viên xác nhận hoàn thành
            </span>
          </div>

          <i class="fa-solid fa-arrow-down flow-arrow"></i>

          <div>
            <i class="fa-solid fa-coins"></i>
            <span>
              80% Quỹ khóa chuyển vào Ví
            </span>
          </div>

        </div>


        <div class="finance-actions">

          <button
              class="wallet-btn"
              type="button"
              @click="openWithdrawWalletDialog"
          >
            <i class="fa-solid fa-building-columns"></i>

            Rút tiền
          </button>


          <button
              class="outline-btn"
              type="button"
              @click="openTransferDialog"
          >
            <i class="fa-solid fa-arrow-right-arrow-left"></i>

            Chuyển vào Quỹ
          </button>

        </div>


        <p class="action-note">
          <i class="fa-solid fa-circle-info"></i>

          Tiền trong Ví có thể chuyển vào Quỹ.
        </p>

      </section>

    </div>


    <!-- =====================================================
         POPUP NHẬP TIỀN NẠP QUỸ
    ====================================================== -->
    <el-dialog
        v-model="depositVisible"
        title="Nạp Quỹ qua QR"
        width="430px"
        :close-on-click-modal="false"
    >
      <div class="money-form">

        <label>
          Số tiền muốn nạp
        </label>

        <el-input-number
            v-model="amount"
            :min="1000"
            :step="100000"
            :controls="false"
            class="money-input"
        />


        <div class="quick-money">

          <button
              type="button"
              @click="amount = 1000000"
          >
            1 triệu
          </button>

          <button
              type="button"
              @click="amount = 5000000"
          >
            5 triệu
          </button>

          <button
              type="button"
              @click="amount = 10000000"
          >
            10 triệu
          </button>

          <button
              type="button"
              @click="amount = 20000000"
          >
            20 triệu
          </button>

        </div>

      </div>


      <template #footer>

        <button
            class="dialog-cancel"
            type="button"
            :disabled="submitting"
            @click="depositVisible = false"
        >
          Hủy
        </button>


        <button
            class="dialog-confirm"
            type="button"
            :disabled="submitting"
            @click="createDepositPayoo"
        >
          <i
              v-if="submitting"
              class="fa-solid fa-spinner fa-spin"
          ></i>

          {{
            submitting
                ? "Đang tạo..."
                : "Tạo QR"
          }}
        </button>

      </template>

    </el-dialog>

    <el-dialog
        v-model="qrVisible"
        width="440px"
        :show-close="payooStatus !== 'processing'"
        :close-on-click-modal="false"
        :close-on-press-escape="payooStatus !== 'processing'"
    >

      <div class="payoo-box">


        <!-- WAITING -->
        <template
            v-if="payooStatus === 'waiting'"
        >

          <h3 class="payoo-title">
            NẠP QUỸ BẢO ĐẢM
          </h3>

          <p class="payoo-description">
            Quét mã QR để thanh toán.
          </p>


          <img
              v-if="qrImage"
              :src="qrImage"
              class="payoo-qr"
              alt="QR Payoo"
              @click="handleQrClick"
          />

          <div class="payoo-amount">
            {{
              formatMoney(
                  currentTransaction?.soTien
              )
            }}
          </div>


          <div class="payoo-code">
            <span>
              Mã giao dịch
            </span>

            <strong>
              {{
                currentTransaction?.maGiaoDich
              }}
            </strong>
          </div>

        </template>


        <!-- PROCESSING -->
        <template
            v-else-if="payooStatus === 'processing'"
        >

          <div class="processing-state">

            <i class="fa-solid fa-spinner fa-spin"></i>

            <h3>
              Đang xử lý...
            </h3>

            <p>
              Đang xác nhận giao dịch
            </p>

            <strong>
              {{
                formatMoney(
                    currentTransaction?.soTien
                )
              }}
            </strong>

          </div>

        </template>


        <!-- SUCCESS -->
        <template
            v-else-if="payooStatus === 'success'"
        >

          <div class="success-state">

            <i class="fa-solid fa-circle-check"></i>

            <h3>
              Thanh toán thành công
            </h3>

            <strong>
              {{
                formatMoney(
                    currentTransaction?.soTien
                )
              }}
            </strong>

            <p>
              Đã xác nhận giao dịch.
              Số dư Quỹ đã được cập nhật.
            </p>

            <small>
              {{
                currentTransaction?.maGiaoDich
              }}
            </small>

          </div>

        </template>

      </div>

    </el-dialog>


    <!-- =====================================================
         POPUP RÚT QUỸ / RÚT VÍ
    ====================================================== -->
    <el-dialog
        v-model="withdrawVisible"
        :title="
        withdrawType === 'fund'
          ? 'Rút Quỹ '
          : 'Rút Ví '
      "
        width="430px"
        :close-on-click-modal="false"
    >

      <div class="money-form">


        <div class="available-box">
          <span>
            {{
              withdrawType === "fund"
                  ? "Quỹ khả dụng"
                  : "Số dư Ví"
            }}
          </span>

          <strong>
            {{
              formatMoney(
                  withdrawType === "fund"
                      ? finance?.soDuQuyKhaDung
                      : finance?.soDuVi
              )
            }}
          </strong>
        </div>


        <label>
          Số tiền muốn rút
        </label>

        <el-input-number
            v-model="amount"
            :min="1000"
            :max="
            withdrawType === 'fund'
              ? Number(finance?.soDuQuyKhaDung || 0)
              : Number(finance?.soDuVi || 0)
          "
            :step="100000"
            :controls="false"
            class="money-input"
        />

      </div>


      <template #footer>

        <button
            class="dialog-cancel"
            type="button"
            :disabled="submitting"
            @click="withdrawVisible = false"
        >
          Hủy
        </button>


        <button
            class="dialog-confirm"
            type="button"
            :disabled="submitting"
            @click="handleWithdraw"
        >
          <i
              v-if="submitting"
              class="fa-solid fa-spinner fa-spin"
          ></i>

          {{
            submitting
                ? "Đang xử lý..."
                : "Xác nhận rút"
          }}
        </button>

      </template>

    </el-dialog>


    <!-- =====================================================
         POPUP VÍ -> QUỸ
    ====================================================== -->
    <el-dialog
        v-model="transferVisible"
        title="Chuyển tiền từ Ví vào Quỹ"
        width="430px"
        :close-on-click-modal="false"
    >

      <div class="money-form">

        <div class="transfer-summary">

          <div>
            <span>
              Ví
            </span>

            <strong>
              {{
                formatMoney(
                    finance?.soDuVi
                )
              }}
            </strong>
          </div>


          <i class="fa-solid fa-arrow-right"></i>


          <div>
            <span>
              Quỹ
            </span>

            <strong>
              {{
                formatMoney(
                    finance?.soDuQuy
                )
              }}
            </strong>
          </div>

        </div>


        <label>
          Số tiền muốn chuyển
        </label>

        <el-input-number
            v-model="amount"
            :min="1000"
            :max="Number(finance?.soDuVi || 0)"
            :step="100000"
            :controls="false"
            class="money-input"
        />

      </div>


      <template #footer>

        <button
            class="dialog-cancel"
            type="button"
            :disabled="submitting"
            @click="transferVisible = false"
        >
          Hủy
        </button>


        <button
            class="dialog-confirm"
            type="button"
            :disabled="submitting"
            @click="handleTransfer"
        >
          <i
              v-if="submitting"
              class="fa-solid fa-spinner fa-spin"
          ></i>

          {{
            submitting
                ? "Đang chuyển..."
                : "Chuyển vào Quỹ"
          }}
        </button>

      </template>

    </el-dialog>

    <!-- =====================================================
         LỊCH SỬ GIAO DỊCH
    ====================================================== -->
    <section class="finance-card history-card">
      <div class="card-head">
        <div class="icon-box history-icon">
          <i class="fa-solid fa-clock-rotate-left"></i>
        </div>
        <div class="head-info">
          <span>Lịch sử giao dịch</span>
          <h3>{{ historyList.length }} giao dịch</h3>
        </div>
      </div>

      <!-- Loading -->
      <div
          v-if="historyLoading"
          class="empty-state"
      >
        <div class="empty-state-icon loading-icon">
          <i class="fa-solid fa-spinner fa-spin"></i>
        </div>
        <span>Đang tải lịch sử giao dịch...</span>
      </div>

      <!-- Không có dữ liệu -->
      <div
          v-else-if="!historyList.length"
          class="empty-state"
      >
        <div class="empty-state-icon">
          <i class="fa-regular fa-folder-open"></i>
        </div>
        <span>Chưa có lịch sử giao dịch</span>
      </div>

      <!-- Timeline -->
      <div
          v-else
          class="history-timeline"
      >
        <div
            v-for="(item, index) in historyList"
            :key="index"
            class="timeline-item"
        >
          <div
              class="timeline-icon"
              :class="classByTransaction(item.loaiVi, item.loaiGiaoDich)"
          >
            <i
                class="fa-solid"
                :class="iconByTransaction(item.loaiVi, item.loaiGiaoDich)"
            ></i>
          </div>

          <div class="timeline-content">
            <div class="timeline-top">
              <h4>
                {{ item.loaiVi === "QUY" ? "Quỹ bảo đảm" : "Ví đối tác" }}
              </h4>
              <span>
                {{ formatDateTime(item.thoiGian) }}
              </span>
            </div>

            <p>
              {{ item.noiDung || "Giao dịch" }}
            </p>

            <div class="timeline-amount">
              <span
                  :class="item.loaiGiaoDich === '+' ? 'amount-plus' : 'amount-minus'"
              >
                {{ item.loaiGiaoDich }}{{ formatMoney(item.soTien) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>


<script setup>

import {
  onMounted,
  ref
} from "vue"

import {
  ElMessage,
  ElMessageBox
} from "element-plus"

import QRCode from "qrcode"


// =====================================================
// SERVICE QUỸ & VÍ
// =====================================================

import {
  getTaiChinhDoiTac,
  moQuyDoiTac,
  taoPayooNapQuy,
  taoPayooRutQuy,
  taoPayooRutVi,
  chuyenViVaoQuy,
  getLichSuGiaoDich
} from "../../services/taiChinhDoiTacService.js"


// =====================================================
// SERVICE PAYOO
// =====================================================

import {
  confirmPayooTransaction
} from "../../services/payooMockService.js"


// =====================================================
// STATE
// =====================================================

const finance =
    ref(null)


const loading =
    ref(false)


const submitting =
    ref(false)


const depositVisible =
    ref(false)


const withdrawVisible =
    ref(false)


const transferVisible =
    ref(false)


const qrVisible =
    ref(false)


const amount =
    ref(1000000)


/*
 * fund = rút Quỹ
 * wallet = rút Ví
 */
const withdrawType =
    ref("fund")


/*
 * Transaction Payoo hiện tại.
 */
const currentTransaction =
    ref(null)


/*
 * QR được tạo bằng thư viện qrcode.
 */
const qrImage =
    ref("")


/*
 * waiting
 * processing
 * success
 */
const payooStatus =
    ref("waiting")


// =====================================================
// LỊCH SỬ GIAO DỊCH
// =====================================================

const historyLoading =
    ref(false)


const historyList =
    ref([])


// =====================================================
// FORMAT MONEY
// =====================================================

const formatMoney = (
    value
) => {

  return (
      Number(
          value || 0
      )
          .toLocaleString(
              "vi-VN"
          )

      + " đ"
  )
}


// =====================================================
// GET ERROR
// =====================================================

const getErrorMessage = (
    error,
    fallback
) => {

  return (
      error
          ?.response
          ?.data
          ?.message

      ||

      error
          ?.response
          ?.data
          ?.error

      ||

      fallback
  )
}


// =====================================================
// DELAY
// =====================================================

const delay = (
    milliseconds
) => {

  return new Promise(
      resolve => {

        setTimeout(
            resolve,
            milliseconds
        )

      }
  )
}


// =====================================================
// LOAD QUỸ + VÍ
// =====================================================

const loadData = async () => {

  loading.value =
      true


  try {

    const result =
        await getTaiChinhDoiTac()

    console.log("=== LOAD DATA DEBUG ===")
    console.log("SoDuQuy:", result?.soDuQuy)
    console.log("SoDuQuyDangKhoa:", result?.soDuQuyDangKhoa)
    console.log("SoDuVi:", result?.soDuVi)

    finance.value = result

  } catch (error) {

    console.error(
        "Lỗi tải Quỹ & Ví:",
        error
    )


    ElMessage.error(
        getErrorMessage(
            error,
            "Không tải được thông tin Quỹ & Ví"
        )
    )

  } finally {

    loading.value =
        false
  }
}


// =====================================================
// LOAD LỊCH SỬ GIAO DỊCH
// =====================================================

const loadHistory = async () => {

  historyLoading.value =
      true


  try {

    const response =
        await getLichSuGiaoDich()

    historyList.value =
        response || []

  } catch (error) {

    console.error(
        "Lỗi tải lịch sử giao dịch:",
        error
    )


    ElMessage.error(
        getErrorMessage(
            error,
            "Không tải được lịch sử giao dịch"
        )
    )

  } finally {

    historyLoading.value =
        false
  }
}


// =====================================================
// HELPER FUNCTIONS CHO TIMELINE
// =====================================================

const iconByTransaction = (loaiVi, loaiGiaoDich) => {
  if (loaiVi === "QUY") {
    if (loaiGiaoDich === "+") return "fa-plus-circle";
    return "fa-minus-circle";
  }
  if (loaiVi === "VI") {
    if (loaiGiaoDich === "+") return "fa-arrow-down";
    return "fa-arrow-up";
  }
  return "fa-exchange-alt";
}

const classByTransaction = (loaiVi, loaiGiaoDich) => {
  if (loaiVi === "QUY") {
    return loaiGiaoDich === "+" ? "green" : "orange";
  }
  if (loaiVi === "VI") {
    return loaiGiaoDich === "+" ? "blue" : "red";
  }
  return "purple";
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return "---";
  const date = new Date(dateTime);
  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}


// =====================================================
// MỞ QUỸ
// =====================================================

const handleOpenFund = async () => {

  try {

    await ElMessageBox.confirm(

        "Sau khi mở Quỹ, các sản phẩm đủ điều kiện "
        + "có thể hiển thị nút Mua trên website. "
        + "Bạn có muốn tiếp tục?",

        "Mở Quỹ bảo đảm",

        {
          confirmButtonText:
              "Mở Quỹ",

          cancelButtonText:
              "Để sau",

          type:
              "warning"
        }
    )

  } catch {

    return
  }


  submitting.value =
      true


  try {

    finance.value =
        await moQuyDoiTac()


    ElMessage.success(
        "Mở Quỹ bảo đảm thành công"
    )

  } catch (error) {

    ElMessage.error(
        getErrorMessage(
            error,
            "Mở Quỹ thất bại"
        )
    )

  } finally {

    submitting.value =
        false
  }
}


// =====================================================
// OPEN NẠP QUỸ
// =====================================================

const openDepositDialog = () => {

  amount.value =
      1000000


  depositVisible.value =
      true
}


// =====================================================
// TẠO PAYOO NẠP QUỸ
// =====================================================

const createDepositPayoo = async () => {

  const soTien =
      Number(
          amount.value || 0
      )


  if (
      soTien < 1000
  ) {

    ElMessage.warning(
        "Số tiền nạp tối thiểu là 1.000đ"
    )

    return
  }


  submitting.value =
      true


  try {

    /*
     * Bước 1:
     * tạo transaction Payoo.
     *
     * Chưa cộng Quỹ.
     */
    const transaction =
        await taoPayooNapQuy(
            soTien
        )


    currentTransaction.value =
        transaction


    /*
     * Bước 2:
     * tạo nội dung QR.
     */
    const qrContent = [
      "PAYOO MOCK",
      `MA_GIAO_DICH=${transaction.maGiaoDich}`,
      `LOAI=${transaction.loaiGiaoDich}`,
      `SO_TIEN=${transaction.soTien}`
    ].join("|")


    /*
     * Bước 3:
     * tạo ảnh QR base64.
     */
    qrImage.value =
        await QRCode.toDataURL(
            qrContent,
            {
              width: 270,
              margin: 2
            }
        )


    depositVisible.value =
        false


    payooStatus.value =
        "waiting"


    qrVisible.value =
        true

  } catch (error) {

    console.error(
        "Không tạo được Payoo:",
        error
    )


    ElMessage.error(
        getErrorMessage(
            error,
            "Không tạo được giao dịch Payoo"
        )
    )

  } finally {

    submitting.value =
        false
  }
}


// =====================================================
// CLICK QR
// =====================================================

const handleQrClick = async () => {

  if (
      !currentTransaction
          .value
          ?.maGiaoDich
  ) {

    return
  }


  /*
   * Chống click QR nhiều lần.
   */
  if (
      payooStatus.value
      !== "waiting"
  ) {

    return
  }


  payooStatus.value =
      "processing"


  try {

    /*
     * Giả lập Payoo xử lý.
     */
    await delay(
        1200
    )


    /*
     * Payoo Mock callback về backend.
     *
     * Backend mới cộng Quỹ.
     */
    const result =
        await confirmPayooTransaction(
            currentTransaction
                .value
                .maGiaoDich
        )


    currentTransaction.value =
        result


    payooStatus.value =
        "success"


    /*
     * Lấy số dư Quỹ mới.
     */
    await loadData()


    /*
     * Làm mới lịch sử giao dịch.
     */
    await loadHistory()


    ElMessage.success(
        "Nạp Quỹ thành công"
    )


    /*
     * Cho người dùng nhìn success.
     */
    await delay(
        1700
    )


    qrVisible.value =
        false


    resetPayoo()

  } catch (error) {

    console.error(
        "Callback Payoo lỗi:",
        error
    )


    payooStatus.value =
        "waiting"


    ElMessage.error(
        getErrorMessage(
            error,
            "Thanh toán Payoo thất bại"
        )
    )
  }
}


// =====================================================
// RESET PAYOO
// =====================================================

const resetPayoo = () => {

  currentTransaction.value =
      null


  qrImage.value =
      ""


  payooStatus.value =
      "waiting"
}


// =====================================================
// OPEN RÚT QUỸ
// =====================================================

const openWithdrawFundDialog = () => {

  const available =
      Number(
          finance
              .value
              ?.soDuQuyKhaDung

          || 0
      )


  if (
      available < 1000
  ) {

    ElMessage.warning(
        "Quỹ khả dụng không đủ để rút"
    )

    return
  }


  withdrawType.value =
      "fund"


  amount.value =
      Math.min(
          available,
          1000000
      )


  withdrawVisible.value =
      true
}


// =====================================================
// OPEN RÚT VÍ
// =====================================================

const openWithdrawWalletDialog = () => {

  const walletBalance =
      Number(
          finance
              .value
              ?.soDuVi

          || 0
      )


  if (
      walletBalance < 1000
  ) {

    ElMessage.warning(
        "Số dư Ví không đủ để rút"
    )

    return
  }


  withdrawType.value =
      "wallet"


  amount.value =
      Math.min(
          walletBalance,
          1000000
      )


  withdrawVisible.value =
      true
}


// =====================================================
// RÚT QUỸ / RÚT VÍ
// =====================================================

const handleWithdraw = async () => {

  const soTien =
      Number(
          amount.value || 0
      )


  if (
      soTien < 1000
  ) {

    ElMessage.warning(
        "Số tiền rút tối thiểu là 1.000đ"
    )

    return
  }


  const maxValue =
      withdrawType.value === "fund"

          ? Number(
              finance
                  .value
                  ?.soDuQuyKhaDung
              || 0
          )

          : Number(
              finance
                  .value
                  ?.soDuVi
              || 0
          )


  if (
      soTien > maxValue
  ) {

    ElMessage.warning(
        withdrawType.value === "fund"
            ? "Số tiền vượt Quỹ khả dụng"
            : "Số tiền vượt số dư Ví"
    )

    return
  }


  submitting.value =
      true


  try {

    let transaction


    /*
     * Bước 1:
     * tạo giao dịch Payoo.
     */
    if (
        withdrawType.value
        === "fund"
    ) {

      transaction =
          await taoPayooRutQuy(
              soTien
          )

    } else {

      transaction =
          await taoPayooRutVi(
              soTien
          )
    }


    withdrawVisible.value =
        false


    ElMessage.info(
        "Đang xử lý chuyển tiền..."
    )


    /*
     * Giả lập Payoo payout.
     */
    await delay(
        1400
    )


    /*
     * Backend callback.
     *
     * Nếu rút Quỹ:
     * SoDuQuy -= tiền.
     *
     * Nếu rút Ví:
     * SoDuVi -= tiền.
     */
    await confirmPayooTransaction(
        transaction.maGiaoDich
    )


    await loadData()


    await loadHistory()


    ElMessage.success(
        withdrawType.value === "fund"
            ? "Rút Quỹ thành công"
            : "Rút Ví thành công"
    )

  } catch (error) {

    console.error(
        "Rút tiền lỗi:",
        error
    )


    ElMessage.error(
        getErrorMessage(
            error,
            "Rút tiền qua Payoo thất bại"
        )
    )

  } finally {

    submitting.value =
        false
  }
}


// =====================================================
// OPEN VÍ -> QUỸ
// =====================================================

const openTransferDialog = () => {

  if (
      !finance
          .value
          ?.daMoQuy
  ) {

    ElMessage.warning(
        "Bạn cần mở Quỹ trước"
    )

    return
  }


  const wallet =
      Number(
          finance
              .value
              ?.soDuVi

          || 0
      )


  if (
      wallet < 1000
  ) {

    ElMessage.warning(
        "Ví không đủ số dư để chuyển"
    )

    return
  }


  amount.value =
      Math.min(
          wallet,
          1000000
      )


  transferVisible.value =
      true
}


// =====================================================
// CHUYỂN VÍ -> QUỸ
// =====================================================

const handleTransfer = async () => {

  const soTien =
      Number(
          amount.value || 0
      )


  if (
      soTien < 1000
  ) {

    ElMessage.warning(
        "Số tiền chuyển tối thiểu là 1.000đ"
    )

    return
  }


  const wallet =
      Number(
          finance
              .value
              ?.soDuVi

          || 0
      )


  if (
      soTien > wallet
  ) {

    ElMessage.warning(
        "Số dư Ví không đủ"
    )

    return
  }


  submitting.value =
      true


  try {

    finance.value =
        await chuyenViVaoQuy(
            soTien
        )


    transferVisible.value =
        false


    await loadHistory()


    ElMessage.success(
        "Chuyển tiền từ Ví vào Quỹ thành công"
    )

  } catch (error) {

    console.error(
        "Chuyển Ví -> Quỹ lỗi:",
        error
    )


    ElMessage.error(
        getErrorMessage(
            error,
            "Chuyển tiền thất bại"
        )
    )

  } finally {

    submitting.value =
        false
  }
}


// =====================================================
// MOUNT
// =====================================================

onMounted(() => {
  loadData()
  loadHistory()
})

</script>


<style scoped>

/* =====================================================
   PAGE
===================================================== */

.finance-page {
  min-height: 100%;
  padding: 26px 30px 40px;
  background: #f6f7f9;
  color: #263238;
}


/* =====================================================
   HEADER
===================================================== */

.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 22px;
}

.eyebrow {
  color: #8a1023;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
}

.page-heading h2 {
  margin: 5px 0 6px;
  font-size: 28px;
  font-weight: 800;
}

.page-heading p {
  margin: 0;
  color: #747c83;
  font-size: 14px;
}


/* =====================================================
   HEADER ACTIONS
===================================================== */

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* =====================================================
   REFRESH
===================================================== */

.refresh-btn {
  min-height: 40px;
  padding: 0 14px;

  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;

  border: 1px solid #e2e5e9;
  border-radius: 9px;

  background: #fff;
  color: #4e565c;

  cursor: pointer;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* =====================================================
   LỊCH SỬ GIAO DỊCH
===================================================== */

.history-card {
  margin-top: 20px;
}

.history-icon {
  background: #e0e7ff;
  color: #4f46e5;
}

.history-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 15px 0;
}

.timeline-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.timeline-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 14px;
  flex-shrink: 0;
}

.timeline-icon.green {
  color: #16a34a;
  background: #dcfce7;
}

.timeline-icon.orange {
  color: #ea580c;
  background: #ffedd5;
}

.timeline-icon.blue {
  color: #2563eb;
  background: #dbeafe;
}

.timeline-icon.red {
  color: #dc2626;
  background: #fee2e2;
}

.timeline-content {
  flex: 1;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.timeline-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.timeline-top h4 {
  margin: 0;
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
}

.timeline-top span {
  color: #6b7280;
  font-size: 12px;
}

.timeline-content p {
  margin: 0 0 8px;
  color: #4b5563;
  font-size: 13px;
}

.timeline-amount {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.amount-plus {
  color: #16a34a;
  background: #dcfce7;
}

.amount-minus {
  color: #dc2626;
  background: #fee2e2;
}


/* =====================================================
   LOADING
===================================================== */

.loading-card {
  min-height: 180px;

  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;

  background: #fff;

  border: 1px solid #e7e9ec;
  border-radius: 15px;

  color: #777f85;
}

.loading-card i {
  color: #8a1023;
  font-size: 22px;
}


/* =====================================================
   GRID
===================================================== */

.finance-grid {
  display: grid;

  grid-template-columns:
    minmax(0, 1.15fr)
    minmax(330px, 0.85fr);

  gap: 20px;
}


/* =====================================================
   CARD
===================================================== */

.finance-card {
  padding: 24px;

  background: #fff;

  border: 1px solid #e7e9ec;
  border-radius: 15px;

  box-shadow:
      0 6px 22px
      rgba(30, 38, 45, 0.05);
}


/* =====================================================
   CARD HEADER
===================================================== */

.card-head {
  display: flex;
  align-items: center;
  gap: 12px;

  padding-bottom: 18px;

  border-bottom: 1px solid #eef0f2;
}

.icon-box {
  width: 46px;
  height: 46px;

  flex: 0 0 46px;

  display: grid;
  place-items: center;

  border-radius: 12px;

  background: #fff0f2;
  color: #8a1023;

  font-size: 19px;
}

.wallet-icon {
  background: #eef5f7;
  color: #244b5a;
}

.head-info {
  flex: 1;
}

.head-info span {
  color: #8b9298;

  font-size: 11px;
  font-weight: 800;
}

.head-info h3 {
  margin: 4px 0 0;

  font-size: 18px;
}


/* =====================================================
   STATUS
===================================================== */

.status-pill {
  padding: 6px 10px;

  border-radius: 999px;

  font-size: 11px;
  font-weight: 700;

  white-space: nowrap;
}

.status-pill.active {
  background: #eaf7ef;
  color: #217a42;
}

.status-pill.inactive {
  background: #f2f3f5;
  color: #737b81;
}


/* =====================================================
   BALANCE
===================================================== */

.main-balance {
  padding: 24px 0 18px;
}

.main-balance span {
  display: block;

  margin-bottom: 6px;

  color: #747d85;

  font-size: 13px;
}

.main-balance strong {
  color: #8a1023;

  font-size: 31px;
  font-weight: 800;
}

.wallet-balance strong {
  color: #244b5a;
}


/* =====================================================
   BALANCE ROW
===================================================== */

.balance-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  margin-bottom: 16px;
}

.balance-box {
  padding: 14px;

  background: #fafbfc;

  border: 1px solid #eceef0;
  border-radius: 10px;
}

.balance-box span {
  display: block;

  margin-bottom: 6px;

  color: #7b838a;

  font-size: 12px;
}

.balance-box strong {
  color: #2f3a40;

  font-size: 16px;
}

.green {
  color: #29985a;
}

.orange {
  color: #c28827;
}


/* =====================================================
   FUND NOTE
===================================================== */

.fund-note {
  display: flex;
  gap: 9px;

  padding: 13px 14px;
  margin-bottom: 18px;

  border-radius: 10px;

  background: #fff8ed;

  color: #735f39;

  font-size: 13px;
  line-height: 1.55;
}


/* =====================================================
   ACTION
===================================================== */

.finance-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.primary-btn,
.outline-btn,
.wallet-btn {
  min-height: 42px;

  padding: 10px 14px;

  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;

  border-radius: 9px;

  font-weight: 700;

  cursor: pointer;

  transition:
      0.2s ease;
}

.primary-btn {
  width: 100%;

  border: 1px solid #8a1023;

  background: #8a1023;

  color: #fff;
}

.primary-btn:hover:not(:disabled) {
  background: #700c1b;
}

.outline-btn {
  border: 1px solid #8a1023;

  background: #fff;

  color: #8a1023;
}

.outline-btn:hover {
  background: #fff4f5;
}

.wallet-btn {
  border: 1px solid #244b5a;

  background: #244b5a;

  color: #fff;
}

.wallet-btn:hover {
  background: #193c48;
}

.action-note {
  margin: 13px 0 0;

  color: #828a90;

  font-size: 12px;
  line-height: 1.5;
}


/* =====================================================
   EMPTY FUND
===================================================== */

.empty-fund {
  padding: 34px 16px 26px;

  text-align: center;
}

.empty-fund > i {
  margin-bottom: 14px;

  color: #c6b39e;

  font-size: 42px;
}

.empty-fund h4 {
  margin: 0 0 8px;
}

.empty-fund p {
  color: #7b8288;

  font-size: 13px;
  line-height: 1.6;
}


/* =====================================================
   WALLET FLOW
===================================================== */

.wallet-flow {
  display: grid;
  gap: 8px;

  padding: 8px 0 18px;
}

.wallet-flow > div {
  display: flex;
  align-items: center;
  gap: 10px;

  padding: 11px 12px;

  border-radius: 9px;

  background: #f7f9fa;

  color: #4b555c;

  font-size: 13px;
}

.wallet-flow > div i {
  width: 22px;

  color: #8a1023;

  text-align: center;
}

.flow-arrow {
  margin-left: 20px;

  color: #adb4ba;
}


/* =====================================================
   MONEY FORM
===================================================== */

.money-form label {
  display: block;

  margin-bottom: 9px;

  font-weight: 700;
}

.money-input {
  width: 100%;
}


/* =====================================================
   QUICK MONEY
===================================================== */

.quick-money {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 7px;

  margin-top: 10px;
}

.quick-money button {
  padding: 7px 4px;

  border: 1px solid #e1e4e7;
  border-radius: 7px;

  background: #f8f9fa;
  color: #596168;

  font-size: 12px;

  cursor: pointer;
}

.quick-money button:hover {
  border-color: #8a1023;

  color: #8a1023;
}


/* =====================================================
   DIALOG NOTE
===================================================== */

.dialog-note {
  display: flex;
  gap: 9px;

  margin-top: 16px;
  padding: 12px;

  border-radius: 9px;

  background: #fff7e9;

  color: #745f37;

  font-size: 12px;
  line-height: 1.55;
}

.green-note {
  background: #edf8f1;

  color: #317049;
}


/* =====================================================
   DIALOG BUTTON
===================================================== */

.dialog-cancel,
.dialog-confirm {
  min-height: 38px;

  padding: 9px 16px;

  border: none;
  border-radius: 8px;

  font-weight: 700;

  cursor: pointer;
}

.dialog-cancel {
  margin-right: 8px;

  background: #eef0f2;

  color: #535c62;
}

.dialog-confirm {
  background: #8a1023;

  color: #fff;
}

.dialog-confirm:disabled,
.dialog-cancel:disabled {
  opacity: 0.6;

  cursor: not-allowed;
}


/* =====================================================
   PAYOO
===================================================== */

.payoo-box {
  padding: 4px 15px 20px;

  text-align: center;
}

.payoo-logo {
  color: #e0202b;

  font-size: 32px;
  font-weight: 900;

  letter-spacing: 1px;
}

.payoo-sub {
  margin-top: 2px;

  color: #899197;

  font-size: 10px;
  font-weight: 700;

  letter-spacing: 2px;
}

.payoo-title {
  margin-top: 23px;

  color: #3c454a;

  font-size: 16px;
}

.payoo-description {
  margin: 6px 0 16px;

  color: #777f85;

  font-size: 13px;
}


/* =====================================================
   QR
===================================================== */

.payoo-qr {
  width: 250px;
  max-width: 100%;

  padding: 10px;

  border: 2px solid #eee;
  border-radius: 14px;

  cursor: pointer;

  transition:
      transform 0.2s ease,
      border-color 0.2s ease,
      box-shadow 0.2s ease;
}

.payoo-qr:hover {
  transform: scale(1.025);

  border-color: #e0202b;

  box-shadow:
      0 10px 35px
      rgba(224, 32, 43, 0.15);
}

.qr-hint {
  margin-top: 9px;

  color: #e0202b;

  font-size: 13px;
  font-weight: 700;
}

.payoo-amount {
  margin-top: 18px;

  color: #8a1023;

  font-size: 25px;
  font-weight: 900;
}

.payoo-code {
  display: grid;
  gap: 4px;

  margin-top: 10px;

  color: #7d858a;

  font-size: 11px;
}

.payoo-code strong {
  color: #555e64;

  font-family: monospace;

  overflow-wrap: anywhere;
}

.mock-note {
  display: flex;
  gap: 8px;

  margin-top: 18px;
  padding: 11px;

  border-radius: 8px;

  background: #fff8e9;

  color: #826c3d;

  font-size: 12px;
  line-height: 1.5;

  text-align: left;
}


/* =====================================================
   PROCESS
===================================================== */

.processing-state {
  padding: 45px 10px 30px;
}

.processing-state > i {
  color: #e0202b;

  font-size: 50px;
}

.processing-state h3 {
  margin: 18px 0 7px;
}

.processing-state p {
  color: #7c8489;

  font-size: 13px;
}

.processing-state strong {
  display: block;

  margin-top: 13px;

  color: #8a1023;

  font-size: 22px;
}


/* =====================================================
   SUCCESS
===================================================== */

.success-state {
  padding: 40px 10px 25px;
}

.success-state > i {
  color: #259c55;

  font-size: 65px;
}

.success-state h3 {
  margin: 16px 0 10px;

  color: #259c55;
}

.success-state > strong {
  display: block;

  margin-bottom: 10px;

  font-size: 24px;
}

.success-state p {
  max-width: 300px;

  margin: 0 auto 12px;

  color: #747c81;

  font-size: 13px;
  line-height: 1.55;
}

.success-state small {
  color: #949a9e;

  font-family: monospace;
}


/* =====================================================
   PAYOO MINI
===================================================== */

.payoo-mini {
  display: flex;
  align-items: center;
  justify-content: space-between;

  margin-bottom: 17px;
  padding: 11px 13px;

  border-radius: 9px;

  background: #fafafa;
}

.payoo-mini strong {
  color: #e0202b;

  font-size: 20px;
  font-weight: 900;
}

.payoo-mini span {
  color: #8a9297;

  font-size: 11px;
}


/* =====================================================
   AVAILABLE
===================================================== */

.available-box {
  margin-bottom: 18px;
  padding: 14px;

  border-radius: 10px;

  background: #f7f9fa;
}

.available-box span {
  display: block;

  margin-bottom: 5px;

  color: #7d858b;

  font-size: 12px;
}

.available-box strong {
  color: #8a1023;

  font-size: 20px;
}


/* =====================================================
   TRANSFER
===================================================== */

.transfer-summary {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 10px;

  margin-bottom: 20px;
}

.transfer-summary > div {
  padding: 12px;

  border: 1px solid #eceef0;
  border-radius: 9px;

  background: #fafbfc;
}

.transfer-summary span {
  display: block;

  margin-bottom: 4px;

  color: #7b838a;

  font-size: 11px;
}

.transfer-summary strong {
  font-size: 14px;
}

.transfer-summary > i {
  color: #8a1023;
}


/* =====================================================
   RESPONSIVE
===================================================== */

@media (max-width: 980px) {

  .finance-grid {
    grid-template-columns: 1fr;
  }
}


@media (max-width: 640px) {

  .finance-page {
    padding: 18px 14px 30px;
  }

  .page-heading {
    flex-direction: column;
  }

  .refresh-btn {
    width: 100%;
  }

  .balance-row {
    grid-template-columns: 1fr;
  }

  .finance-actions {
    grid-template-columns: 1fr;
  }

  .quick-money {
    grid-template-columns: 1fr 1fr;
  }

  .transfer-summary {
    grid-template-columns: 1fr;
  }

  .transfer-summary > i {
    transform: rotate(90deg);

    justify-self: center;
  }
}

</style>