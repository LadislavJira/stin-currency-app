import axios from 'axios';

const API_BASE_URL = import.meta.env.DEV ? 'http://localhost:8080' : '';

export const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

apiClient.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('authToken');
    if (token) {
        config.headers.Authorization = `Basic ${token}`;
    }
    return config;
});