import api from "../api/api.js";

const API_URL = "/api/san-pham";
const RELIGION_OPTIONS = [
  "Không phân biệt",
  "Phật giáo",
  "Công giáo",
  "Tin Lành",
  "Cao Đài",
  "Hòa Hảo",
  "Hồi giáo",
  "Ấn Độ giáo",
];

export async function getProducts(params = {}) {
  const response = await api.get(API_URL, {
    params: {
      keyword: params.keyword || "",
      loai: params.loai || "",
      vatLieu: Array.isArray(params.vatLieu)
          ? params.vatLieu.join(",")
          : "",
      tonGiao: Array.isArray(params.tonGiao)
          ? params.tonGiao.join(",")
          : "",
      mauSac: params.mauSac || "",
      minPrice: params.priceRange?.[0] ?? 0,
      maxPrice: params.priceRange?.[1] ?? 999999999,
      sortBy: params.sortBy || "newest",
      page: params.page || 1,
      pageSize: params.pageSize || 16,
    },
  });

  return {
    items: response.data.items || [],
    total: response.data.total || 0,
  };
}

export async function getProductById(id) {
  const response = await api.get(`${API_URL}/${id}`);
  return response.data;
}

export async function getFilterOptions() {
  const response = await api.get(`${API_URL}/bo-loc`);
  const data = response.data || {};

  return {
    categories: mapCategoryOptions(data.categories || []),
    materials: mapBasicOptions(data.materials || []),

    // Tôn giáo hiện full danh sách, không phụ thuộc dữ liệu sản phẩm
    religions: RELIGION_OPTIONS.map((name) => ({
      id: name,
      name,
      total: 0,
    })),

    colors: mapColorOptions(data.colors || []),
  };
}

function mapBasicOptions(items) {
  return items.map((item) => ({
    id: item.id || item.name,
    name: item.name,
    total: Number(item.total || 0),
  }));
}

function mapCategoryOptions(items) {
  return items.map((item) => ({
    id: item.id || item.name,
    name: item.name,
    total: Number(item.total || 0),
    icon: getCategoryIcon(item.name),
  }));
}

function mapColorOptions(items) {
  return items.map((item) => ({
    id: item.id || item.name,
    name: item.name,
    total: Number(item.total || 0),
    hex: getColorHex(item.name),
  }));
}

function getCategoryIcon(name = "") {
  const value = name.toLowerCase();

  if (value.includes("quan tài")) return "fa-solid fa-box";
  if (value.includes("bình tro")) return "fa-solid fa-vase";
  if (value.includes("vận chuyển") || value.includes("xe")) return "fa-solid fa-car";
  if (value.includes("vật phẩm")) return "fa-solid fa-hands-praying";
  if (value.includes("dịch vụ")) return "fa-solid fa-briefcase";

  return "fa-solid fa-box-open";
}

function getColorHex(name = "") {
  const value = name.toLowerCase();

  if (value.includes("đen")) return "#000000";
  if (value.includes("trắng")) return "#ffffff";
  if (value.includes("nâu đỏ")) return "#7f1d1d";
  if (value.includes("nâu vàng")) return "#b8860b";
  if (value.includes("nâu")) return "#8b4513";
  if (value.includes("vàng")) return "#d4a017";

  return "#d9d9d9";
}