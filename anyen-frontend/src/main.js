import { createApp } from "vue";
import App from "./App.vue";

import router from "./router";

import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

import "bootstrap/dist/css/bootstrap.min.css";
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

import "./assets/styles/global.css";
import "./assets/styles/design-system.css";

createApp(App)
    .use(router)
    .use(ElementPlus)
    .mount("#app");