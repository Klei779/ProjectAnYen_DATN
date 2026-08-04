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
    const cartCount = computed(() => cartItems.value.reduce((sum, item) => sum + (item.quantity || 1), 0))

    function isInCart(productId) {
        return cartItems.value.some(
            item => String(item.id) === String(productId)
        )
    }

    function getItem(productId) {
        return cartItems.value.find(
            item => String(item.id) === String(productId)
        )
    }

    function addToCart(product, quantity = 1) {
        if (
            !product ||
            product.id === null ||
            product.id === undefined
        ) {
            return;
        }

        const safeQuantity = Math.max(
            1,
            Number(quantity) || 1
        );

        const existingItem = getItem(product.id);

        if (existingItem) {
            existingItem.quantity =
                (Number(existingItem.quantity) || 1)
                + safeQuantity;
        } else {
            cartItems.value.push({
                id: product.id,

                name:
                    product.name ||
                    product.tenSanPham ||
                    "",

                subname:
                    product.subname ||
                    product.tenLoai ||
                    product.loai ||
                    "",

                // Nên lưu riêng loại sản phẩm
                loai:
                    product.loai ||
                    product.tenLoai ||
                    product.categoryName ||
                    product.subname ||
                    "",


                maDoiTac:
                    product.maDoiTac ??
                    product.partnerId ??
                    null,

                price:
                    product.price ??
                    product.giaTien ??
                    null,

                oldPrice:
                    product.oldPrice ??
                    null,

                image:
                    product.image ||
                    product.hinhAnh ||
                    "",

                material:
                    product.material ||
                    product.vatLieu ||
                    "",

                religion:
                    product.religion ||
                    product.tonGiao ||
                    "",

                quantity: safeQuantity,

                addedAt: new Date().toISOString()
            });
        }

        saveCart();
    }

    function removeFromCart(productId) {
        cartItems.value = cartItems.value.filter(
            item => String(item.id) !== String(productId)
        )

        saveCart()
    }

    function updateQuantity(productId, quantity) {
        const item = getItem(productId)

        if (item) {
            if (quantity <= 0) {
                removeFromCart(productId)
            } else {
                item.quantity = quantity
                saveCart()
            }
        }
    }

    function increaseQuantity(productId) {
        const item = getItem(productId)
        if (item) {
            updateQuantity(productId, (item.quantity || 1) + 1)
        }
    }

    function decreaseQuantity(productId) {
        const item = getItem(productId)
        if (item) {
            updateQuantity(productId, (item.quantity || 1) - 1)
        }
    }

    function toggleCart(product) {
        if (isInCart(product.id)) {
            removeFromCart(product.id)
            return false
        }

        addToCart(product)
        return true
    }

    function clearCart() {
        cartItems.value = []
        saveCart()
    }

    return {
        cartItems,
        cartCount,
        isInCart,
        getItem,
        addToCart,
        removeFromCart,
        updateQuantity,
        increaseQuantity,
        decreaseQuantity,
        toggleCart,
        clearCart
    }
}