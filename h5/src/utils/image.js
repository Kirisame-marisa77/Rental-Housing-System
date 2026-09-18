/**
 * 图片 URL 归一化
 *
 * 后端 rental_house_image.image_url 存的是 infra 文件服务返回的地址，实际有三种形态：
 *
 *   1) http(s)://host/admin-api/infra/file/4/get/xxx.jpg  —— domain 配了值（默认就是这种）
 *   2) /admin-api/infra/file/4/get/xxx.jpg                —— domain 为空时的相对路径
 *   3) /demo/xxx.svg                                      —— 种子数据里的前端静态图
 *
 * 处理规则：
 *   - 已经是 http(s):// 或 data: 的原样返回。**绝不能**再拼前缀，否则会变成
 *     http://localhost:8081/http://127.0.0.1:48080/... 这种畸形地址。
 *   - / 开头的原样返回：走 Vite 代理（只代理了 /admin-api）或 public 目录的静态文件。
 *   - 其余（裸路径）按 infra 文件接口补全。
 *
 * 注：<img> 加载是跨源请求，不受 CORS 限制，所以形态 1) 里的
 * 127.0.0.1:48080 能正常显示，不需要走代理。
 */
/** 形如 http(s)://任意主机/admin-api/... 的地址，只保留 /admin-api 起的部分 */
const API_URL_PATTERN = /^https?:\/\/[^/]+(\/admin-api\/.*)$/i

export const resolveImageUrl = (url) => {
  if (!url) {
    return ''
  }
  const u = String(url).trim()
  if (!u) {
    return ''
  }
  // 历史数据里存的是绝对地址（http://127.0.0.1:48080/admin-api/...）。
  // 在局域网里用手机打开时，127.0.0.1 指的是手机自己，图片必然裂。
  // 所以凡是 infra 文件服务的地址，一律削成相对路径，交给当前站点的代理去请求 ——
  // 这样不管从哪台机器访问、后端换到哪个 IP，都能正常显示。
  const apiMatch = u.match(API_URL_PATTERN)
  if (apiMatch) {
    return apiMatch[1]
  }
  if (/^https?:\/\//i.test(u) || u.startsWith('data:')) {
    return u
  }
  if (u.startsWith('/')) {
    return u
  }
  return '/admin-api/infra/file/' + u.replace(/^\/+/, '')
}

export const resolveImageUrls = (list) => (list || []).map(resolveImageUrl).filter(Boolean)
