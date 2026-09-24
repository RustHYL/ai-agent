import axios from 'axios';

const isProduction = import.meta.env.PROD;

// 开发环境指向本地后端；生产环境使用同域相对路径
export const API_BASE_URL = isProduction
  ? '/api'
  : 'http://localhost:8123/api';

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

export function buildSseUrl(path, params = {}) {
  const url = API_BASE_URL.startsWith('http')
    ? new URL(`${API_BASE_URL}${path}`)
    : new URL(`${API_BASE_URL}${path}`, window.location.origin);

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value);
    }
  });

  return url.toString();
}
