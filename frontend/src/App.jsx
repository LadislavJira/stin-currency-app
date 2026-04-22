import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

const ProtectedRoute = ({ children }) => {
const isAuthenticated = !!localStorage.getItem('authToken');
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    return children;
};

const PublicRoute = ({ children }) => {
    const isAuthenticated = !!localStorage.getItem('authToken');
    if (isAuthenticated) return <Navigate to="/dashboard" replace />;
    return children;
};
function App() {
    const getHomeRedirect = () => {
        return localStorage.getItem('authToken') ? "/dashboard" : "/login";
    };
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to={getHomeRedirect()} replace />} />

                <Route
                    path="/login"
                    element={
                        <PublicRoute>
                            <Login />
                        </PublicRoute>
                    }
                />

                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <Dashboard />
                        </ProtectedRoute>
                    }
                />

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;