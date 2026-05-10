/**
 * @file ProductListPage.tsx
 * @description 商品列表管理頁 / Product list management page
 * @description_en List, search, create, update, and delete products with category filter
 * @description_zh 商品列表、搜尋、分類篩選、新增、修改、刪除頁面
 */
import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { productApi } from '../api/productApi';
import type { Category, ProductItem, ProductItemRequest, UnitType } from '../types';
import { ConfirmDialog, DataTable, PageHeader, StatusChip, type Column } from '@shared/components';
import { formatMoney } from '@shared/utils';
import { useAuthStore } from '@shared/store/authStore';

const UNIT_OPTIONS: UnitType[] = ['PCS', 'KG', 'LB', 'ML', 'L'];

const createProductForm = (initial?: ProductItem | null): ProductItemRequest => {
  if (!initial) return { sku: '', name: '', basePrice: 0, unit: 'PCS', active: true };

  return {
    sku: initial.sku,
    name: initial.name,
    description: initial.description ?? undefined,
    categoryId: initial.categoryId ?? undefined,
    basePrice: initial.basePrice,
    costPrice: initial.costPrice ?? undefined,
    unit: initial.unit,
    barcodePrimary: initial.barcodePrimary ?? undefined,
    trackInventory: initial.trackInventory,
    sellable: initial.sellable,
    active: initial.active,
  };
};

// ========================================
// 商品編輯彈窗 / Product edit dialog
// ========================================
interface ProductDialogProps {
  open: boolean;
  initial?: ProductItem | null;
  categories: Category[];
  onClose: () => void;
  onSave: (data: ProductItemRequest) => void;
}

const ProductDialog: React.FC<ProductDialogProps> = ({ open, initial, categories, onClose, onSave }) => {
  const [form, setForm] = useState<ProductItemRequest>(() => createProductForm(initial));

  const set = (key: keyof ProductItemRequest) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((prev) => ({ ...prev, [key]: e.target.value }));

  const handleSubmit = () => {
    if (!form.sku.trim() || !form.name.trim()) return;
    onSave({ ...form, basePrice: Number(form.basePrice) });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{initial ? '編輯商品' : '新增商品'}</DialogTitle>
      <DialogContent>
        <TextField label="SKU" fullWidth margin="normal" required value={form.sku} onChange={set('sku')} />
        <TextField label="商品名稱" fullWidth margin="normal" required value={form.name} onChange={set('name')} />
        <TextField label="商品描述" fullWidth margin="normal" multiline rows={2}
          value={form.description ?? ''} onChange={set('description')} />

        <FormControl fullWidth margin="normal">
          <InputLabel>所屬分類</InputLabel>
          <Select
            label="所屬分類"
            value={form.categoryId ?? ''}
            onChange={(e) => setForm((prev) => ({ ...prev, categoryId: e.target.value || undefined }))}
          >
            <MenuItem value="">（不指定）</MenuItem>
            {categories.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
          </Select>
        </FormControl>

        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField label="售價" type="number" fullWidth margin="normal" required
            value={form.basePrice} onChange={set('basePrice')}
            inputProps={{ min: 0, step: 0.01 }} />
          <TextField label="成本" type="number" fullWidth margin="normal"
            value={form.costPrice ?? ''} onChange={set('costPrice')}
            inputProps={{ min: 0, step: 0.01 }} />
        </Box>

        <Box sx={{ display: 'flex', gap: 2 }}>
          <FormControl fullWidth margin="normal">
            <InputLabel>單位</InputLabel>
            <Select
              label="單位"
              value={form.unit ?? 'PCS'}
              onChange={(e) => setForm((prev) => ({ ...prev, unit: e.target.value as UnitType }))}
            >
              {UNIT_OPTIONS.map((u) => <MenuItem key={u} value={u}>{u}</MenuItem>)}
            </Select>
          </FormControl>
          <TextField label="主條碼" fullWidth margin="normal"
            value={form.barcodePrimary ?? ''} onChange={set('barcodePrimary')} />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>取消</Button>
        <Button variant="contained" onClick={handleSubmit}
          disabled={!form.sku.trim() || !form.name.trim()}>
          儲存
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ========================================
// 主頁面 / Main page
// ========================================
const ProductListPage: React.FC = () => {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<ProductItem | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ProductItem | null>(null);

  const PAGE_SIZE = 20;

  const load = useCallback(async () => {
    if (!isAuthenticated) {
      setProducts([]);
      setTotal(0);
      setError('請先登入系統後再管理商品資料。');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await productApi.getProducts({
        page: page - 1,
        size: PAGE_SIZE,
        keyword: keyword || undefined,
        categoryId: categoryFilter || undefined,
      });
      if (res.success) {
        setProducts(res.data.content);
        setTotal(res.data.totalPages);
      }
    } catch {
      setError('載入商品失敗，請重試');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, page, keyword, categoryFilter]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    if (!isAuthenticated) {
      setCategories([]);
      return;
    }
    productApi.getCategories().then((res) => { if (res.success) setCategories(res.data); });
  }, [isAuthenticated]);

  const handleSearch = () => { setKeyword(searchInput); setPage(1); };

  const handleSave = async (data: ProductItemRequest) => {
    if (!isAuthenticated) return;

    try {
      if (editTarget) {
        await productApi.updateProduct(editTarget.id, data);
      } else {
        await productApi.createProduct(data);
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
      await productApi.deleteProduct(deleteTarget.id);
      setDeleteTarget(null);
      load();
    } catch {
      setError('刪除失敗，請重試');
    }
  };

  const getCategoryName = (id: string | null) =>
    categories.find((c) => c.id === id)?.name ?? '-';

  const columns: Column<ProductItem>[] = [
    {
      key: 'sku',
      label: 'SKU',
      render: (product) => (
        <Box component="span" sx={{ fontFamily: 'monospace', fontWeight: 800 }}>
          {product.sku}
        </Box>
      ),
    },
    { key: 'name', label: '商品名稱' },
    {
      key: 'categoryId',
      label: '分類',
      render: (product) => getCategoryName(product.categoryId),
    },
    {
      key: 'basePrice',
      label: '售價',
      align: 'right',
      render: (product) => formatMoney(product.basePrice),
    },
    { key: 'unit', label: '單位', width: 90 },
    {
      key: 'active',
      label: '狀態',
      width: 110,
      render: (product) => (
        <StatusChip status={product.active ? 'ACTIVE' : 'INACTIVE'} labelMap={{ ACTIVE: '啟用', INACTIVE: '停用' }} />
      ),
    },
    {
      key: 'actions',
      label: '操作',
      align: 'right',
      width: 120,
      render: (product) => (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
          <IconButton
            aria-label={`編輯 ${product.name}`}
            size="small"
            onClick={() => { setEditTarget(product); setDialogOpen(true); }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton
            aria-label={`刪除 ${product.name}`}
            size="small"
            color="error"
            onClick={() => setDeleteTarget(product)}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <PageHeader
        title="商品管理"
        subtitle="維護 POS 可銷售商品、SKU、售價、分類與啟用狀態。"
        actions={(
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            disabled={!isAuthenticated}
            onClick={() => { setEditTarget(null); setDialogOpen(true); }}
          >
            新增商品
          </Button>
        )}
      />

      {error && (
        <Alert
          severity="error"
          onClose={() => setError(null)}
          action={!isAuthenticated ? (
            <Button color="inherit" size="small" onClick={() => navigate('/login?redirect=/pos/products')}>
              前往登入
            </Button>
          ) : undefined}
        >
          {error}
        </Alert>
      )}

      {/* ===== 篩選列 / Filter bar ===== */}
      <Box sx={{
        display: 'flex',
        gap: 2,
        flexWrap: 'wrap',
        bgcolor: 'background.paper',
        p: 2,
        borderRadius: 3,
        border: '1px solid rgba(255,255,255,0.06)',
      }}>
        <TextField
          placeholder="搜尋商品名稱、SKU、條碼"
          size="small"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          sx={{ flex: '1 1 260px' }}
        />
        <Button variant="outlined" startIcon={<SearchIcon />} onClick={handleSearch}>搜尋</Button>

        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>篩選分類</InputLabel>
          <Select
            label="篩選分類"
            value={categoryFilter}
            onChange={(e) => { setCategoryFilter(e.target.value); setPage(1); }}
          >
            <MenuItem value="">全部分類</MenuItem>
            {categories.map((c) => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
          </Select>
        </FormControl>
      </Box>

      <DataTable
        columns={columns}
        rows={products}
        loading={loading}
        total={total * PAGE_SIZE}
        page={page - 1}
        pageSize={PAGE_SIZE}
        onPageChange={(nextPage) => setPage(nextPage + 1)}
        emptyMessage="尚無商品資料"
        rowKey={(product) => product.id}
      />

      {dialogOpen && (
        <ProductDialog
          open={dialogOpen}
          initial={editTarget}
          categories={categories}
          onClose={() => { setDialogOpen(false); setEditTarget(null); }}
          onSave={handleSave}
        />
      )}

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="刪除商品"
        message={`確定要刪除「${deleteTarget?.name ?? ''}」嗎？此操作會停用該商品。`}
        confirmLabel="確認刪除"
        severity="error"
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </Box>
  );
};

export default ProductListPage;
