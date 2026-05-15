import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { posTheme } from './shared/theme';
import { ProtectedRoute } from './shared/auth';

// 引入 auth 頁面
import { LoginPage } from './features/auth/pages/LoginPage';
import { RoleListPage } from './features/auth/pages/RoleListPage';
import { UserListPage } from './features/auth/pages/UserListPage';

// 引入 organization 頁面
import { CompanyPage } from './features/organization/pages/CompanyPage';
import { DepartmentPage } from './features/organization/pages/DepartmentPage';
import { PositionPage } from './features/organization/pages/PositionPage';
import { EmployeeListPage } from './features/organization/pages/EmployeeListPage';

// 引入 workflow 頁面
import { DefinitionListPage } from './features/workflow/pages/DefinitionListPage';
import { MyTasksPage } from './features/workflow/pages/MyTasksPage';

// 引入 leave 頁面
import LeaveTypePage from './features/leave/pages/LeaveTypePage';
import LeaveBalancePage from './features/leave/pages/LeaveBalancePage';
import LeaveRequestPage from './features/leave/pages/LeaveRequestPage';
import LeaveCalendarPage from './features/leave/pages/LeaveCalendarPage';

// 引入 POS 相關
import AdminLayout from './layouts/AdminLayout';
import AdminDashboardPage from './features/admin/pages/AdminDashboardPage';
import AuditLogPage from './features/admin/pages/AuditLogPage';
import PosLayout from './layouts/PosLayout';
import RegisterPage from './features/pos-orders/pages/RegisterPage';
import CheckoutPage from './features/pos-orders/pages/CheckoutPage';
import OrderListPage from './features/pos-orders/pages/OrderListPage';
import RefundPage from './features/pos-orders/pages/RefundPage';

import StockOverviewPage from './features/pos-inventory/pages/StockOverviewPage';
import ReceivingPage from './features/pos-inventory/pages/ReceivingPage';
import StockTakePage from './features/pos-inventory/pages/StockTakePage';
import TransferPage from './features/pos-inventory/pages/TransferPage';
import PosLoginPage from './features/pos-auth/pages/PosLoginPage';
import ProductListPage from './features/pos-products/pages/ProductListPage';
import CategoryListPage from './features/pos-products/pages/CategoryListPage';
import InvoicePage from './features/pos-tax/pages/InvoicePage';
import InvoiceTrackPage from './features/pos-tax/pages/InvoiceTrackPage';
import TaxClassSettingsPage from './features/pos-tax/pages/TaxClassSettingsPage';
import ShiftPage from './features/pos-staff/pages/ShiftPage';
import ZReportPage from './features/pos-staff/pages/ZReportPage';
import ReconciliationPage from './features/pos-payment/pages/ReconciliationPage';
import PayMethodSettingsPage from './features/pos-payment/pages/PayMethodSettingsPage';
import GatewayConfigPage from './features/pos-payment/pages/GatewayConfigPage';

function App() {
  return (
    <ThemeProvider theme={posTheme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* 受保護的後台區域 */}
          <Route path="/admin" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboardPage />} />
            <Route path="pos/products" element={<ProductListPage />} />
            <Route path="pos/categories" element={<CategoryListPage />} />
            <Route path="pos/orders" element={<OrderListPage />} />
            <Route path="pos/refunds" element={<RefundPage />} />
            <Route path="inventory/overview" element={<StockOverviewPage />} />
            <Route path="inventory/receiving" element={<ReceivingPage />} />
            <Route path="inventory/stock-takes" element={<StockTakePage />} />
            <Route path="inventory/stock-takes/:stockTakeId" element={<StockTakePage />} />
            <Route path="inventory/transfers" element={<TransferPage />} />
            <Route path="operations/invoices" element={<InvoicePage />} />
            <Route path="operations/invoice-tracks" element={<InvoiceTrackPage />} />
            <Route path="operations/tax-classes" element={<TaxClassSettingsPage />} />
            <Route path="operations/shifts" element={<ShiftPage />} />
            <Route path="operations/z-reports" element={<ZReportPage />} />
            <Route path="operations/reconciliation" element={<ReconciliationPage />} />
            <Route path="operations/pay-methods" element={<PayMethodSettingsPage />} />
            <Route path="operations/gateways" element={<GatewayConfigPage />} />
            <Route path="access/roles" element={<RoleListPage />} />
            <Route path="access/users" element={<UserListPage />} />
            <Route path="access/audit-logs" element={<AuditLogPage />} />
            <Route path="organization/company" element={<CompanyPage />} />
            <Route path="organization/departments" element={<DepartmentPage />} />
            <Route path="organization/positions" element={<PositionPage />} />
            <Route path="organization/employees" element={<EmployeeListPage />} />
            <Route path="workflow/definitions" element={<DefinitionListPage />} />
            <Route path="workflow/tasks" element={<MyTasksPage />} />
            <Route path="leave/types" element={<LeaveTypePage />} />
            <Route path="leave/balances" element={<LeaveBalancePage />} />
            <Route path="leave/request" element={<LeaveRequestPage />} />
            <Route path="leave/calendar" element={<LeaveCalendarPage />} />
            <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
          </Route>

          {/* 舊後台路由轉址 */}
          <Route path="/role" element={<Navigate to="/admin/access/roles" replace />} />
          <Route path="/users" element={<Navigate to="/admin/access/users" replace />} />
          <Route path="/audit-logs" element={<Navigate to="/admin/access/audit-logs" replace />} />
          <Route path="/company" element={<Navigate to="/admin/organization/company" replace />} />
          <Route path="/department" element={<Navigate to="/admin/organization/departments" replace />} />
          <Route path="/position" element={<Navigate to="/admin/organization/positions" replace />} />
          <Route path="/employee" element={<Navigate to="/admin/organization/employees" replace />} />
          <Route path="/workflow" element={<Navigate to="/admin/workflow/definitions" replace />} />
          <Route path="/my-tasks" element={<Navigate to="/admin/workflow/tasks" replace />} />
          <Route path="/leave/types" element={<Navigate to="/admin/leave/types" replace />} />
          <Route path="/leave/balances" element={<Navigate to="/admin/leave/balances" replace />} />
          <Route path="/leave/request" element={<Navigate to="/admin/leave/request" replace />} />
          <Route path="/leave/calendar" element={<Navigate to="/admin/leave/calendar" replace />} />
          <Route path="/products" element={<Navigate to="/admin/pos/products" replace />} />
          <Route path="/categories" element={<Navigate to="/admin/pos/categories" replace />} />
          <Route path="/inventory" element={<Navigate to="/admin/inventory/overview" replace />} />
          <Route path="/receiving" element={<Navigate to="/admin/inventory/receiving" replace />} />
          <Route path="/stock-takes" element={<Navigate to="/admin/inventory/stock-takes" replace />} />
          <Route path="/transfers" element={<Navigate to="/admin/inventory/transfers" replace />} />
          <Route path="/invoices" element={<Navigate to="/admin/operations/invoices" replace />} />
          <Route path="/invoice-tracks" element={<Navigate to="/admin/operations/invoice-tracks" replace />} />
          <Route path="/tax-classes" element={<Navigate to="/admin/operations/tax-classes" replace />} />
          <Route path="/shifts" element={<Navigate to="/admin/operations/shifts" replace />} />
          <Route path="/z-reports" element={<Navigate to="/admin/operations/z-reports" replace />} />
          <Route path="/reconciliation" element={<Navigate to="/admin/operations/reconciliation" replace />} />
          <Route path="/pay-methods" element={<Navigate to="/admin/operations/pay-methods" replace />} />
          <Route path="/gateways" element={<Navigate to="/admin/operations/gateways" replace />} />

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
              <Route path="inventory" element={<StockOverviewPage />} />
              <Route path="inventory/receiving" element={<ReceivingPage />} />
              <Route path="inventory/stock-takes" element={<StockTakePage />} />
              <Route path="inventory/stock-takes/:stockTakeId" element={<StockTakePage />} />
              <Route path="invoices" element={<InvoicePage />} />
              <Route path="shifts" element={<ShiftPage />} />
              <Route path="reconciliation" element={<ReconciliationPage />} />
              <Route path="*" element={<Navigate to="/pos/register" replace />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
