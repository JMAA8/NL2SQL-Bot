import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import AdminDashboard from './components/Dashboard/AdminDashboard';
import AdvancedUserDashboard from './components/Dashboard/AdvancedUserDashboard';
import BasicUserDashboard from './components/Dashboard/BasicUserDashboard';
import ProtectedRoute from './components/Shared/ProtectedRoute';
import Header from './components/Shared/Header';
import Footer from './components/Shared/Footer';

function App() {
    return (
        <>
            <Header />
            <main>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route
                        path="/admin"
                        element={
                            <ProtectedRoute role="ADMIN">
                                <AdminDashboard />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/advanced-user"
                        element={
                            <ProtectedRoute role="ADVANCED_USER">
                                <AdvancedUserDashboard />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/basic-user"
                        element={
                            <ProtectedRoute role="BASIC_USER">
                                <BasicUserDashboard />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </main>
            <Footer />
        </>
    );
}

export default App;
