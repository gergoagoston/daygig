import axios from 'axios';

const httpClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export function isHttpConflictError(error: unknown) {
  return axios.isAxiosError(error) && error.response?.status === 409;
}

export function isHttpUnauthorizedError(error: unknown) {
  return axios.isAxiosError(error) && error.response?.status === 401;
}

export default httpClient;
