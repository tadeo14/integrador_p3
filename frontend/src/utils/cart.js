/** Costo de envío fijo (documentado en README). */
export const ENVIO = 0;
const KEY = 'foodstore_cart';
export function getCart() {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : [];
}
function saveCart(cart) {
    localStorage.setItem(KEY, JSON.stringify(cart));
}
export function addToCart(item) {
    const cart = getCart();
    const existing = cart.find(i => i.idProducto === item.idProducto);
    if (existing) {
        existing.cantidad += item.cantidad;
    }
    else {
        cart.push({ ...item });
    }
    saveCart(cart);
}
export function removeFromCart(idProducto) {
    saveCart(getCart().filter(i => i.idProducto !== idProducto));
}
export function updateQty(idProducto, cantidad) {
    const cart = getCart();
    const item = cart.find(i => i.idProducto === idProducto);
    if (item) {
        item.cantidad = cantidad;
        saveCart(cart);
    }
}
export function clearCart() {
    localStorage.removeItem(KEY);
}
export function cartCount() {
    return getCart().reduce((acc, i) => acc + i.cantidad, 0);
}
