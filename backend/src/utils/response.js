export function successResponse(data, message = '操作成功') { return { success: true, message, data } }
export function errorResponse(message, details = null) { return { success: false, message, details } }
