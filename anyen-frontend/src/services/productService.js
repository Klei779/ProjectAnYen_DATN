import axios from 'axios'

const API_URL = 'http://localhost:8080/api/san-pham'

export async function getProducts(params = {}) {
  const response = await axios.get(API_URL, {
    params: {
      keyword: params.keyword || '',
      loai: params.loai || '',
      vatLieu: Array.isArray(params.vatLieu) ? params.vatLieu.join(',') : '',
      tonGiao: Array.isArray(params.tonGiao) ? params.tonGiao.join(',') : '',
      mauSac: params.mauSac || '',
      minPrice: params.priceRange?.[0] ?? 0,
      maxPrice: params.priceRange?.[1] ?? 999999999,
      sortBy: params.sortBy || 'newest',
      page: params.page || 1,
      pageSize: params.pageSize || 16
    }
  })

  return {
    items: response.data.items || [],
    total: response.data.total || 0
  }
}

export async function getCategories() {
  return [
    { id: 1, name: 'Quan tai', total: 0, icon: 'fa-solid fa-box' },
    { id: 2, name: 'Binh tro cot', total: 0, icon: 'fa-solid fa-vase' },
    { id: 3, name: 'Dich vu', total: 0, icon: 'fa-solid fa-briefcase' },
    { id: 4, name: 'Dịch vụ vận chuyển', total: 0, icon: 'fa-solid fa-car' },
    { id: 5, name: 'Vật phẩm tang lễ', total: 0, icon: 'fa-solid fa-hands-praying' }
  ]
}

export async function getMaterials() {
  return [
    { id: 1, name: 'Go thong', total: 0 },
    { id: 2, name: 'Gom su', total: 0 },
    { id: 3, name: 'Tong hop', total: 0 },
    { id: 4, name: 'Xe chuyên dụng', total: 0 },
    { id: 5, name: 'Gỗ MDF', total: 0 }
  ]
}

export async function getReligions() {
  return [
    { id: 1, name: 'Phat giao', total: 0 },
    { id: 2, name: 'Khong phan biet', total: 0 },
    { id: 3, name: 'Cong giao', total: 0 },
    { id: 4, name: 'Không phân biệt', total: 0 },
    { id: 5, name: 'Phật giáo', total: 0 }
  ]
}

export async function getColors() {
  return [
    { name: 'Nau', hex: '#8B4513' },
    { name: 'Trang', hex: '#ffffff' },
    { name: 'Trang den', hex: '#222222' },
    { name: 'Đen', hex: '#000000' },
    { name: 'Nâu vàng', hex: '#b8860b' }
  ]
}