import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { Box, AppBar, Toolbar, Typography, Button, Container, CssBaseline } from '@mui/material';

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

const AppLayout = ({ children }: { children: React.ReactNode }) => (
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
        <Button color="inherit" component={Link} to="/login">登出</Button>
      </Toolbar>
    </AppBar>
    <Container sx={{ mt: 4, flexGrow: 1 }}>
      {children}
    </Container>
  </Box>
);

function App() {
  return (
    <>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          {/* 受保護的後台區域 */}
          <Route path="/role" element={<AppLayout><RoleListPage /></AppLayout>} />
          <Route path="/company" element={<AppLayout><CompanyPage /></AppLayout>} />
          <Route path="/department" element={<AppLayout><DepartmentPage /></AppLayout>} />
          <Route path="/position" element={<AppLayout><PositionPage /></AppLayout>} />
          <Route path="/employee" element={<AppLayout><EmployeeListPage /></AppLayout>} />
          <Route path="/workflow" element={<AppLayout><DefinitionListPage /></AppLayout>} />
          <Route path="/my-tasks" element={<AppLayout><MyTasksPage /></AppLayout>} />

          {/* 預設導向登入頁面 */}
          <Route path="/" element={<Navigate to="/department" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </>
  );
}

export default App;
