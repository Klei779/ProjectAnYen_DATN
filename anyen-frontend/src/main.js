import { createApp } from "vue";
import App from "./App.vue";

import router from "./router";

import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

import "bootstrap/dist/css/bootstrap.min.css";
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

import "./assets/styles/website/global.css";
import "./assets/styles/website/design-system.css";

import 'leaflet/dist/leaflet.css'

createApp(App)
    .use(router)
    .use(ElementPlus)
    .mount("#app");

