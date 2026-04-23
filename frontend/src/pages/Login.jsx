import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import './Login.css'; // Přidáme odkaz na styly!

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

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
                setError('Server neodpovídá. Ujistěte se, že běží backend (port 8080).');
            }
            else if (err.response.status === 401) {
                setError('Nesprávné uživatelské jméno nebo heslo.');
            }
            else {
                setError('Něco se pokazilo. Zkuste to prosím později.');
            }
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h1 className="login-title">Přihlášení </h1>

                {error && <div className="login-error">{error}</div>}

                <form onSubmit={handleLogin} className="login-form">
                    <div className="input-group">
                        <label>Uživatelské jméno</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="input-group">
                        <label>Heslo</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="login-button">
                        Přihlásit se
                    </button>
                </form>
            </div>
        </div>
    );
}