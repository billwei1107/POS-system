/**
 * @file CategoryListPage.tsx
 * @description 商品分類管理頁 / Product category management page
 * @description_en List, create, update, and delete product categories
 * @description_zh 商品分類的列表、新增、修改、刪除頁面
 */
import React, { useCallback, useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  TextField,
  Alert,
  Typography,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { productApi } from '../api/productApi';
import type { Category, CategoryRequest } from '../types';
import { ConfirmDialog, DataTable, PageHeader, StatusChip, type Column } from '@shared/components';
import { useAuthStore } from '@shared/store/authStore';

const getCategoryForm = (initial?: Category | null) => ({
  name: initial?.name ?? '',
  displayColor: initial?.displayColor ?? '',
  sortOrder: initial?.sortOrder ?? 0,
});

// ========================================
// 新增/編輯彈窗 / Create / Edit dialog
// ========================================
interface CategoryDialogProps {
  open: boolean;
  initial?: Category | null;
  onClose: () => void;
  onSave: (data: CategoryRequest) => void;
}

const CategoryDialog: React.FC<CategoryDialogProps> = ({ open, initial, onClose, onSave }) => {
  const [form, setForm] = useState(() => getCategoryForm(initial));

  const handleSubmit = () => {
    if (!form.name.trim()) return;
    onSave({
      name: form.name.trim(),
      displayColor: form.displayColor || null,
      sortOrder: form.sortOrder,
      active: true,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{initial ? '編輯分類' : '新增分類'}</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          label="分類名稱"
          fullWidth
          margin="normal"
          value={form.name}
          onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
          required
        />
        <TextField
          label="顯示顏色 (HEX)"
          fullWidth
          margin="normal"
          placeholder="#1A237E"
          value={form.displayColor}
          onChange={(e) => setForm((prev) => ({ ...prev, displayColor: e.target.value }))}
          inputProps={{ maxLength: 20 }}
        />
        <TextField
          label="排列順序"
          type="number"
          fullWidth
          margin="normal"
          value={form.sortOrder}
          onChange={(e) => setForm((prev) => ({ ...prev, sortOrder: Number(e.target.value) }))}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>取消</Button>
        <Button variant="contained" onClick={handleSubmit} disabled={!form.name.trim()}>
          儲存
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ========================================
// 主頁面 / Main page
// ========================================
const CategoryListPage: React.FC = () => {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Category | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Category | null>(null);

  const load = useCallback(async () => {
    if (!isAuthenticated) {
      setCategories([]);
      setError('請先登入系統後再管理商品分類。');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await productApi.getCategories();
      if (res.success) setCategories(res.data);
    } catch {
      setError('載入分類失敗，請重試');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => { load(); }, [load]);

  const handleSave = async (data: CategoryRequest) => {
    if (!isAuthenticated) return;

    try {
      if (editTarget) {
        await productApi.updateCategory(editTarget.id, data);
      } else {
        await productApi.createCategory(data);
      }
      setDialogOpen(false);
      setEditTarget(null);
      load();
    } catch {
      setError('儲存失敗，請重試');
    }
  };

  const handleDelete = async () => {
    if (!isAuthenticated || !deleteTarget) return;

    try {
      await productApi.deleteCategory(deleteTarget.id);
      setDeleteTarget(null);
      load();
    } catch {
      setError('刪除失敗，請重試');
    }
  };

  const renderCategoryActions = (category: Category) => (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.75 }}>
      <IconButton
        aria-label={`編輯 ${category.name}`}
        size="small"
        onClick={() => { setEditTarget(category); setDialogOpen(true); }}
        sx={{ width: 44, height: 44, borderRadius: 1.5, bgcolor: 'rgba(255,255,255,0.04)' }}
      >
        <EditIcon fontSize="small" />
      </IconButton>
      <IconButton
        aria-label={`刪除 ${category.name}`}
        size="small"
        color="error"
        onClick={() => setDeleteTarget(category)}
        sx={{ width: 44, height: 44, borderRadius: 1.5, bgcolor: 'rgba(255,82,82,0.1)' }}
      >
        <DeleteIcon fontSize="small" />
      </IconButton>
    </Box>
  );

  const renderColor = (category: Category) => category.displayColor ? (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
      <Box
        sx={{
          width: 16,
          height: 16,
          borderRadius: '50%',
          bgcolor: category.displayColor,
          border: '1px solid rgba(255,255,255,0.24)',
          flex: '0 0 auto',
        }}
      />
      <Box component="span" sx={{ overflowWrap: 'anywhere' }}>{category.displayColor}</Box>
    </Box>
  ) : '-';

  const columns: Column<Category>[] = [
    { key: 'name', label: '分類名稱' },
    {
      key: 'displayColor',
      label: '顏色',
      render: renderColor,
    },
    { key: 'sortOrder', label: '排序', width: 100 },
    {
      key: 'active',
      label: '狀態',
      width: 110,
      render: (category) => (
        <StatusChip status={category.active ? 'ACTIVE' : 'INACTIVE'} labelMap={{ ACTIVE: '啟用', INACTIVE: '停用' }} />
      ),
    },
    {
      key: 'actions',
      label: '操作',
      align: 'right',
      width: 120,
      render: renderCategoryActions,
    },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <PageHeader
        title="商品分類管理"
        subtitle="維護前台點單分類、排序、顯示色與啟用狀態。"
        actions={(
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            disabled={!isAuthenticated}
            onClick={() => { setEditTarget(null); setDialogOpen(true); }}
          >
            新增分類
          </Button>
        )}
      />

      {error && (
        <Alert
          severity="error"
          onClose={() => setError(null)}
          action={!isAuthenticated ? (
            <Button color="inherit" size="small" onClick={() => navigate('/login?redirect=/admin/pos/categories')}>
              前往登入
            </Button>
          ) : undefined}
        >
          {error}
        </Alert>
      )}

      {/* ===== 手機卡片列表 / Mobile card list ===== */}
      <Box sx={{ display: { xs: 'flex', md: 'none' }, flexDirection: 'column', gap: 1.25 }}>
        {loading ? (
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <CircularProgress size={20} />
              <Typography variant="body2" color="text.secondary">載入分類中...</Typography>
            </CardContent>
          </Card>
        ) : categories.length === 0 ? (
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary">尚無分類資料</Typography>
            </CardContent>
          </Card>
        ) : categories.map((category) => (
          <Card key={category.id} variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.25 }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 1 }}>
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="caption" color="text.secondary">分類名稱</Typography>
                  <Typography variant="h6" sx={{ fontSize: '1rem', lineHeight: 1.35, overflowWrap: 'anywhere' }}>
                    {category.name}
                  </Typography>
                </Box>
                <StatusChip
                  status={category.active ? 'ACTIVE' : 'INACTIVE'}
                  labelMap={{ ACTIVE: '啟用', INACTIVE: '停用' }}
                />
              </Box>

              <Box sx={{
                display: 'grid',
                gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
                gap: 1,
              }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">顏色</Typography>
                  <Typography component="div" variant="body2">{renderColor(category)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">排序</Typography>
                  <Typography variant="body2" fontWeight={800}>{category.sortOrder}</Typography>
                </Box>
              </Box>

              {renderCategoryActions(category)}
            </CardContent>
          </Card>
        ))}
      </Box>

      <Box sx={{ display: { xs: 'none', md: 'block' } }}>
        <DataTable
          columns={columns}
          rows={categories}
          loading={loading}
          emptyMessage="尚無分類資料"
          rowKey={(category) => category.id}
        />
      </Box>

      {dialogOpen && (
        <CategoryDialog
          open={dialogOpen}
          initial={editTarget}
          onClose={() => { setDialogOpen(false); setEditTarget(null); }}
          onSave={handleSave}
        />
      )}

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="刪除分類"
        message={`確定要刪除「${deleteTarget?.name ?? ''}」嗎？此操作會停用該分類。`}
        confirmLabel="確認刪除"
        severity="error"
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </Box>
  );
};

export default CategoryListPage;
