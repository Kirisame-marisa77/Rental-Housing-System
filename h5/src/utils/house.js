/**
 * 房源展示文案
 *
 * 原来 8+ 个页面各自复制了一份 houseText，且存在两种不同写法。
 * 这里统一成一个函数，但**保留两种输出格式**（改格式会让页面上已看惯的文案变样）。
 */

/**
 * @param {object} row        列表行（需含 communityName / buildingNo / roomNo / houseId）
 * @param {object} [opts]
 * @param {'unit'|'slash'} [opts.style='unit']
 *   unit  → "云栖花园 4栋 1502室"（合同、退租、账单用）
 *   slash → "云栖花园 4/1502"   （申请、预约、收藏用）
 * @param {'id'|'idOrDash'|'dash'} [opts.fallback='idOrDash'] 拼不出地址时的兜底
 *   id       → "房源 12"
 *   idOrDash → 有 houseId 就 "房源 12"，否则 "-"
 *   dash     → "-"
 * @returns {string}
 */
export const houseText = (row, { style = 'unit', fallback = 'idOrDash' } = {}) => {
  const dash = () => (fallback === 'id' ? '' : '-')

  if (!row) {
    return dash()
  }

  const parts =
    style === 'slash'
      ? [row.communityName, row.buildingNo && `${row.buildingNo}/${row.roomNo}`]
      : [row.communityName, row.buildingNo && `${row.buildingNo}栋`, row.roomNo && `${row.roomNo}室`]

  const addr = parts.filter(Boolean).join(' ')
  if (addr) {
    return addr
  }

  if (fallback === 'id') {
    return `房源 ${row.houseId}`
  }
  if (fallback === 'idOrDash') {
    return row.houseId ? `房源 ${row.houseId}` : '-'
  }
  return '-'
}
