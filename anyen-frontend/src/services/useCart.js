import { computed, ref } from 'vue'

const CART_KEY = 'an_yen_cart'

function readCart() {
    try {
        const data = localStorage.getItem(CART_KEY)
        return data ? JSON.parse(data) : []
    } catch (error) {
        console.error('Lỗi đọc giỏ hàng:', error)
        return []
    }
}

const cartItems = ref(readCart())

function saveCart() {
    localStorage.setItem(CART_KEY, JSON.stringify(cartItems.value))

    // Báo cho các component khác như Header.vue
    window.dispatchEvent(new Event('cart-updated'))
}

export function useCart() {
    const cartCount = computed(() => cartItems.value.length)

    function isInCart(productId) {
        return cartItems.value.some(
            item => String(item.id) === String(productId)
        )
    }

    function addToCart(product) {
        if (!product || product.id === null || product.id === undefined) {
            return
        }

        if (isInCart(product.id)) {
            return
        }

        cartItems.value.push({
            id: product.id,
            name: product.name || '',
            subname: product.subname || '',
            price: product.price ?? null,
            oldPrice: product.oldPrice ?? null,
            image: product.image || '',
            material: product.material || product.vatLieu || '',
            religion: product.religion || product.tonGiao || '',
            addedAt: new Date().toISOString()
        })

        saveCart()
    }

    function removeFromCart(productId) {
        cartItems.value = cartItems.value.filter(
            item => String(item.id) !== String(productId)
        )

        saveCart()
    }

    function toggleCart(product) {
        if (isInCart(product.id)) {
            removeFromCart(product.id)
            return false
        }

        addToCart(product)
        return true
    }

    return {
        cartItems,
        cartCount,
        isInCart,
        addToCart,
        removeFromCart,
        toggleCart,
        clearCart
    }

    function clearCart() {
        cartItems.value = []
        saveCart()
    }
}