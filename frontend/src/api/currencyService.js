import { apiClient } from './apiClient';

export const currencyService = {
    getAvailableSymbols: async () => {
        try {
            const response = await apiClient.get('/api/currencies/symbols');
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání měn:', error);
            throw new Error('Nepodařilo se načíst seznam dostupných měn.');
        }
    },

    getDashboardData: async (base, symbols, startDate, endDate) => {
        try {
            const symbolsParam = Array.isArray(symbols) ? symbols.join(',') : symbols;

            const response = await apiClient.get('/api/currencies/dashboard', {
                params: {
                    base: base,
                    symbols: symbolsParam,
                    startDate: startDate,
                    endDate: endDate
                }
            });
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání dat pro dashboard:', error);

            if (error.response && error.response.data && error.response.data.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('Nepodařilo se stáhnout data z burzy. Zkuste to prosím později.');
        }
    },
    getSettings: async () => {
        try {
            const response = await apiClient.get('/api/settings');
            return response.data;
        } catch (error) {
            console.error('Chyba při načítání nastavení:', error);
            throw new Error('Nepodařilo se načíst uživatelské nastavení.');
        }
    },

    saveSettings: async (settings) => {
        try {
            const response = await apiClient.post('/api/settings', settings);
            return response.data;
        } catch (error) {
            console.error('Chyba při ukládání nastavení:', error);
            throw new Error('Nepodařilo se uložit nastavení na server.');
        }
    }
};