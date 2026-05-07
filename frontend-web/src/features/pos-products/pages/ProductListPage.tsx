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
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Pagination,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { productApi } from '../api/productApi';
import type { Category, ProductItem, ProductItemRequest, UnitType } from '../types';

const UNIT_OPTIONS: UnitType[] = ['PCS', 'KG', 'LB', 'ML', 'L'];

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
  const [form, setForm] = useState<ProductItemRequest>({
    sku: '', name: '', basePrice: 0, unit: 'PCS', active: true,
  });

  useEffect(() => {
    if (initial) {
      setForm({
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
      });
    } else {
      setForm({ sku: '', name: '', basePrice: 0, unit: 'PCS', active: true });
    }
  }, [initial, open]);

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

  const PAGE_SIZE = 20;

  const load = useCallback(async () => {
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
  }, [page, keyword, categoryFilter]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    productApi.getCategories().then((res) => { if (res.success) setCategories(res.data); });
  }, []);

  const handleSearch = () => { setKeyword(searchInput); setPage(1); };

  const handleSave = async (data: ProductItemRequest) => {
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

  const handleDelete = async (id: string) => {
    if (!window.confirm('確定要刪除此商品？')) return;
    try {
      await productApi.deleteProduct(id);
      load();
    } catch {
      setError('刪除失敗，請重試');
    }
  };

  const getCategoryName = (id: string | null) =>
    categories.find((c) => c.id === id)?.name ?? '-';

  return (
    <Box sx={{ p: 3 }}>
      {/* ===== 頁面標頭 / Page header ===== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">商品管理</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => { setEditTarget(null); setDialogOpen(true); }}
        >
          新增商品
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      {/* ===== 篩選列 / Filter bar ===== */}
      <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
        <TextField
          placeholder="搜尋商品名稱、SKU、條碼"
          size="small"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          sx={{ minWidth: 260 }}
        />
        <Button variant="outlined" startIcon={<SearchIcon />} onClick={handleSearch}>搜尋</Button>

        <FormControl size="small" sx={{ minWidth: 160 }}>
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

      {/* ===== 商品列表 / Product table ===== */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>SKU</TableCell>
                  <TableCell>商品名稱</TableCell>
                  <TableCell>分類</TableCell>
                  <TableCell align="right">售價</TableCell>
                  <TableCell>單位</TableCell>
                  <TableCell>狀態</TableCell>
                  <TableCell align="right">操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {products.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                      尚無商品資料
                    </TableCell>
                  </TableRow>
                ) : (
                  products.map((product) => (
                    <TableRow key={product.id} hover>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{product.sku}</TableCell>
                      <TableCell>{product.name}</TableCell>
                      <TableCell>{getCategoryName(product.categoryId)}</TableCell>
                      <TableCell align="right">
                        {new Intl.NumberFormat('zh-TW', {
                          style: 'currency', currency: 'TWD', minimumFractionDigits: 0,
                        }).format(product.basePrice)}
                      </TableCell>
                      <TableCell>{product.unit}</TableCell>
                      <TableCell>
                        <Chip
                          label={product.active ? '啟用' : '停用'}
                          color={product.active ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          onClick={() => { setEditTarget(product); setDialogOpen(true); }}
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                        <IconButton size="small" color="error" onClick={() => handleDelete(product.id)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          {total > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
              <Pagination count={total} page={page} onChange={(_, v) => setPage(v)} color="primary" />
            </Box>
          )}
        </>
      )}

      {/* ===== 新增/編輯彈窗 ===== */}
      <ProductDialog
        open={dialogOpen}
        initial={editTarget}
        categories={categories}
        onClose={() => { setDialogOpen(false); setEditTarget(null); }}
        onSave={handleSave}
      />
    </Box>
  );
};

export default ProductListPage;
