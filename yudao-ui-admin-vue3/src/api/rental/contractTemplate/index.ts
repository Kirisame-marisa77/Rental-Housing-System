import request from '@/config/axios'

export interface ContractTemplateVO {
  id?: number
  templateName?: string
  // 0-标准租赁合同 1-短期租赁合同 2-商业租赁合同
  templateType?: number
  content?: string
  variables?: string
  version?: number
  // 0-启用 1-禁用
  status?: number
  remark?: string
  createTime?: Date
}

export interface TemplateVariableVO {
  key: string
  label: string
}

// 查询合同模板分页
export const getContractTemplatePage = (params: PageParam) => {
  return request.get({ url: '/rental/contract-template/page', params })
}

// 查询合同模板详情
export const getContractTemplate = (id: number) => {
  return request.get({ url: '/rental/contract-template/get?id=' + id })
}

// 新增合同模板
export const createContractTemplate = (data: ContractTemplateVO) => {
  return request.post({ url: '/rental/contract-template/create', data })
}

// 修改合同模板
export const updateContractTemplate = (data: ContractTemplateVO) => {
  return request.put({ url: '/rental/contract-template/update', data })
}

// 删除合同模板
export const deleteContractTemplate = (id: number) => {
  return request.delete({ url: '/rental/contract-template/delete?id=' + id })
}

// 查询启用中的模板精简列表（供「新建合同选模板」下拉使用，不含正文）
export const getEnabledTemplateList = () => {
  return request.get<ContractTemplateVO[]>({ url: '/rental/contract-template/list-all-simple' })
}

// 查询内置模板变量清单（供编辑正文时插入占位符）
export const getTemplateVariables = () => {
  return request.get<TemplateVariableVO[]>({ url: '/rental/contract-template/variables' })
}
