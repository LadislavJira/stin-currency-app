import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { useTranslation } from 'react-i18next';
import './Login.css';

export default function Login() {
    const { t, i18n } = useTranslation();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const changeLanguage = (lang) => {
        i18n.changeLanguage(lang);
        localStorage.setItem('appLang', lang);
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        const encodedToken = btoa(`${username}:${password}`);

        try {
            await apiClient.get('/api/settings', {
                headers: {
                    'Authorization': `Basic ${encodedToken}`
                }
            });

            localStorage.setItem('authToken', encodedToken);
            navigate('/dashboard');

        } catch (err) {
            if (!err.response) {
                setError(t('login.errorServer'));
            }
            else if (err.response.status === 401) {
                setError(t('login.errorAuth'));
            }
            else {
                setError(t('login.errorGeneric'));
            }
        }
    };

    return (
        <div className="login-container">
            <div className="login-lang-switcher">
                <button className={i18n.language === 'cs' ? 'active' : ''} onClick={() => changeLanguage('cs')}>CZ</button>
                <button className={i18n.language === 'en' ? 'active' : ''} onClick={() => changeLanguage('en')}>EN</button>
            </div>

            <div className="login-card">
                <h1 className="login-title">{t('login.title')}</h1>

                {error && <div className="login-error">{error}</div>}

                <form onSubmit={handleLogin} className="login-form">
                    <div className="input-group">
                        <label>{t('login.username')}</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="input-group">
                        <label>{t('login.password')}</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="login-button">
                        {t('login.submit')}
                    </button>
                </form>
            </div>
        </div>
    );
}