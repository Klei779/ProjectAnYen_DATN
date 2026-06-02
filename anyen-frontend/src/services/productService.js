/**
 * productService.js
 *
 * Service layer để lấy dữ liệu sản phẩm và bộ lọc.
 * Hiện tại dùng dữ liệu mock từ file JSON.
 * Sau này chỉ cần thay thế từng hàm bằng lời gọi API thực tế (axios/fetch)
 * mà KHÔNG cần sửa component.
 *
 * Ví dụ khi chuyển sang API thật:
 *   export async function getProducts(params) {
 *     const res = await axios.get('/api/products', { params })
 *     return res.data
 *   }
 */

import mockProducts  from '../mock/products.json'
import mockCategories from '../mock/categories.json'
import mockMaterials  from '../mock/materials.json'
import mockReligions  from '../mock/religions.json'
import mockColors     from '../mock/colors.json'

// Giả lập độ trễ mạng (ms) — xóa khi dùng API thật
const FAKE_DELAY = 300

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// ─── Products ────────────────────────────────────────────────

/**
 * Lấy danh sách sản phẩm với các tham số lọc & sắp xếp.
 * @param {Object} params
 * @param {number|null}   params.categoryId    - Lọc theo loại sản phẩm (null = tất cả)
 * @param {number[]}      params.materialIds   - Lọc theo chất liệu ([] = tất cả)
 * @param {number[]}      params.religionIds   - Lọc theo tôn giáo ([] = tất cả)
 * @param {string|null}   params.colorHex      - Lọc theo màu (null = tất cả)
 * @param {number[]}      params.priceRange    - [min, max] khoảng giá
 * @param {string}        params.keyword       - Từ khóa tìm kiếm
 * @param {string}        params.sortBy        - newest | oldest | price_asc | price_desc
 * @param {number}        params.page          - Trang hiện tại (bắt đầu từ 1)
 * @param {number}        params.pageSize      - Số sản phẩm mỗi trang
 * @returns {Promise<{ items: Product[], total: number }>}
 */
export async function getProducts(params = {}) {
  await delay(FAKE_DELAY)

  const {
    categoryId  = null,
    materialIds = [],
    religionIds = [],
    colorHex    = null,
    priceRange  = [0, 10_000_000],
    keyword     = '',
    sortBy      = 'newest',
    page        = 1,
    pageSize = 16
  } = params

  let result = [...mockProducts]

  // Lọc theo loại sản phẩm
  if (categoryId !== null) {
    result = result.filter((p) => p.categoryId === categoryId)
  }

  // Lọc theo chất liệu
  if (materialIds.length > 0) {
    result = result.filter((p) => materialIds.includes(p.materialId))
  }

  // Lọc theo tôn giáo
  if (religionIds.length > 0) {
    result = result.filter((p) => religionIds.includes(p.religionId))
  }

  // Lọc theo màu sắc
  if (colorHex) {
    result = result.filter((p) => p.colorHex === colorHex)
  }

  // Lọc theo khoảng giá
  result = result.filter(
    (p) => p.price >= priceRange[0] && p.price <= priceRange[1]
  )

  // Lọc theo từ khóa
  if (keyword.trim()) {
    const kw = keyword.trim().toLowerCase()
    result = result.filter(
      (p) =>
        p.name.toLowerCase().includes(kw) ||
        (p.subname && p.subname.toLowerCase().includes(kw))
    )
  }

  // Sắp xếp
  switch (sortBy) {
    case 'oldest':
      result.sort((a, b) => a.id - b.id)
      break
    case 'price_asc':
      result.sort((a, b) => a.price - b.price)
      break
    case 'price_desc':
      result.sort((a, b) => b.price - a.price)
      break
    case 'newest':
    default:
      result.sort((a, b) => b.id - a.id)
      break
  }

  const total = result.length

  // Phân trang
  const start = (page - 1) * pageSize
  const items = result.slice(start, start + pageSize)

  return { items, total }
}

// ─── Filter Options ───────────────────────────────────────────

/** Lấy danh sách loại sản phẩm */
export async function getCategories() {
  await delay(FAKE_DELAY)
  return [...mockCategories]
}

/** Lấy danh sách chất liệu */
export async function getMaterials() {
  await delay(FAKE_DELAY)
  return [...mockMaterials]
}

/** Lấy danh sách tôn giáo */
export async function getReligions() {
  await delay(FAKE_DELAY)
  return [...mockReligions]
}

/** Lấy bảng màu */
export async function getColors() {
  await delay(FAKE_DELAY)
  return [...mockColors]
}
