import axios from 'axios';

const httpClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export function isHttpConflictError(error: unknown) {
  return axios.isAxiosError(error) && error.response?.status === 409;
}

export default httpClient;
