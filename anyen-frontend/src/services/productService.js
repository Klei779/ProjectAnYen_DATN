import axios from 'axios'

import mockCategories from '../mock/categories.json'
import mockMaterials from '../mock/materials.json'
import mockReligions from '../mock/religions.json'
import mockColors from '../mock/colors.json'

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

  return response.data
}

export async function getCategories() {
  return [...mockCategories]
}

export async function getMaterials() {
  return [...mockMaterials]
}

export async function getReligions() {
  return [...mockReligions]
}

export async function getColors() {
  return [...mockColors]
}