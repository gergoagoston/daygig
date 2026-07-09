import axios from 'axios'; // 1. Javítva: Import hozzáadva
import { notifyUnauthorized } from '../utils/authEvents';

const httpClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (isHttpUnauthorizedError(error)) {
      notifyUnauthorized();
    }

    return Promise.reject(error);
  }
);

export function isHttpUnauthorizedError(error: unknown): boolean {
  return axios.isAxiosError(error) && error.response?.status === 401;
}

export function isHttpConflictError(error: unknown): boolean {
  return axios.isAxiosError(error) && error.response?.status === 409;
}

export default httpClient;
