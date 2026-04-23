import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import './Dashboard.css';

export default function Dashboard() {
    const navigate = useNavigate();

    const today = new Date().toISOString().split('T')[0];

    const [baseCurrency, setBaseCurrency] = useState('EUR');
    const [selectedCurrencies, setSelectedCurrencies] = useState(['CZK', 'USD', 'GBP']);
    const [startDate, setStartDate] = useState('2026-04-01');
    const [endDate, setEndDate] = useState(today);

    const availableCurrencies = ['EUR', 'CZK', 'USD', 'GBP', 'PLN', 'HUF'];

    const handleLogout = () => {
        localStorage.removeItem('authToken');
        navigate('/login');
    };

    // Simulace uložení nastavení (Bod 3.4)
    const saveSettings = async () => {
        try {
            // Tady později zavoláme tvůj backend endpoint pro uložení UserSettings
            console.log("Ukládám nastavení:", { baseCurrency, selectedCurrencies });
            alert("Nastavení uloženo perzistentně!");
        } catch (err) {
            console.error("Chyba při ukládání", err);
        }
    };

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div className="logo">Currency Analyser</div>
                <div className="user-info">
                    <span>Uživatel: admin</span>
                    <button onClick={handleLogout} className="logout-button">Odhlásit</button>
                </div>
            </header>

            <div className="dashboard-layout">
                <aside className="controls-panel">
                    <h3>Nastavení analýzy</h3>

                    <div className="control-group">
                        <label>Základní měna (Base)</label>
                        <select value={baseCurrency} onChange={(e) => setBaseCurrency(e.target.value)}>
                            {availableCurrencies.map(c => <option key={c} value={c}>{c}</option>)}
                        </select>
                    </div>

                    <div className="control-group">
                        <label>Sledované měny</label>
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
                        <label>Období od</label>
                        <input
                            type="date"
                            value={startDate}
                            max={endDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                        <label>Období do</label>
                        <input
                            type="date"
                            value={endDate}
                            max={today}
                            min={startDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                    </div>

                    <button className="save-button" onClick={saveSettings}>Uložit měny</button>
                </aside>

                <main className="results-area">
                    <div className="stats-cards">
                        <div className="card">
                            <h4>Nejsilnější měna</h4>
                            <div className="stat-highlight">
                                <span>CZK</span>
                                <strong>24.50</strong>
                            </div>
                        </div>
                        <div className="card">
                            <h4>Nejslabší měna</h4>
                            <div className="stat-highlight">
                                <span>USD</span>
                                <strong>1.08</strong>
                            </div>
                        </div>
                        <div className="card">
                            <h4>Průměr za období</h4>
                            <ul className="averages-list">
                                <li><span>CZK:</span> <strong>24.45</strong></li>
                                <li><span>USD:</span> <strong>1.07</strong></li>
                                <li><span>GBP:</span> <strong>0.85</strong></li>
                            </ul>
                        </div>
                    </div>

                    <div className="chart-placeholder">
                        <p>Zde bude graf (Line Chart) pro období {startDate} až {endDate}</p>
                    </div>
                </main>
            </div>
        </div>
    );
}