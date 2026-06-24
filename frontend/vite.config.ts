import { resolve } from 'path'
import { defineConfig } from 'vite'

export default defineConfig({
  build: {
    rollupOptions: {
      input: {
        main:            resolve(__dirname, 'index.html'),
        login:           resolve(__dirname, 'src/pages/auth/login/index.html'),
        register:        resolve(__dirname, 'src/pages/auth/register/index.html'),
        home:            resolve(__dirname, 'src/pages/store/home/index.html'),
        productDetail:   resolve(__dirname, 'src/pages/store/productDetail/index.html'),
        cart:            resolve(__dirname, 'src/pages/store/cart/index.html'),
        clientOrders:    resolve(__dirname, 'src/pages/client/orders/index.html'),
        adminHome:       resolve(__dirname, 'src/pages/admin/adminHome/index.html'),
        adminCategories: resolve(__dirname, 'src/pages/admin/categories/index.html'),
        adminProducts:   resolve(__dirname, 'src/pages/admin/products/index.html'),
        adminOrders:     resolve(__dirname, 'src/pages/admin/orders/index.html'),
      }
    }
  }
})
