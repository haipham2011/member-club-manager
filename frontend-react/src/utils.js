const { VITE_BACKEND_HOST, VITE_BACKEND_PORT } = import.meta.env
export const API_URL = `http://${VITE_BACKEND_HOST}:${VITE_BACKEND_PORT}`
