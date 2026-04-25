import { apiClient } from './apiClient';
import i18n from '../i18n';

export const currencyService = {
    getAvailableSymbols: async () => {
        try {
            const response = await apiClient.get('/api/currencies/symbols');
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání měn:', error);
            throw new Error(i18n.t('api.errorSymbols'));
        }
    },

    getExtremes: async (base, symbols) => {
        try {
            const symbolsParam = Array.isArray(symbols) ? symbols.join(',') : symbols;

            const response = await apiClient.get('/api/currencies/extremes', {
                params: {
                    base: base,
                    symbols: symbolsParam
                }
            });
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání extrémů:', error);
            if (error.response && error.response.data && error.response.data.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error(i18n.t('api.errorDashboard'));
        }
    },

    getHistory: async (base, symbols, startDate, endDate) => {
        try {
            const symbolsParam = Array.isArray(symbols) ? symbols.join(',') : symbols;

            const response = await apiClient.get('/api/currencies/history', {
                params: {
                    base: base,
                    symbols: symbolsParam,
                    startDate: startDate,
                    endDate: endDate
                }
            });
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání historie:', error);
            if (error.response && error.response.data && error.response.data.message) {
                throw new Error(i18n.t(error.response.data.message, { defaultValue: error.response.data.message }));
            }
            throw new Error(i18n.t('api.errorDashboard'));
        }
    },

    getSettings: async () => {
        try {
            const response = await apiClient.get('/api/settings');
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání nastavení:', error);
            throw new Error(i18n.t('api.errorSettingsLoad'));
        }
    },

    saveSettings: async (settings) => {
        try {
            const response = await apiClient.post('/api/settings', settings);
            return response.data;
        } catch (error) {
            console.error('Chyba při ukládání nastavení:', error);
            throw new Error(i18n.t('api.errorSettingsSave'));
        }
    }
};