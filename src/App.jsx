import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import AdminDashboard from './components/Dashboard/AdminDashboard';
import AdvancedUserDashboard from './components/Dashboard/AdvancedUserDashboard';
import BasicUserDashboard from './components/Dashboard/BasicUserDashboard';
import GroupDashboard from './components/Dashboard/GroupDashboard'; // Import für GroupDashboard
import ProtectedRoute from './components/Shared/ProtectedRoute';
import Header from './components/Shared/Header';
import Footer from './components/Shared/Footer';
import LandingPage from './components/Dashboard/LandingPage';
import Chat from "./components/Dashboard/Chat";

function App() {
    return (
        <>
            <Header />
            <main>
                <Routes>
                    <Route path="/" element={<LandingPage />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    {/* Admin Dashboard */}
                    <Route
                        path="/admin"
                        element={
                            <ProtectedRoute role="ADMIN">
                                <AdminDashboard />
                            </ProtectedRoute>
                        }
                    />

                    {/* Advanced User Dashboard */}
                    <Route
                        path="/advanced-user"
                        element={
                            <ProtectedRoute role="ADVANCED_USER">
                                <AdvancedUserDashboard />
                            </ProtectedRoute>
                        }
                    />

                    {/* Basic User Dashboard */}
                    <Route
                        path="/basic-user"
                        element={
                            <ProtectedRoute role="BASIC_USER">
                                <BasicUserDashboard />
                            </ProtectedRoute>
                        }
                    />

                    {/* Group Dashboard: Nur für ADMIN und ADVANCED_USER */}
                    <Route
                        path="/group/:groupId"
                        element={
                            <ProtectedRoute role={["ADMIN", "ADVANCED_USER"]}>
                                <GroupDashboard />
                            </ProtectedRoute>
                        }
                    />

                    {/* Chat (keine spezifische Rolle notwendig) */}
                    <Route
                        path="/chat"
                        element={
                            <ProtectedRoute>
                                <Chat />
                            </ProtectedRoute>
                        }
                    />

                    {/* 404 Fehlerseite */}
                    <Route path="*" element={<div>404 - Seite nicht gefunden</div>} />
                </Routes>
            </main>
            <Footer />
        </>
    );
}

export default App;
