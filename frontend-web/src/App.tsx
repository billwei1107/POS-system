import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { Box, AppBar, Toolbar, Typography, Button, Container, CssBaseline, ThemeProvider } from '@mui/material';
import { posTheme } from './shared/theme';
import { ProtectedRoute } from './shared/auth';
import { useAuthStore } from './shared/store/authStore';

// 引入 auth 頁面
import { LoginPage } from './features/auth/pages/LoginPage';
import { RoleListPage } from './features/auth/pages/RoleListPage';

// 引入 organization 頁面
import { CompanyPage } from './features/organization/pages/CompanyPage';
import { DepartmentPage } from './features/organization/pages/DepartmentPage';
import { PositionPage } from './features/organization/pages/PositionPage';
import { EmployeeListPage } from './features/organization/pages/EmployeeListPage';

// 引入 workflow 頁面
import { DefinitionListPage } from './features/workflow/pages/DefinitionListPage';
import { MyTasksPage } from './features/workflow/pages/MyTasksPage';
import { NotificationBell } from './features/notification/components/NotificationBell';

// 引入 leave 頁面
import LeaveTypePage from './features/leave/pages/LeaveTypePage';
import LeaveBalancePage from './features/leave/pages/LeaveBalancePage';
import LeaveRequestPage from './features/leave/pages/LeaveRequestPage';
import LeaveCalendarPage from './features/leave/pages/LeaveCalendarPage';

// 引入 POS 相關
import PosLayout from './layouts/PosLayout';
import RegisterPage from './features/pos-orders/pages/RegisterPage';
import CheckoutPage from './features/pos-orders/pages/CheckoutPage';
import OrderListPage from './features/pos-orders/pages/OrderListPage';
import RefundPage from './features/pos-orders/pages/RefundPage';

import InventoryPage from './features/pos-inventory/pages/InventoryPage';
import PosLoginPage from './features/pos-auth/pages/PosLoginPage';
import ProductListPage from './features/pos-products/pages/ProductListPage';
import CategoryListPage from './features/pos-products/pages/CategoryListPage';
import InvoicePage from './features/pos-tax/pages/InvoicePage';

const AppLayout = ({ children }: { children: React.ReactNode }) => {
  const logout = useAuthStore((state) => state.logout);

  return (
    <Box sx={{ flexGrow: 1, minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            模塊化企業系統
          </Typography>
          <NotificationBell />
          <Button color="inherit" component={Link} to="/department">組織管理</Button>
          <Button color="inherit" component={Link} to="/employee">員工管理</Button>
          <Button color="inherit" component={Link} to="/workflow">發起簽核</Button>
          <Button color="inherit" component={Link} to="/my-tasks">我的待辦</Button>
          <Button color="inherit" component={Link} to="/leave/request">請假申請</Button>
          <Button color="inherit" component={Link} to="/leave/calendar">請假日曆</Button>
          <Button color="inherit" component={Link} to="/login" onClick={logout}>登出</Button>
        </Toolbar>
      </AppBar>
      <Container sx={{ mt: 4, flexGrow: 1 }}>
        {children}
      </Container>
    </Box>
  );
};

function App() {
  return (
    <ThemeProvider theme={posTheme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* 受保護的後台區域 */}
          <Route path="/role" element={<ProtectedRoute><AppLayout><RoleListPage /></AppLayout></ProtectedRoute>} />
          <Route path="/company" element={<ProtectedRoute><AppLayout><CompanyPage /></AppLayout></ProtectedRoute>} />
          <Route path="/department" element={<ProtectedRoute><AppLayout><DepartmentPage /></AppLayout></ProtectedRoute>} />
          <Route path="/position" element={<ProtectedRoute><AppLayout><PositionPage /></AppLayout></ProtectedRoute>} />
          <Route path="/employee" element={<ProtectedRoute><AppLayout><EmployeeListPage /></AppLayout></ProtectedRoute>} />
          <Route path="/workflow" element={<ProtectedRoute><AppLayout><DefinitionListPage /></AppLayout></ProtectedRoute>} />
          <Route path="/my-tasks" element={<ProtectedRoute><AppLayout><MyTasksPage /></AppLayout></ProtectedRoute>} />
          <Route path="/leave/types" element={<ProtectedRoute><AppLayout><LeaveTypePage /></AppLayout></ProtectedRoute>} />
          <Route path="/leave/balances" element={<ProtectedRoute><AppLayout><LeaveBalancePage /></AppLayout></ProtectedRoute>} />
          <Route path="/leave/request" element={<ProtectedRoute><AppLayout><LeaveRequestPage /></AppLayout></ProtectedRoute>} />
          <Route path="/leave/calendar" element={<ProtectedRoute><AppLayout><LeaveCalendarPage /></AppLayout></ProtectedRoute>} />

          {/* 預設導向登入頁面 */}
          <Route path="/" element={<Navigate to="/pos/register" replace />} />
          
          {/* POS 登入畫面 */}
          <Route path="/pos/login" element={<PosLoginPage />} />

          <Route path="/pos" element={<ProtectedRoute redirectTo="/pos/login"><PosLayout /></ProtectedRoute>}>
              <Route path="register" element={<RegisterPage />} />
              <Route path="orders" element={<OrderListPage />} />
              <Route path="refunds" element={<RefundPage />} />
              <Route path="products" element={<ProductListPage />} />
              <Route path="categories" element={<CategoryListPage />} />
              <Route path="checkout" element={<CheckoutPage />} />
              <Route path="inventory" element={<InventoryPage />} />
              <Route path="invoices" element={<InvoicePage />} />
              <Route path="*" element={<Navigate to="/pos/register" replace />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
