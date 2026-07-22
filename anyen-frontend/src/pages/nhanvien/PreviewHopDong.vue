<script setup>
import { computed } from "vue";

const props = defineProps({
  contract: {
    type: Object,
    required: false,
    default: () => ({}),
  },

  extraServices: {
    type: Array,
    required: false,
    default: () => [],
  },

  orderProducts: {
    type: Array,
    required: false,
    default: () => [],
  },
});

const safeNumber = (value) => Number(value || 0);

/**
 * Parse ngày an toàn, tránh lệch ngày do múi giờ.
 *
 * Backend trả YYYY-MM-DD thì không dùng trực tiếp new Date("YYYY-MM-DD"),
 * vì trình duyệt có thể hiểu theo UTC.
 */
const parseDate = (value) => {
  if (!value) {
    return null;
  }

  if (value instanceof Date) {
    return Number.isNaN(value.getTime())
        ? null
        : value;
  }

  const rawValue = String(value).trim();

  /*
   * Dạng backend:
   * 2026-07-22
   */
  const isoDateMatch = rawValue.match(
      /^(\d{4})-(\d{2})-(\d{2})$/
  );

  if (isoDateMatch) {
    const [, year, month, day] = isoDateMatch;

    return new Date(
        Number(year),
        Number(month) - 1,
        Number(day)
    );
  }

  /*
   * Dạng backend có thời gian:
   * 2026-07-22T16:13:00
   * 2026-07-22 16:13:00
   */
  const isoDateTimeMatch = rawValue.match(
      /^(\d{4})-(\d{2})-(\d{2})[T\s](\d{1,2}):(\d{2})(?::(\d{2}))?/
  );

  if (isoDateTimeMatch) {
    const [
      ,
      year,
      month,
      day,
      hour,
      minute,
      second = "0",
    ] = isoDateTimeMatch;

    return new Date(
        Number(year),
        Number(month) - 1,
        Number(day),
        Number(hour),
        Number(minute),
        Number(second)
    );
  }

  /*
   * Dạng đang dùng khi tạo hợp đồng:
   * 16:13 22/07/2026
   */
  const timeFirstMatch = rawValue.match(
      /^(\d{1,2}):(\d{2})(?::(\d{2}))?\s+(\d{1,2})\/(\d{1,2})\/(\d{4})$/
  );

  if (timeFirstMatch) {
    const [
      ,
      hour,
      minute,
      second = "0",
      day,
      month,
      year,
    ] = timeFirstMatch;

    return new Date(
        Number(year),
        Number(month) - 1,
        Number(day),
        Number(hour),
        Number(minute),
        Number(second)
    );
  }

  /*
   * Dạng:
   * 22/07/2026 16:13
   */
  const dateFirstMatch = rawValue.match(
      /^(\d{1,2})\/(\d{1,2})\/(\d{4})(?:\s+(\d{1,2}):(\d{2})(?::(\d{2}))?)?$/
  );

  if (dateFirstMatch) {
    const [
      ,
      day,
      month,
      year,
      hour = "0",
      minute = "0",
      second = "0",
    ] = dateFirstMatch;

    return new Date(
        Number(year),
        Number(month) - 1,
        Number(day),
        Number(hour),
        Number(minute),
        Number(second)
    );
  }

  const date = new Date(rawValue);

  return Number.isNaN(date.getTime())
      ? null
      : date;
};
/**
 * Ngày viết hợp đồng trên đầu PDF.
 * Chỉ lấy ngày từ hợp đồng, không lấy ngày hiện tại.
 */
const contractWriteDate = computed(() => {
  return parseDate(
      props.contract.contractDate ||
      props.contract.ngayViet ||
      ""
  );
});

const contractWriteDay = computed(() => {
  const date = contractWriteDate.value;

  return date
      ? String(date.getDate()).padStart(2, "0")
      : "......";
});

const contractWriteMonth = computed(() => {
  const date = contractWriteDate.value;

  return date
      ? String(date.getMonth() + 1).padStart(2, "0")
      : "......";
});

const contractWriteYear = computed(() => {
  const date = contractWriteDate.value;

  return date
      ? String(date.getFullYear())
      : "......";
});

const productsTotal = computed(() => {
  return props.orderProducts.reduce((sum, item) => {
    const price = safeNumber(item.price);
    const quantity = safeNumber(item.quantity);
    const thanhTien = safeNumber(item.thanhTien);

    return sum + (
        thanhTien > 0
            ? thanhTien
            : price * quantity
    );
  }, 0);
});

const extraServicesTotal = computed(() => {
  return props.extraServices.reduce((sum, item) => {
    return sum + safeNumber(item.price);
  }, 0);
});

const grandTotal = computed(() => {
  return (
      productsTotal.value +
      extraServicesTotal.value
  );
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat("vi-VN").format(
      safeNumber(value)
  );
};

const formatDate = (value) => {
  const date = parseDate(value);

  if (!date) {
    return "";
  }

  return date.toLocaleDateString("vi-VN");
};

const formatDateTime = (value) => {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

/**
 * Ngày bắt đầu thực hiện hợp đồng.
 *
 * Không fallback sang ngayViet vì ngày viết và ngày thực hiện
 * là hai trường khác nhau.
 */
const previewContractStartDate = computed(() => {
  return (
      props.contract.contractStartDate ||
      props.contract.ngayKyHD ||
      props.contract.executionDate ||
      ""
  );
});

const previewContractEndDate = computed(() => {
  return (
      props.contract.contractEndDate ||
      props.contract.thoiHanKetThuc ||
      props.contract.ngayKetThuc ||
      props.contract.ngayHetHan ||
      ""
  );
});

const readVietnameseNumber = (number) => {
  let num = Math.round(safeNumber(number));

  if (num === 0) {
    return "Không đồng";
  }

  const units = [
    "",
    "nghìn",
    "triệu",
    "tỷ",
    "nghìn tỷ",
    "triệu tỷ",
  ];

  const ones = [
    "không",
    "một",
    "hai",
    "ba",
    "bốn",
    "năm",
    "sáu",
    "bảy",
    "tám",
    "chín",
  ];

  const readBlock = (value, full) => {
    let result = "";

    const hundred = Math.floor(value / 100);
    const remainder = value % 100;
    const ten = Math.floor(remainder / 10);
    const one = remainder % 10;

    if (full || hundred > 0) {
      result += `${ones[hundred]} trăm `;
    }

    if (ten === 0 && one > 0) {
      if (full || hundred > 0) {
        result += "lẻ ";
      }
    } else if (ten === 1) {
      result += "mười ";
    } else if (ten > 1) {
      result += `${ones[ten]} mươi `;
    }

    if (ten > 1 && one === 1) {
      result += "mốt ";
    } else if (ten > 0 && one === 5) {
      result += "lăm ";
    } else if (one > 0) {
      result += `${ones[one]} `;
    }

    return result.trim();
  };

  let result = "";
  let index = 0;

  while (num > 0) {
    const block = num % 1000;

    if (block > 0) {
      const blockText = readBlock(
          block,
          index > 0 && num > 1000
      );

      result =
          `${blockText} ${units[index]} ${result}`;
    }

    num = Math.floor(num / 1000);
    index++;
  }

  result = result
      .trim()
      .replace(/\s+/g, " ");

  return (
      result.charAt(0).toUpperCase() +
      result.slice(1) +
      " đồng"
  );
};

const grandTotalInWords = computed(() => {
  return readVietnameseNumber(
      grandTotal.value
  );
});
</script>
<template>
  <div class="preview-wrapper">
    <div class="contract-paper">

      <!-- Header -->
      <div class="header-row">
        <div class="header-left">
          <div>SỞ LAO ĐỘNG - THƯƠNG BINH VÀ XÃ HỘI HỒ CHÍ MINH</div>
          <div class="font-bold">BAN PHỤC VỤ LỄ TANG AN YÊN</div>
        </div>
        <div class="header-right">
          <div class="font-bold">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</div>
          <div class="font-bold">Độc lập – Tự do – Hạnh phúc</div>
          <div class="separator-line"></div>
        </div>
      </div>

      <div class="meta-row">
        <div class="meta-left">
          <div class="flex-row">
            <span style="width: 55px;">Số ĐH:</span>
            <span class="dotted-input flex-grow">{{ contract.orderCode }}</span>
          </div>
          <div class="flex-row mt-1">
            <span style="width: 55px;">Số HĐ:</span>
            <span class="dotted-input flex-grow">{{ contract.contractCode }}</span>
          </div>
        </div>
        <div class="meta-right">
          Hồ Chí Minh, ngày {{ contractWriteDay }}
          tháng {{ contractWriteMonth }}
          năm {{ contractWriteYear }}
        </div>      </div>

      <h2 class="title">HỢP ĐỒNG MAI TÁNG THI HÀI</h2>

      <div class="content">
        <div class="flex-row">
          <span class="font-bold">Bên A:&nbsp;</span>Đại diện tang chủ:
          <span class="dotted-input flex-grow">{{ contract.customerName }}</span>
          CCCD:
          <span class="dotted-input w-25">{{ contract.citizenId }}</span>
        </div>

        <div class="flex-row mt-1">
          Địa chỉ:
          <span class="dotted-input flex-grow">{{ contract.address }}</span>
          Điện thoại:
          <span class="dotted-input w-30">{{ contract.phone }}</span>
        </div>

        <div class="mt-1">
          <span class="font-bold">Bên B: Ban Phục vụ lễ tang An Yên.</span>
        </div>

        <div class="mt-1 text-justify">
          Địa chỉ: <strong>123 Tô Ký, Phường Trung Mỹ Tây, Thành phố Hồ Chí Minh</strong> &nbsp;&nbsp;&nbsp;&nbsp;Điện thoại: <strong>0826.772.109 - 0357.402.033 - 0392.168.473</strong>
        </div>

        <div class="mt-1 text-justify">
          Số tài khoản: <strong>2000211208</strong> Tại Ngân hàng TMCP Quân Đội, chi nhánh TP Hồ Chí Minh.
        </div>

        <div class="mt-1 text-justify">
          Hai bên thỏa thuận ký hợp đồng cung cấp dịch vụ theo nội dung như sau:
        </div>

        <div class="font-bold mt-2">
          Điều 1: Đối tượng của hợp đồng, thời gian thực hiện hợp đồng:
        </div>

        <div class="flex-row mt-1">
          Tên người mất:
          <span class="dotted-input flex-grow">{{ contract.deceasedName }}</span>

          Giới tính:
          <span class="dotted-input flex-grow">{{ contract.gender }}</span>
        </div>

        <div class="flex-row mt-1">
          Ngày sinh:
          <span class="dotted-input flex-grow">{{ formatDate(contract.birthDate) }}</span>

          Ngày mất:
          <span class="dotted-input flex-grow">{{ formatDate(contract.deathDate) }}</span>
        </div>

        <div class="flex-row mt-1">
          Số giấy báo tử:
          <span class="dotted-input flex-grow">{{ contract.deathCertificateNo  }}</span>
          Nơi cấp giấy báo tử:
          <span class="dotted-input flex-grow">{{ contract.deathCertificateIssuePlace }}</span>
        </div>

        <div class="flex-row mt-1">
          Được mai táng tại:
          <span class="dotted-input flex-grow">{{ contract.facility }}</span>
          Ngày giờ mai táng:
          <span class="dotted-input flex-grow">{{ formatDateTime(contract.burialDatetime) }}</span>
        </div>

        <div class="flex-row mt-1">
          Khu mộ:
          <span class="dotted-input flex-grow">{{ contract.cemeteryArea }}</span>
          Số mộ:
          <span class="dotted-input w-30">{{ contract.graveNumber }}</span>
        </div>

        <div class="flex-row mt-1">
          Thời gian thực hiện hợp đồng:
          từ ngày
          <span class="dotted-input flex-grow">
    {{ formatDate(previewContractStartDate) }}
  </span>
          đến ngày
          <span class="dotted-input flex-grow">
    {{ formatDate(previewContractEndDate) }}
  </span>
        </div>

        <div class="font-bold mt-2">
          Điều 2: Bên B cung cấp cho bên A các dịch vụ theo nội dung như sau:
        </div>

        <div class="mt-1">
          1/ Mai táng:
        </div>

        <table class="service-table">
          <thead>
          <tr>
            <th class="w-stt">STT</th>
            <th>Tên dịch vụ</th>
            <th class="w-sl">Số lượng</th>
            <th class="w-dg">Đơn giá</th>
            <th class="w-tt">Thành tiền</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(item, index) in orderProducts" :key="index">
            <td class="text-center">{{ index + 1 }}</td>
            <td>{{ item.name }}</td>
            <td class="text-center">{{ item.quantity }}</td>
            <td class="text-right">{{ formatCurrency(item.price) }}</td>
            <td class="text-right">{{ formatCurrency(item.quantity * item.price) }}</td>
          </tr>
          <tr v-if="orderProducts.length === 0">
            <td colspan="5" class="text-center">Chưa có sản phẩm / dịch vụ</td>
          </tr>
          </tbody>
        </table>

        <div style="display: flex; margin-top: 4px;">
          <div style="width: 100px;">2/ Thu khác:</div>
          <div style="flex-grow: 1;">
            <!-- Render empty placeholder if no extra services -->
            <div v-if="extraServices.length === 0" style="display: flex; justify-content: space-between;">
              <div>....................................................................</div>
              <div style="display: flex; width: 250px; justify-content: space-between;">
                <span>Thành tiền:</span>
                <span style="width: 100px; text-align: right; padding-right: 25px;">0</span>
              </div>
            </div>

            <!-- Render each extra service -->
            <div v-for="(srv, i) in extraServices" :key="i" style="display: flex; justify-content: space-between; margin-bottom: 4px;">
              <div>+ {{ srv.name || '........................................' }}</div>
              <div style="display: flex; width: 250px; justify-content: space-between;">
                <span>Thành tiền:</span>
                <span style="width: 100px; text-align: right; padding-right: 25px;">{{ formatCurrency(srv.price || 0) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div style="display: flex; margin-top: 4px;">
          <div style="width: 100px;"></div>
          <div style="flex-grow: 1; display: flex; justify-content: space-between; font-weight: bold;">
            <div style="text-align: center; flex-grow: 1;">Tổng cộng:</div>
            <div style="display: flex; width: 250px; justify-content: space-between;">
              <span>Thành tiền:</span>
              <span style="width: 100px; text-align: right; padding-right: 25px;">{{ formatCurrency(grandTotal) }}</span>
            </div>
          </div>
        </div>

        <div class="font-bold mt-2">
          Tổng giá trị hợp đồng thanh toán:
        </div>

        <div class="flex-row mt-1">
          <span class="font-bold">Bằng chữ:</span>
          <span style="border-bottom: 2px dotted #000; flex-grow: 1; margin: 0 8px; font-style: italic; line-height: 24px; min-height: 24px; padding-left: 8px;">
            {{ grandTotalInWords }}
          </span>
        </div>

        <div class="font-bold mt-2">
          Điều 3: Phương thức thanh toán:
        </div>
        <div class="mt-1 text-justify">
          Thanh toán 100% giá trị hợp đồng cho bên B bằng tiền mặt hoặc chuyển khoản.
        </div>

        <div class="font-bold mt-2">
          Điều 4: Trách nhiệm của các bên:
        </div>

        <div class="font-bold mt-1">
          4.1 Trách nhiệm Bên A:
        </div>
        <div class="mt-1 text-justify">
          - Thông báo kịp thời cho Bên B khi muốn hủy, thay đổi, bổ sung các nội dung trong hợp đồng. Những thay đổi, bổ sung sẽ được thống nhất giữa hai bên trong hợp đồng bổ sung. Trường hợp muốn hủy một phần hoặc toàn bộ hợp đồng, Bên A phải trả cho bên B chi phí thiệt hại theo quy định hiện hành của Bên B. Bên B sẽ trừ trực tiếp chi phí thiệt hại vào số tiền do Bên A đã thanh toán.
        </div>

        <div class="font-bold mt-2">
          4.2 Trách nhiệm Bên B:
        </div>
        <div class="mt-1 text-justify">
          - Đảm bảo cung cấp dịch vụ theo đúng nội dung ghi tại Điều 1.<br/>
          - Cung cấp cho Bên B hóa đơn tài chính theo quy định.
        </div>

        <div class="font-bold mt-2">
          Điều 5: Điều khoản khác:
        </div>
        <div class="mt-1 text-justify">
          - Hợp đồng này có hiệu lực kể từ ngày ký và đương nhiên được thanh lý khi các bên đã hoàn thành các nghĩa vụ của mình trong Hợp đồng. Trong quá trình thực hiện nếu có khó khăn, vướng mắc hai bên gặp nhau bàn bạc để đi đến thống nhất.
        </div>
        <div class="mt-1 text-justify">
          Hợp đồng này được lập thành 02 bản, mỗi Bên giữ 01 bản có giá trị pháp lý như nhau.
        </div>

        <div class="signature-row mt-4">
          <div class="font-bold text-center" style="width: 50%;">Đại diện bên A</div>
          <div class="font-bold text-center" style="width: 50%;">Đại diện bên B</div>
        </div>
        <div style="height: 100px;"></div>
      </div>
    </div>
  </div>
</template>
<style scoped src="../../assets/styles/components/PreviewHopDong.css"></style>