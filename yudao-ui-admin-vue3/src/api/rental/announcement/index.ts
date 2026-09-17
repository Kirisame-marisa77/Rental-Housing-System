import request from '@/config/axios'

export interface AnnouncementVO {
  id?: number
  noticeNo?: string
  title?: string
  content?: string
  category?: string
  publisherId?: number
  publishTime?: Date
  status?: number
  isTop?: number
  targetScope?: string
  createTime?: Date
}

// 查询公告分页
export const getAnnouncementPage = (params: PageParam) => {
  return request.get({ url: '/rental/announcement/page', params })
}

// 查询公告详情
export const getAnnouncement = (id: number) => {
  return request.get({ url: '/rental/announcement/get?id=' + id })
}

// 新增公告
export const createAnnouncement = (data: AnnouncementVO) => {
  return request.post({ url: '/rental/announcement/create', data })
}

// 修改公告
export const updateAnnouncement = (data: AnnouncementVO) => {
  return request.put({ url: '/rental/announcement/update', data })
}

// 删除公告
export const deleteAnnouncement = (id: number) => {
  return request.delete({ url: '/rental/announcement/delete?id=' + id })
}

// 发布公告
export const publishAnnouncement = (id: number) => {
  return request.put({ url: '/rental/announcement/publish?id=' + id })
}

// 置顶/取消置顶
export const updateAnnouncementTop = (id: number, isTop: number) => {
  return request.put({ url: '/rental/announcement/update-top', params: { id, isTop } })
}
