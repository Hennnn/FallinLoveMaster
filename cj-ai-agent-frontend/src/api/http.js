import axios from 'axios'

export const API_BASE_URL = 'http://localhost:8123/api'

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000
})

export function buildSseUrl(path, params = {}) {
  return http.getUri({
    url: path,
    params
  })
}
