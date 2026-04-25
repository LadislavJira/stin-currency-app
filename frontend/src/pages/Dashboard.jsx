import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { currencyService } from '../api/currencyService.js';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useTranslation } from 'react-i18next';
import './Dashboard.css';

const COLORS = ['#2563eb', '#dc2626', '#16a34a', '#d97706', '#9333ea', '#0891b2', '#be123c'];

export default function Dashboard() {
    const navigate = useNavigate();
    const today = new Date().toISOString().split('T')[0];



    const [baseCurrency, setBaseCurrency] = useState('EUR');
    const [selectedCurrencies, setSelectedCurrencies] = useState([]);
    const [startDate, setStartDate] = useState('2026-04-01');
    const [endDate, setEndDate] = useState(today);
    const [availableCurrencies, setAvailableCurrencies] = useState([]);

    const [error, setError] = useState(null);

    const [isFetching, setIsFetching] = useState(false);
    const [loadingExtremes, setLoadingExtremes] = useState(false);
    const [loadingHistory, setLoadingHistory] = useState(false);

    const [extremesData, setExtremesData] = useState(null);
    const [historyData, setHistoryData] = useState(null);

    const { t, i18n } = useTranslation();

    const calculateDateOffset = (dateString, daysOffset) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        date.setDate(date.getDate() + daysOffset);
        return date.toISOString().split('T')[0];
    };

    const maxEndDateByStart = calculateDateOffset(startDate, 365);
    const actualMaxEndDate = maxEndDateByStart < today ? maxEndDateByStart : today;
    const minStartDateByEnd = calculateDateOffset(endDate, -365);

    const chartData = historyData && historyData.timeseries
        ? Object.entries(historyData.timeseries).map(([date, rates]) => ({
            date,
            ...rates
        }))
        : [];

    const hasAverages = historyData && Object.keys(historyData.averages).length > 0;
    const hasChartData = chartData.length > 0;

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                const symbols = await currencyService.getAvailableSymbols();
                setAvailableCurrencies(symbols);

                const settings = await currencyService.getSettings();
                if (settings.baseCurrency) {
                    setBaseCurrency(settings.baseCurrency);
                }
                if (settings.selectedCurrencies && settings.selectedCurrencies.length > 0) {
                    setSelectedCurrencies(settings.selectedCurrencies);
                }
            } catch (err) {
                setError(err.message);
            }
        };
        fetchInitialData();
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('authToken');
        navigate('/login');
    };

    const changeLanguage = (lang) => {
        i18n.changeLanguage(lang);
        localStorage.setItem('appLang', lang);
    };

    const saveSettings = async () => {
        try {
            const payload = {
                baseCurrency: baseCurrency,
                selectedCurrencies: selectedCurrencies
            };
            const responseMessage = await currencyService.saveSettings(payload);
            alert(responseMessage);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleFetchData = async () => {
        if (selectedCurrencies.length === 0) {
            setError(t('dashboard.noCurrencyError'));
            return;
        }

        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffDays = Math.ceil(Math.abs(end - start) / (1000 * 60 * 60 * 24));

        if (diffDays > 365) {
            setError(t('error.date.tooLong'));
            return;
        }
        setIsFetching(true);
        setError(null);
        setExtremesData(null);
        setHistoryData(null);

        const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

        try {
            setLoadingExtremes(true);
            const extremes = await currencyService.getExtremes(baseCurrency, selectedCurrencies);
            setExtremesData(extremes);
            setLoadingExtremes(false);
            await delay(1200);

            setLoadingHistory(true);
            const history = await currencyService.getHistory(baseCurrency, selectedCurrencies, startDate, endDate);
            setHistoryData(history);
            setLoadingHistory(false);

        } catch (err) {
            setError(err.message);
            setLoadingExtremes(false);
            setLoadingHistory(false);
        } finally {
            setIsFetching(false);
        }
    };

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div className="logo">{t('dashboard.headerTitle')}</div>

                <div className="lang-switcher">
                    <button className={i18n.language === 'cs' ? 'active' : ''} onClick={() => changeLanguage('cs')}>CZ</button>
                    <button className={i18n.language === 'en' ? 'active' : ''} onClick={() => changeLanguage('en')}>EN</button>
                </div>

                <div className="user-info">
                    <span>{t('dashboard.user')} admin</span>
                    <button onClick={handleLogout} className="logout-button">{t('dashboard.logout')}</button>
                </div>
            </header>

            <div className="dashboard-layout">
                <aside className="controls-panel">
                    <h3>{t('dashboard.settingsTitle')}</h3>

                    <div className="control-group">
                        <label>{t('dashboard.baseCurrency')}</label>
                        <select value={baseCurrency} onChange={(e) => setBaseCurrency(e.target.value)}>
                            {availableCurrencies.map(c => <option key={c} value={c}>{c}</option>)}
                        </select>
                    </div>

                    <div className="control-group">
                        <label>{t('dashboard.targetCurrencies')}</label>
                        <div className="currency-selector">
                            {availableCurrencies.filter(c => c !== baseCurrency).map(c => (
                                <label key={c} className="checkbox-label">
                                    <input
                                        type="checkbox"
                                        checked={selectedCurrencies.includes(c)}
                                        onChange={(e) => {
                                            if(e.target.checked) setSelectedCurrencies([...selectedCurrencies, c]);
                                            else setSelectedCurrencies(selectedCurrencies.filter(curr => curr !== c));
                                        }}
                                    /> {c}
                                </label>
                            ))}
                        </div>
                    </div>

                    <div className="control-group">
                        <label>{t('dashboard.dateFrom')}</label>
                        <input
                            type="date"
                            value={startDate}
                            max={endDate}
                            min={minStartDateByEnd}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                        <label>{t('dashboard.dateTo')}</label>
                        <input
                            type="date"
                            value={endDate}
                            max={actualMaxEndDate}
                            min={startDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                    </div>

                    <button className="primary-button" onClick={handleFetchData} disabled={isFetching}>
                        {isFetching ? t('dashboard.loadingBtn') : t('dashboard.analyzeBtn')}
                    </button>

                    <button className="save-button" onClick={saveSettings}>{t('dashboard.saveBtn')}</button>
                </aside>

                <main className="results-area">
                    {error && (
                        <div className="error-banner">
                            <span><strong>{t('dashboard.errorText')}</strong> {error}</span>
                            <button onClick={() => setError(null)}>X</button>
                        </div>
                    )}

                    {(extremesData || historyData || isFetching) ? (
                        <>
                            <div className="stats-cards">
                                <div className="card">
                                    <h4>{t('results.strongest')}</h4>
                                    {loadingExtremes ? (
                                        <div className="loading-mini">{t('results.fetching')}</div>
                                    ) : extremesData?.weakestCurrency ? (
                                        <div className="stat-highlight">
                                            <span>{extremesData.weakestCurrency}</span>
                                            <strong>{extremesData.weakestValue.toFixed(4)}</strong>
                                        </div>
                                    ) : (
                                        <p className="no-data-text">{t('results.noData')}</p>
                                    )}
                                </div>
                                <div className="card">
                                    <h4>{t('results.weakest')}</h4>
                                    {loadingExtremes ? (
                                        <div className="loading-mini">{t('results.fetching')}</div>
                                    ) : extremesData?.strongestCurrency ? (
                                        <div className="stat-highlight">
                                            <span>{extremesData.strongestCurrency}</span>
                                            <strong>{extremesData.strongestValue.toFixed(4)}</strong>
                                        </div>
                                    ) : (
                                        <p className="no-data-text">{t('results.noData')}</p>
                                    )}
                                </div>

                                <div className="card">
                                    <h4>{t('results.average')}</h4>
                                    {loadingHistory ? (
                                        <div className="loading-mini">{t('results.fetching')}</div>
                                    ) : hasAverages ? (
                                        <ul className="averages-list">
                                            {Object.entries(historyData.averages).map(([currency, value]) => (
                                                <li key={currency}><span>{currency}:</span> <strong>{value.toFixed(4)}</strong></li>
                                            ))}
                                        </ul>
                                    ) : (
                                        <p className="no-data-text">{t('results.noDataAverages')}</p>
                                    )}
                                </div>
                            </div>

                            <div className="chart-placeholder">
                                <h3>{t('results.chartTitle')}</h3>
                                {loadingHistory ? (
                                    <div className="loading-overlay">{t('results.fetching')}</div>
                                ) : hasChartData ? (
                                    <ResponsiveContainer width="100%" height="85%">
                                        <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                                            <XAxis dataKey="date" stroke="#6b7280" />
                                            <YAxis stroke="#6b7280" domain={['auto', 'auto']} />
                                            <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }} />
                                            <Legend wrapperStyle={{ paddingTop: '10px' }} />

                                            {selectedCurrencies.map((currency, index) => (
                                                Object.keys(historyData.averages).includes(currency) && (
                                                    <Line
                                                        key={currency}
                                                        type="monotone"
                                                        dataKey={currency}
                                                        stroke={COLORS[index % COLORS.length]}
                                                        strokeWidth={3}
                                                        dot={{ r: 4, strokeWidth: 2 }}
                                                        activeDot={{ r: 6 }}
                                                    />
                                                )
                                            ))}
                                        </LineChart>
                                    </ResponsiveContainer>
                                ) : (
                                    <div className="chart-empty-message">
                                        {t('results.noDataChart')}
                                    </div>
                                )}
                            </div>
                        </>
                    ) : (
                        !error && !isFetching && <div className="empty-state-message">{t('results.selectParams')}</div>
                    )}
                </main>
            </div>
        </div>
    );
}