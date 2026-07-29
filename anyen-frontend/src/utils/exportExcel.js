import * as XLSX from "xlsx";

const MONEY_FORMAT = '#,##0" ₫"';

/**
 * Chuyển dữ liệu về số an toàn.
 */
const toNumber = (value) => {
    const number = Number(value);

    return Number.isFinite(number) ? number : 0;
};

/**
 * Định dạng ngày theo dd/MM/yyyy.
 */
const formatDate = (value) => {
    if (!value) {
        return "";
    }

    const stringValue = String(value);

    // Trường hợp backend trả YYYY-MM-DD.
    if (/^\d{4}-\d{2}-\d{2}$/.test(stringValue)) {
        const [year, month, day] = stringValue.split("-");
        return `${day}/${month}/${year}`;
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return stringValue;
    }

    return date.toLocaleDateString("vi-VN");
};

/**
 * Ngăn Excel hiểu nội dung người dùng nhập là công thức.
 */
const safeText = (value) => {
    if (value === null || value === undefined) {
        return "";
    }

    const text = String(value);

    if (/^[=+\-@]/.test(text)) {
        return `'${text}`;
    }

    return text;
};

/**
 * Làm sạch tên file.
 */
const sanitizeFileName = (value) => {
    return String(value || "bao-cao")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[<>:"/\\|?*]/g, "-")
        .replace(/\s+/g, "-")
        .replace(/-+/g, "-")
        .replace(/^-|-$/g, "");
};

/**
 * Ngày hiện tại dùng trong tên file.
 */
const getCurrentDate = () => {
    const now = new Date();

    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
};

/**
 * Tạo worksheet.
 *
 * columns:
 * {
 *   header: "Tên cột",
 *   key: "tenThuocTinh",
 *   value: (item, index) => ...,
 *   type: "text" | "number" | "money" | "date",
 *   width: 20
 * }
 */
const createWorksheet = (columns, data = []) => {
    const headerRow = columns.map((column) => column.header);

    const dataRows = data.map((item, index) => {
        return columns.map((column) => {
            const value =
                typeof column.value === "function"
                    ? column.value(item, index)
                    : item?.[column.key];

            switch (column.type) {
                case "number":
                case "money":
                    return toNumber(value);

                case "date":
                    return formatDate(value);

                default:
                    return safeText(value);
            }
        });
    });

    const worksheet = XLSX.utils.aoa_to_sheet([
        headerRow,
        ...dataRows,
    ]);

    worksheet["!cols"] = columns.map((column) => ({
        wch: column.width || 18,
    }));

    if (data.length > 0) {
        const lastColumn = XLSX.utils.encode_col(
            columns.length - 1
        );

        worksheet["!autofilter"] = {
            ref: `A1:${lastColumn}${data.length + 1}`,
        };
    }

    // Định dạng những cột tiền.
    columns.forEach((column, columnIndex) => {
        if (column.type !== "money") {
            return;
        }

        for (
            let rowIndex = 1;
            rowIndex <= data.length;
            rowIndex++
        ) {
            const address = XLSX.utils.encode_cell({
                r: rowIndex,
                c: columnIndex,
            });

            if (worksheet[address]) {
                worksheet[address].z = MONEY_FORMAT;
            }
        }
    });

    return worksheet;
};

const appendSheet = (
    workbook,
    sheetName,
    columns,
    data
) => {
    const worksheet = createWorksheet(columns, data);

    XLSX.utils.book_append_sheet(
        workbook,
        worksheet,
        sheetName.substring(0, 31)
    );
};

const saveWorkbook = (workbook, fileName) => {
    XLSX.writeFile(
        workbook,
        `${sanitizeFileName(fileName)}-${getCurrentDate()}.xlsx`,
        {
            compression: true,
        }
    );
};

/* =====================================================
   DOANH THU
===================================================== */

const formatPaymentMethodForExcel = (value) => {
    const key = String(value ?? "")
        .trim()
        .toUpperCase();

    const paymentMethods = {
        "0": "Chưa chọn",
        "1": "Tiền mặt",
        "2": "Chuyển khoản",

        TIEN_MAT: "Tiền mặt",
        CHUYEN_KHOAN: "Chuyển khoản",

        "TIỀN MẶT": "Tiền mặt",
        "CHUYỂN KHOẢN": "Chuyển khoản",
    };

    return (
        paymentMethods[key] ||
        value ||
        "Chưa xác định"
    );
};

const formatRevenuePeriod = (
    value,
    kieuThongKe
) => {
    if (!value) {
        return "";
    }

    if (kieuThongKe === "NAM") {
        return `Năm ${value}`;
    }

    if (kieuThongKe === "THANG") {
        const [year, month] =
            String(value).split("-");

        return month && year
            ? `Tháng ${month}/${year}`
            : String(value);
    }

    return formatDate(value);
};

export const exportDoanhThuExcel = ({
                                        thongKe,
                                        tuNgay,
                                        denNgay,
                                        kieuThongKe = "NGAY",

                                        loaiTaiKhoan = "NHAN_VIEN",
                                        vaiTro = "",

                                        tyLeDoanhThu,
                                        doanhThuThucNhan,

                                        pageInfo = {},
                                        currentUser = {},
                                    }) => {
    if (!thongKe || !thongKe.tongQuan) {
        throw new Error(
            "Không có dữ liệu doanh thu để xuất"
        );
    }

    const workbook = XLSX.utils.book_new();

    const tongQuan = thongKe.tongQuan || {};

    const role = String(
        vaiTro ||
        loaiTaiKhoan ||
        ""
    )
        .trim()
        .toUpperCase();

    const isPartner =
        role === "DOI_TAC" ||
        role === "DOITAC";

    const isAdminRole = [
        "ADMIN",
        "QUAN_LY",
        "QUANLY",
        "1",
    ].includes(role);

    const defaultRate = isPartner
        ? 80
        : isAdminRole
            ? 20
            : 100;

    const backendRate = toNumber(
        tongQuan.tyLeDoanhThu
    );

    const displayedRate =
        tyLeDoanhThu !== undefined &&
        tyLeDoanhThu !== null
            ? toNumber(tyLeDoanhThu)
            : backendRate > 0
                ? backendRate
                : defaultRate;

    const grossRevenue = toNumber(
        tongQuan.tongDoanhThu
    );

    const backendActualRevenue = toNumber(
        tongQuan.doanhThuThucNhan
    );

    const displayedActualRevenue =
        doanhThuThucNhan !== undefined &&
        doanhThuThucNhan !== null
            ? toNumber(doanhThuThucNhan)
            : (
                backendActualRevenue > 0 ||
                grossRevenue === 0
            )
                ? backendActualRevenue
                : (
                    grossRevenue *
                    displayedRate /
                    100
                );

    const paymentData = Array.isArray(
        thongKe.phuongThucThanhToan
    )
        ? thongKe.phuongThucThanhToan
        : [];

    const totalPaymentRevenue =
        paymentData.reduce(
            (total, item) => {
                return (
                    total +
                    toNumber(item.doanhThu)
                );
            },
            0
        );

    const exportedBy =
        currentUser.hoTen ||
        currentUser.tenDangNhap ||
        currentUser.username ||
        currentUser.email ||
        "";

    /* ===========================
       SHEET 1: THÔNG TIN BÁO CÁO
    =========================== */

    const summaryRows = [
        {
            label: "Tiêu đề báo cáo",
            value:
                pageInfo.title ||
                "Thống kê doanh thu",
            type: "text",
        },
        {
            label: "Đối tượng báo cáo",
            value:
                pageInfo.subtitle ||
                role ||
                loaiTaiKhoan,
            type: "text",
        },
        {
            label: "Mô tả",
            value: pageInfo.desc || "",
            type: "text",
        },
        {
            label: "Ghi chú cách tính",
            value: pageInfo.note || "",
            type: "text",
        },
        {
            label: "Từ ngày",
            value: formatDate(
                thongKe.tuNgay || tuNgay
            ),
            type: "text",
        },
        {
            label: "Đến ngày",
            value: formatDate(
                thongKe.denNgay || denNgay
            ),
            type: "text",
        },
        {
            label: "Hiển thị theo",
            value:
                kieuThongKe === "NAM"
                    ? "Theo năm"
                    : kieuThongKe === "THANG"
                        ? "Theo tháng"
                        : "Theo ngày",
            type: "text",
        },
        {
            label: "Người xuất",
            value: exportedBy,
            type: "text",
        },
        {
            label: "Thời điểm xuất",
            value: new Date().toLocaleString(
                "vi-VN"
            ),
            type: "text",
        },
        {
            label: "Tổng giá trị đơn hàng",
            value: grossRevenue,
            type: "money",
        },
        {
            label: "Tỷ lệ doanh thu thực nhận",
            value: displayedRate,
            type: "percent",
        },
        {
            label: "Doanh thu thực nhận",
            value: displayedActualRevenue,
            type: "money",
        },
        {
            label: "Tổng hóa đơn",
            value: toNumber(
                tongQuan.tongHoaDon
            ),
            type: "number",
        },
        {
            label: "Tổng đơn hàng",
            value: toNumber(
                tongQuan.tongDonHang
            ),
            type: "number",
        },
        {
            label: "Giá trị trung bình/đơn",
            value: toNumber(
                tongQuan.doanhThuTrungBinh
            ),
            type: "money",
        },
        {
            label:
                "Tổng doanh thu theo phương thức thanh toán",
            value: totalPaymentRevenue,
            type: "money",
        },
    ];

    const summarySheet =
        XLSX.utils.aoa_to_sheet([
            [
                "NỘI DUNG",
                "GIÁ TRỊ",
            ],

            ...summaryRows.map((item) => [
                item.label,
                item.value,
            ]),
        ]);

    summarySheet["!cols"] = [
        {
            wch: 45,
        },
        {
            wch: 55,
        },
    ];

    summarySheet["!autofilter"] = {
        ref: `A1:B${summaryRows.length + 1}`,
    };

    summaryRows.forEach(
        (item, index) => {
            const rowNumber = index + 2;
            const cell =
                summarySheet[`B${rowNumber}`];

            if (!cell) {
                return;
            }

            if (item.type === "money") {
                cell.z = MONEY_FORMAT;
            }

            if (item.type === "number") {
                cell.z = "#,##0";
            }

            if (item.type === "percent") {
                cell.z = '0.##"%"';
            }
        }
    );

    XLSX.utils.book_append_sheet(
        workbook,
        summarySheet,
        "Tổng quan"
    );

    /* ===========================
       SHEET 2: DOANH THU
    =========================== */

    appendSheet(
        workbook,
        "Doanh thu theo thời gian",
        [
            {
                header: "STT",
                value: (_, index) =>
                    index + 1,
                type: "number",
                width: 8,
            },
            {
                header: "Thời gian",
                value: (item) =>
                    formatRevenuePeriod(
                        item.thoiGian,
                        kieuThongKe
                    ),
                width: 22,
            },
            {
                header: "Số đơn hàng",
                key: "soDonHang",
                type: "number",
                width: 18,
            },
            {
                header: "Doanh thu",
                key: "doanhThu",
                type: "money",
                width: 24,
            },
            {
                header:
                    "Doanh thu thực nhận",
                value: (item) => {
                    return (
                        toNumber(
                            item.doanhThu
                        ) *
                        displayedRate /
                        100
                    );
                },
                type: "money",
                width: 24,
            },
        ],
        Array.isArray(
            thongKe.bieuDoDoanhThu
        )
            ? thongKe.bieuDoDoanhThu
            : []
    );

    /* ===========================
       SHEET 3: THANH TOÁN
    =========================== */

    appendSheet(
        workbook,
        "Phương thức thanh toán",
        [
            {
                header: "STT",
                value: (_, index) =>
                    index + 1,
                type: "number",
                width: 8,
            },
            {
                header:
                    "Phương thức thanh toán",
                value: (item) =>
                    formatPaymentMethodForExcel(
                        item.phuongThucThanhToan
                    ),
                width: 30,
            },
            {
                header: "Số hóa đơn",
                key: "soHoaDon",
                type: "number",
                width: 18,
            },
            {
                header: "Doanh thu",
                key: "doanhThu",
                type: "money",
                width: 24,
            },
            {
                header: "Tỷ trọng (%)",
                value: (item) => {
                    if (
                        totalPaymentRevenue <= 0
                    ) {
                        return 0;
                    }

                    return Math.round(
                        (
                            toNumber(
                                item.doanhThu
                            ) /
                            totalPaymentRevenue *
                            100
                        ) *
                        100
                    ) / 100;
                },
                type: "number",
                width: 18,
            },
        ],
        paymentData
    );

    /* ===========================
       SHEET 4: TOP SẢN PHẨM
    =========================== */

    appendSheet(
        workbook,
        "Top sản phẩm",
        [
            {
                header: "Xếp hạng",
                value: (_, index) =>
                    index + 1,
                type: "number",
                width: 12,
            },
            {
                header: "Mã sản phẩm",
                value: (item) => {
                    if (!item.maSanPham) {
                        return "";
                    }

                    return `SP${String(
                        item.maSanPham
                    ).padStart(4, "0")}`;
                },
                width: 18,
            },
            {
                header: "Tên sản phẩm",
                key: "tenSanPham",
                width: 38,
            },
            {
                header: "Số lượng bán",
                key: "soLuongBan",
                type: "number",
                width: 18,
            },
            {
                header: "Doanh thu",
                key: "doanhThu",
                type: "money",
                width: 24,
            },
            {
                header:
                    "Doanh thu thực nhận",
                value: (item) => {
                    return (
                        toNumber(
                            item.doanhThu
                        ) *
                        displayedRate /
                        100
                    );
                },
                type: "money",
                width: 24,
            },
        ],
        Array.isArray(
            thongKe.topSanPham
        )
            ? thongKe.topSanPham
            : []
    );

    /* ===========================
       SHEET 5: TOP NHÂN VIÊN
    =========================== */

    appendSheet(
        workbook,
        "Top nhân viên",
        [
            {
                header: "Xếp hạng",
                value: (_, index) =>
                    index + 1,
                type: "number",
                width: 12,
            },
            {
                header: "Mã nhân viên",
                value: (item) => {
                    if (!item.maDoiTuong) {
                        return "";
                    }

                    return `NV${String(
                        item.maDoiTuong
                    ).padStart(4, "0")}`;
                },
                width: 18,
            },
            {
                header: "Tên nhân viên",
                key: "tenDoiTuong",
                width: 38,
            },
            {
                header: "Số đơn hàng",
                key: "soDonHang",
                type: "number",
                width: 18,
            },
            {
                header:
                    "Giá trị đơn hàng",
                key: "doanhThu",
                type: "money",
                width: 24,
            },
        ],
        Array.isArray(
            thongKe.topNhanVien
        )
            ? thongKe.topNhanVien
            : []
    );

    /* ===========================
       SHEET 6: TOP ĐỐI TÁC
    =========================== */

    appendSheet(
        workbook,
        "Top đối tác",
        [
            {
                header: "Xếp hạng",
                value: (_, index) =>
                    index + 1,
                type: "number",
                width: 12,
            },
            {
                header: "Mã đối tác",
                value: (item) => {
                    if (!item.maDoiTuong) {
                        return "";
                    }

                    return `DT${String(
                        item.maDoiTuong
                    ).padStart(4, "0")}`;
                },
                width: 18,
            },
            {
                header: "Tên đối tác",
                key: "tenDoiTuong",
                width: 38,
            },
            {
                header: "Số đơn hàng",
                key: "soDonHang",
                type: "number",
                width: 18,
            },
            {
                header:
                    "Giá trị sản phẩm đã bán",
                key: "doanhThu",
                type: "money",
                width: 26,
            },
            {
                header:
                    "Đối tác thực nhận 80%",
                value: (item) => {
                    return (
                        toNumber(
                            item.doanhThu
                        ) *
                        80 /
                        100
                    );
                },
                type: "money",
                width: 24,
            },
            {
                header:
                    "An Yên nhận 20%",
                value: (item) => {
                    return (
                        toNumber(
                            item.doanhThu
                        ) *
                        20 /
                        100
                    );
                },
                type: "money",
                width: 24,
            },
        ],
        Array.isArray(
            thongKe.topDoiTac
        )
            ? thongKe.topDoiTac
            : []
    );

    const reportType = isPartner
        ? "doanh-thu-doi-tac"
        : isAdminRole
            ? "doanh-thu-admin"
            : "doanh-thu-nhan-vien";

    saveWorkbook(
        workbook,
        reportType
    );
};

/* =====================================================
   COMBO
===================================================== */

const getComboStatusText = (combo) => {
    if (combo?.tenTrangThai) {
        return combo.tenTrangThai;
    }

    if (combo?.trangThaiText) {
        return combo.trangThaiText;
    }

    const status = Number(combo?.trangThai);

    if (status === 1) {
        return "Đang hoạt động";
    }

    if (status === 2) {
        return "Ngừng kinh doanh";
    }

    if (status === 0) {
        return "Đang ẩn";
    }

    return "Không xác định";
};

const getComboFilterStatusText = (status) => {
    if (
        status === "all" ||
        status === null ||
        status === undefined
    ) {
        return "Tất cả trạng thái";
    }

    const numberStatus = Number(status);

    if (numberStatus === 1) {
        return "Đang hoạt động";
    }

    if (numberStatus === 2) {
        return "Ngừng kinh doanh";
    }

    if (numberStatus === 0) {
        return "Đang ẩn";
    }

    return "Không xác định";
};

export const exportComboExcel = ({
                                     combos = [],
                                     allCombos = [],
                                     keyword = "",
                                     statusFilter = "all",
                                     productsCount = 0,
                                 }) => {
    if (
        !Array.isArray(combos) ||
        combos.length === 0
    ) {
        throw new Error(
            "Không có dữ liệu combo để xuất"
        );
    }

    const workbook = XLSX.utils.book_new();

    const sourceCombos =
        Array.isArray(allCombos) &&
        allCombos.length > 0
            ? allCombos
            : combos;

    const activeCount = sourceCombos.filter(
        (item) => Number(item.trangThai) === 1
    ).length;

    const hiddenCount = sourceCombos.filter(
        (item) => Number(item.trangThai) === 0
    ).length;

    const stoppedCount = sourceCombos.filter(
        (item) => Number(item.trangThai) === 2
    ).length;

    const totalComboPrice = combos.reduce(
        (total, combo) => {
            return total + toNumber(combo.gia);
        },
        0
    );

    const totalProductPrice = combos.reduce(
        (total, combo) => {
            return (
                total +
                toNumber(combo.tongGiaSanPham)
            );
        },
        0
    );

    const totalQuantity = combos.reduce(
        (total, combo) => {
            const comboProducts =
                Array.isArray(combo.sanPhams)
                    ? combo.sanPhams
                    : [];

            return (
                total +
                comboProducts.reduce(
                    (sum, product) => {
                        return (
                            sum +
                            toNumber(
                                product.soLuongTrongCombo ??
                                product.soLuong ??
                                1
                            )
                        );
                    },
                    0
                )
            );
        },
        0
    );

    /* =========================
       SHEET 1: TỔNG QUAN
    ========================= */

    const summaryRows = [
        {
            label: "Tên báo cáo",
            value: "Danh sách combo",
            type: "text",
        },
        {
            label: "Từ khóa tìm kiếm",
            value:
                String(keyword || "").trim() ||
                "Không có",
            type: "text",
        },
        {
            label: "Trạng thái lọc",
            value:
                getComboFilterStatusText(
                    statusFilter
                ),
            type: "text",
        },
        {
            label: "Tổng combo trong hệ thống",
            value: sourceCombos.length,
            type: "number",
        },
        {
            label: "Số combo sau khi lọc",
            value: combos.length,
            type: "number",
        },
        {
            label: "Combo đang hoạt động",
            value: activeCount,
            type: "number",
        },
        {
            label: "Combo đang ẩn",
            value: hiddenCount,
            type: "number",
        },
        {
            label: "Combo ngừng kinh doanh",
            value: stoppedCount,
            type: "number",
        },
        {
            label: "Sản phẩm có thể chọn",
            value: productsCount,
            type: "number",
        },
        {
            label:
                "Tổng số lượng sản phẩm trong combo",
            value: totalQuantity,
            type: "number",
        },
        {
            label:
                "Tổng giá bán của các combo",
            value: totalComboPrice,
            type: "money",
        },
        {
            label:
                "Tổng giá sản phẩm gốc",
            value: totalProductPrice,
            type: "money",
        },
        {
            label:
                "Tổng chênh lệch giá",
            value:
                totalProductPrice -
                totalComboPrice,
            type: "money",
        },
        {
            label: "Thời điểm xuất",
            value: new Date().toLocaleString(
                "vi-VN"
            ),
            type: "text",
        },
    ];

    const summarySheet =
        XLSX.utils.aoa_to_sheet([
            ["NỘI DUNG", "GIÁ TRỊ"],

            ...summaryRows.map((item) => [
                item.label,
                item.value,
            ]),
        ]);

    summarySheet["!cols"] = [
        { wch: 42 },
        { wch: 45 },
    ];

    summarySheet["!autofilter"] = {
        ref: `A1:B${summaryRows.length + 1}`,
    };

    summaryRows.forEach(
        (item, index) => {
            const cell =
                summarySheet[`B${index + 2}`];

            if (!cell) {
                return;
            }

            if (item.type === "money") {
                cell.z = MONEY_FORMAT;
            }

            if (item.type === "number") {
                cell.z = "#,##0";
            }
        }
    );

    XLSX.utils.book_append_sheet(
        workbook,
        summarySheet,
        "Tổng quan"
    );

    /* =========================
       SHEET 2: DANH SÁCH COMBO
    ========================= */

    appendSheet(
        workbook,
        "Danh sách combo",
        [
            {
                header: "STT",
                value: (_, index) => index + 1,
                type: "number",
                width: 8,
            },
            {
                header: "Mã combo",
                value: (combo) => {
                    const id =
                        combo.comboId ??
                        combo.id ??
                        "";

                    return id
                        ? `CB${String(id).padStart(
                            4,
                            "0"
                        )}`
                        : "";
                },
                width: 16,
            },
            {
                header: "Tên combo",
                key: "tenCombo",
                width: 35,
            },
            {
                header: "Mã đối tác",
                value: (combo) =>
                    combo.maDoiTac ?? "",
                width: 16,
            },
            {
                header: "Tên đối tác",
                value: (combo) =>
                    combo.tenDoiTac ?? "",
                width: 30,
            },
            {
                header: "Giá combo",
                key: "gia",
                type: "money",
                width: 22,
            },
            {
                header: "Tổng giá sản phẩm",
                key: "tongGiaSanPham",
                type: "money",
                width: 24,
            },
            {
                header: "Chênh lệch giá",
                value: (combo) => {
                    return (
                        toNumber(
                            combo.tongGiaSanPham
                        ) -
                        toNumber(combo.gia)
                    );
                },
                type: "money",
                width: 22,
            },
            {
                header: "Tiết kiệm (%)",
                value: (combo) => {
                    const originalPrice =
                        toNumber(
                            combo.tongGiaSanPham
                        );

                    if (originalPrice <= 0) {
                        return 0;
                    }

                    return Math.max(
                        0,
                        Math.round(
                            (
                                (
                                    originalPrice -
                                    toNumber(combo.gia)
                                ) /
                                originalPrice *
                                100
                            ) *
                            100
                        ) / 100
                    );
                },
                type: "number",
                width: 16,
            },
            {
                header: "Số loại sản phẩm",
                value: (combo) => {
                    return Array.isArray(
                        combo.sanPhams
                    )
                        ? combo.sanPhams.length
                        : 0;
                },
                type: "number",
                width: 18,
            },
            {
                header: "Tổng số lượng",
                value: (combo) => {
                    const comboProducts =
                        Array.isArray(
                            combo.sanPhams
                        )
                            ? combo.sanPhams
                            : [];

                    return comboProducts.reduce(
                        (total, product) => {
                            return (
                                total +
                                toNumber(
                                    product.soLuongTrongCombo ??
                                    product.soLuong ??
                                    1
                                )
                            );
                        },
                        0
                    );
                },
                type: "number",
                width: 18,
            },
            {
                header: "Trạng thái",
                value: getComboStatusText,
                width: 22,
            },
            {
                header: "Mô tả",
                key: "moTa",
                width: 55,
            },
            {
                header: "Ghi chú",
                value: (combo) =>
                    combo.ghiChu ?? "",
                width: 50,
            },
            {
                header: "Đường dẫn ảnh",
                key: "hinhAnh",
                width: 60,
            },
        ],
        combos
    );

    /* =========================
       SHEET 3: CHI TIẾT SẢN PHẨM
    ========================= */

    const productRows = [];

    combos.forEach((combo) => {
        const comboProducts =
            Array.isArray(combo.sanPhams)
                ? combo.sanPhams
                : [];

        comboProducts.forEach(
            (product, index) => {
                const quantity = toNumber(
                    product.soLuongTrongCombo ??
                    product.soLuong ??
                    1
                );

                const unitPrice = toNumber(
                    product.giaTien ??
                    product.gia ??
                    product.price
                );

                productRows.push({
                    comboId:
                        combo.comboId ??
                        combo.id,

                    tenCombo:
                        combo.tenCombo ?? "",

                    thuTu: index + 1,

                    maSanPham:
                        product.maSanPham ??
                        product.id,

                    tenSanPham:
                        product.tenSanPham ??
                        product.name ??
                        "",

                    giaTien: unitPrice,

                    soLuongTrongCombo:
                    quantity,

                    thanhTien:
                        product.thanhTien ??
                        unitPrice * quantity,

                    soLuongTon:
                        product.soLuongTon ??
                        product.soLuongKho ??
                        product.soLuong ??
                        "",

                    hinhAnh:
                        product.hinhAnh ?? "",
                });
            }
        );
    });

    appendSheet(
        workbook,
        "Chi tiết sản phẩm",
        [
            {
                header: "Mã combo",
                value: (item) =>
                    item.comboId
                        ? `CB${String(
                            item.comboId
                        ).padStart(4, "0")}`
                        : "",
                width: 16,
            },
            {
                header: "Tên combo",
                key: "tenCombo",
                width: 35,
            },
            {
                header:
                    "Thứ tự trong combo",
                key: "thuTu",
                type: "number",
                width: 18,
            },
            {
                header: "Mã sản phẩm",
                value: (item) =>
                    item.maSanPham
                        ? `SP${String(
                            item.maSanPham
                        ).padStart(4, "0")}`
                        : "",
                width: 18,
            },
            {
                header: "Tên sản phẩm",
                key: "tenSanPham",
                width: 38,
            },
            {
                header: "Đơn giá",
                key: "giaTien",
                type: "money",
                width: 22,
            },
            {
                header:
                    "Số lượng trong combo",
                key: "soLuongTrongCombo",
                type: "number",
                width: 22,
            },
            {
                header: "Thành tiền",
                key: "thanhTien",
                type: "money",
                width: 22,
            },
            {
                header: "Số lượng tồn",
                key: "soLuongTon",
                type: "number",
                width: 18,
            },
            {
                header:
                    "Đường dẫn ảnh sản phẩm",
                key: "hinhAnh",
                width: 60,
            },
        ],
        productRows
    );

    saveWorkbook(
        workbook,
        "danh-sach-combo"
    );
};

const getHopDongStatusText = (status) => {
    const numericStatus = Number(status);

    switch (numericStatus) {
        case 0:
            return "Chờ ký";

        case 1:
            return "Đang hiệu lực";

        case 2:
            return "Đã hủy";

        default:
            return "Không xác định";
    }
};

export const exportHopDongExcel = ({
                                       hopDongs = [],
                                   }) => {
    if (!Array.isArray(hopDongs) || hopDongs.length === 0) {
        throw new Error("Không có dữ liệu hợp đồng để xuất");
    }

    const workbook = XLSX.utils.book_new();

    appendSheet(
        workbook,
        "Danh sách hợp đồng",
        [
            {
                header: "STT",
                value: (_, index) => index + 1,
                type: "number",
                width: 8,
            },
            {
                header: "Mã hợp đồng",
                value: (item) =>
                    item.soHopDong ||
                    `HD${String(item.maHopDong).padStart(7, "0")}`,
                width: 18,
            },
            {
                header: "Khách hàng",
                key: "tenKhachHang",
                width: 30,
            },
            {
                header: "Số điện thoại",
                key: "soDienThoai",
                width: 18,
            },
            {
                header: "Giá trị hợp đồng",
                key: "giaTriHopDong",
                type: "money",
                width: 22,
            },
            {
                header: "Ngày ký",
                key: "ngayKyHD",
                type: "date",
                width: 18,
            },
            {
                header: "Ngày hết hạn",
                value: (item) =>
                    item.ngayHetHan ??
                    item.ngayKetThuc,
                type: "date",
                width: 18,
            },
            {
                header: "Trạng thái",
                value: (item) =>
                    getHopDongStatusText(item.trangThai),
                width: 20,
            },
        ],
        hopDongs
    );

    saveWorkbook(
        workbook,
        "danh-sach-hop-dong"
    );
};

export const exportSanPhamExcel = ({
                                       products = [],
                                       keyword = "",
                                       categoryFilter = "all",
                                       partnerFilter = "all",
                                   }) => {

    if (!products.length) {
        throw new Error("Không có dữ liệu để xuất");
    }

    const workbook = XLSX.utils.book_new();

    appendSheet(
        workbook,
        "Danh sách sản phẩm",
        [
            {
                header: "STT",
                value: (_, index) => index + 1,
                type: "number",
                width: 8,
            },
            {
                header: "Mã sản phẩm",
                value: (item) => `SP${String(item.id).padStart(4,"0")}`,
                width: 18,
            },
            {
                header: "Tên sản phẩm",
                key: "name",
                width: 35,
            },
            {
                header: "Đối tác",
                key: "tenDoiTac",
                width: 30,
            },
            {
                header: "Loại",
                key: "loai",
                width: 20,
            },
            {
                header: "Vật liệu",
                key: "vatLieu",
                width: 20,
            },
            {
                header: "Số lượng",
                key: "soLuong",
                type: "number",
                width: 15,
            },
            {
                header: "Giá bán",
                key: "price",
                type: "money",
                width: 18,
            },
            {
                header: "Trạng thái",
                key: "tenTrangThai",
                width: 20,
            },
            {
                header: "Ngày gửi",
                key: "ngayTao",
                type: "date",
                width: 18,
            },
        ],
        products
    );

    saveWorkbook(workbook, "danh-sach-san-pham");
};