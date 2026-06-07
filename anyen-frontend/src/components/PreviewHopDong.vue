<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  contract: {
    type: Object,
    required: false,
    default: () => ({})
  },
  extraServices: {
    type: Array,
    required: false,
    default: () => []
  }
});

const mockServices = ref([
  { name: 'Xe phục vụ tang lễ', quantity: 1, price: 1500000 },
  { name: 'Áo quan', quantity: 1, price: 8000000 },
  { name: 'Vòng hoa', quantity: 3, price: 500000 }
]);

const mockServicesTotal = computed(() => {
  return mockServices.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const extraServicesTotal = computed(() => {
  return props.extraServices.reduce((sum, item) => sum + (Number(item.price) || 0), 0);
});

const grandTotal = computed(() => {
  return mockServicesTotal.value + extraServicesTotal.value;
});

const readVietnameseNumber = (num) => {
  if (num === 0) return 'Không đồng';
  const units = ["", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"];
  const ones = ["không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"];

  function readBlock(n, full) {
    let str = "";
    let hundred = Math.floor(n / 100);
    let n100 = n % 100;
    if (full || hundred > 0) {
      str += ones[hundred] + " trăm ";
    }
    let ten = Math.floor(n100 / 10);
    let one = n100 % 10;
    
    if (ten === 0 && one > 0) {
      if (full || hundred > 0) {
        str += "lẻ ";
      }
    } else if (ten === 1) {
      str += "mười ";
    } else if (ten > 1) {
      str += ones[ten] + " mươi ";
    }

    if (ten > 1 && one === 1) {
      str += "mốt ";
    } else if (ten > 0 && one === 5) {
      str += "lăm ";
    } else if (one > 0) {
      str += ones[one] + " ";
    }
    return str.trim();
  }

  let str = "";
  let i = 0;
  while (num > 0) {
    let block = num % 1000;
    if (block > 0) {
      let blockStr = readBlock(block, i > 0 && num > 1000);
      str = blockStr + " " + units[i] + " " + str;
    }
    num = Math.floor(num / 1000);
    i++;
  }
  str = str.trim().replace(/\s+/g, ' ');
  return str.charAt(0).toUpperCase() + str.slice(1);
};

const grandTotalInWords = computed(() => {
  return readVietnameseNumber(grandTotal.value);
});

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN').format(value);
};

const formatDate = (date) => {
  if (!date) return "";
  return new Date(date).toLocaleDateString("vi-VN");
};
</script>

<template>
  <div class="preview-wrapper">
    <div class="contract-paper">
      
      <!-- Header -->
      <div class="header-row">
        <div class="header-left">
          <div>SỞ LAO ĐỘNG - THƯƠNG BINH VÀ XÃ HỘI HÀ NỘI</div>
          <div class="font-bold">BAN PHỤC VỤ LỄ TANG HÀ NỘI</div>
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
            <input type="text" class="dotted-input flex-grow" v-model="contract.orderCode" />
          </div>
          <div class="flex-row mt-1">
            <span style="width: 55px;">Số HĐ:</span>
            <input type="text" class="dotted-input flex-grow" v-model="contract.contractCode" />
          </div>
        </div>
        <div class="meta-right">
          Hà Nội, ngày.... tháng.... năm 2026
        </div>
      </div>

      <h2 class="title">HỢP ĐỒNG MAI TÁNG THI HÀI</h2>

      <div class="content">
        <div class="flex-row">
          <span class="font-bold">Bên A:&nbsp;</span>Đại diện tang chủ:
          <input type="text" class="dotted-input flex-grow" v-model="contract.customerName" />
          CMND:
          <input type="text" class="dotted-input w-25" v-model="contract.citizenId" />
        </div>
        
        <div class="flex-row mt-1">
          Địa chỉ:
          <input type="text" class="dotted-input flex-grow" v-model="contract.address" />
          Điện thoại:
          <input type="text" class="dotted-input w-30" v-model="contract.phone" />
        </div>

        <div class="mt-1">
          <span class="font-bold">Bên B: Ban Phục vụ lễ tang Hà Nội.</span>
        </div>

        <div class="mt-1 text-justify">
          Địa chỉ: <strong>125 Phùng Hưng - Hoàn Kiếm - Hà Nội</strong> &nbsp;&nbsp;&nbsp;&nbsp;Điện thoại: <strong>024.39232323 - 024.38285688 - 024.38255728 - 0982.012.723.</strong>
        </div>

        <div class="mt-1 text-justify">
          Số tài khoản: <strong>119000001724</strong> Tại Ngân hàng TMCP Công thương Việt Nam, chi nhánh TP Hà Nội.
        </div>

        <div class="mt-1 text-justify">
          Hai bên thỏa thuận ký hợp đồng cung cấp dịch vụ theo nội dung như sau:
        </div>

        <div class="font-bold mt-2">
          Điều 1: Đối tượng của hợp đồng, thời gian thực hiện hợp đồng:
        </div>

        <div class="flex-row mt-1">
          Tên người chết:
          <input type="text" class="dotted-input flex-grow" v-model="contract.deceasedName" />
          ngày chết:
          <input type="text" class="dotted-input w-30" :value="formatDate(contract.deathDate)" />
        </div>

        <div class="flex-row mt-1">
          Giấy báo tử, trích lục khai tử số:
          <input type="text" class="dotted-input flex-grow" v-model="contract.deathCertificateNo" />
          Nơi cấp:
          <input type="text" class="dotted-input w-25" v-model="contract.issuedPlace" />
        </div>

        <div class="flex-row mt-1">
          Được mai táng vào Khu mộ:
          <input type="text" class="dotted-input flex-grow" v-model="contract.cemeteryArea" />
          Số mộ:
          <input type="text" class="dotted-input w-30" v-model="contract.graveNumber" />
        </div>

        <div class="flex-row mt-1">
          Thời gian thực hiện hợp đồng:
          <input type="text" class="dotted-input flex-grow" :value="formatDate(contract.executionDate)" />
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
            <tr v-for="(item, index) in mockServices" :key="index">
              <td class="text-center">{{ index + 1 }}</td>
              <td>{{ item.name }}</td>
              <td class="text-center">{{ item.quantity }}</td>
              <td class="text-right">{{ formatCurrency(item.price) }}</td>
              <td class="text-right">{{ formatCurrency(item.quantity * item.price) }}</td>
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
            {{ grandTotalInWords }} đồng
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

<style scoped>
.preview-wrapper {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background-color: #f0f2f5;
}

.contract-paper {
  width: 210mm;
  min-height: 297mm;
  margin: 0 auto;
  background: white;
  padding: 20mm 15mm;
  box-shadow: 0 4px 12px rgba(0,0,0,.08);
  font-family: 'Times New Roman', Times, serif;
  font-size: 15px;
  line-height: 1.5;
  color: #000;
}

.header-row {
  display: flex;
  justify-content: space-between;
}

.header-left, .header-right {
  text-align: center;
}

.header-left {
  width: 45%;
}

.header-right {
  width: 50%;
}

.font-bold {
  font-weight: bold;
}

.separator-line {
  width: 50%;
  border-bottom: 1px solid black;
  margin: 4px auto 0;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.meta-left {
  width: 40%;
}

.meta-right {
  width: 50%;
  text-align: right;
  font-style: italic;
  align-self: flex-end;
}

.flex-row {
  display: flex;
  align-items: flex-end;
}

.flex-grow {
  flex-grow: 1;
}

.mt-1 { margin-top: 4px; }
.mt-2 { margin-top: 12px; }
.mt-4 { margin-top: 24px; }

.dotted-input {
  border: none;
  border-bottom: 2px dotted #000;
  outline: none;
  font-family: 'Times New Roman', Times, serif;
  font-size: 15px;
  background: transparent;
  padding: 0 4px;
  margin: 0 4px;
  box-sizing: border-box;
}

.w-25 { width: 25%; }
.w-30 { width: 30%; }

.title {
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  margin: 30px 0 20px;
}

.content {
  text-align: left;
}

.text-justify {
  text-align: justify;
}

.service-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 8px;
}

.service-table th,
.service-table td {
  border: 1px solid #000;
  padding: 6px;
  vertical-align: middle;
}

.service-table th {
  font-weight: normal;
  text-align: center;
}

.w-stt { width: 50px; }
.w-sl { width: 80px; }
.w-dg { width: 120px; }
.w-tt { width: 120px; }

.text-center { text-align: center; }
.text-right { text-align: right; }

.signature-row {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}
</style>
