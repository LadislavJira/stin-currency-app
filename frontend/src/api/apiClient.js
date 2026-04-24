import axios from 'axios';

const API_BASE_URL = import.meta.env.DEV ? 'http://localhost:8080' : '';

export const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

apiClient.interceptors.request.use((config) => {
    config.headers['Accept-Language'] = localStorage.getItem('appLang') || 'cs';
    const token = localStorage.getItem('authToken');
    if (token && !config.headers.Authorization) {
        config.headers.Authorization = `Basic ${token}`;
    }
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('authToken');
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);