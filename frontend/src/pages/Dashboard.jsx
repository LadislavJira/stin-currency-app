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
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [dashboardData, setDashboardData] = useState(null);

    const { t, i18n } = useTranslation();

    const chartData = dashboardData && dashboardData.timeseries
        ? Object.entries(dashboardData.timeseries).map(([date, rates]) => ({
            date,
            ...rates
        }))
        : [];

    const hasAverages = dashboardData && Object.keys(dashboardData.averages).length > 0;
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
            // Zde by šlo přeložit i "Nastavení uloženo", ale protože to vrací backend jako responseMessage, necháme to tak.
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

        setLoading(true);
        setError(null);

        try {
            const data = await currencyService.getDashboardData(baseCurrency, selectedCurrencies, startDate, endDate);
            setDashboardData(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
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
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                        <label>{t('dashboard.dateTo')}</label>
                        <input
                            type="date"
                            value={endDate}
                            max={today}
                            min={startDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                    </div>

                    <button className="primary-button" onClick={handleFetchData} disabled={loading}>
                        {loading ? t('dashboard.loadingBtn') : t('dashboard.analyzeBtn')}
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

                    {loading && <div className="loading-overlay">{t('results.fetching')}</div>}

                    {dashboardData ? (
                        <>
                            <div className="stats-cards">
                                <div className="card">
                                    <h4>{t('results.strongest')}</h4>
                                    {dashboardData.extremes.weakestCurrency ? (
                                        <div className="stat-highlight">
                                            <span>{dashboardData.extremes.weakestCurrency}</span>
                                            <strong>{dashboardData.extremes.weakestValue.toFixed(4)}</strong>
                                        </div>
                                    ) : (
                                        <p className="no-data-text">{t('results.noData')}</p>
                                    )}
                                </div>
                                <div className="card">
                                    <h4>{t('results.weakest')}</h4>
                                    {dashboardData.extremes.strongestCurrency ? (
                                        <div className="stat-highlight">
                                            <span>{dashboardData.extremes.strongestCurrency}</span>
                                            <strong>{dashboardData.extremes.strongestValue.toFixed(4)}</strong>
                                        </div>
                                    ) : (
                                        <p className="no-data-text">{t('results.noData')}</p>
                                    )}
                                </div>

                                <div className="card">
                                    <h4>{t('results.average')}</h4>
                                    {hasAverages ? (
                                        <ul className="averages-list">
                                            {Object.entries(dashboardData.averages).map(([currency, value]) => (
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
                                {hasChartData ? (
                                    <ResponsiveContainer width="100%" height="85%">
                                        <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                                            <XAxis dataKey="date" stroke="#6b7280" />
                                            <YAxis stroke="#6b7280" domain={['auto', 'auto']} />
                                            <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }} />
                                            <Legend wrapperStyle={{ paddingTop: '10px' }} />

                                            {selectedCurrencies.map((currency, index) => (
                                                Object.keys(dashboardData.averages).includes(currency) && (
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
                        !error && !loading && <div className="empty-state-message">{t('results.selectParams')}</div>
                    )}
                </main>
            </div>
        </div>
    );
}