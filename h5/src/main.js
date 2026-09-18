import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 必须放在 EP 的样式之后：本文件里的窄屏覆盖要能盖住 EP 自身样式
import './styles/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.mount('#app')
