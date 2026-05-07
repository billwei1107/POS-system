/**
 * @file CategoryListPage.tsx
 * @description 商品分類管理頁 / Product category management page
 * @description_en List, create, update, and delete product categories
 * @description_zh 商品分類的列表、新增、修改、刪除頁面
 */
import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { productApi } from '../api/productApi';
import type { Category, CategoryRequest } from '../types';

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
  const [name, setName] = useState('');
  const [displayColor, setDisplayColor] = useState('');
  const [sortOrder, setSortOrder] = useState(0);

  useEffect(() => {
    setName(initial?.name ?? '');
    setDisplayColor(initial?.displayColor ?? '');
    setSortOrder(initial?.sortOrder ?? 0);
  }, [initial, open]);

  const handleSubmit = () => {
    if (!name.trim()) return;
    onSave({ name: name.trim(), displayColor: displayColor || null, sortOrder, active: true });
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
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <TextField
          label="顯示顏色 (HEX)"
          fullWidth
          margin="normal"
          placeholder="#1A237E"
          value={displayColor}
          onChange={(e) => setDisplayColor(e.target.value)}
          inputProps={{ maxLength: 20 }}
        />
        <TextField
          label="排列順序"
          type="number"
          fullWidth
          margin="normal"
          value={sortOrder}
          onChange={(e) => setSortOrder(Number(e.target.value))}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>取消</Button>
        <Button variant="contained" onClick={handleSubmit} disabled={!name.trim()}>
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
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Category | null>(null);

  const load = async () => {
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
  };

  useEffect(() => { load(); }, []);

  const handleSave = async (data: CategoryRequest) => {
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

  const handleDelete = async (id: string) => {
    if (!window.confirm('確定要刪除此分類？')) return;
    try {
      await productApi.deleteCategory(id);
      load();
    } catch {
      setError('刪除失敗，請重試');
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* ===== 頁面標頭 / Page header ===== */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" fontWeight="bold">商品分類管理</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => { setEditTarget(null); setDialogOpen(true); }}
        >
          新增分類
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      {/* ===== 分類列表 / Category table ===== */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>分類名稱</TableCell>
                <TableCell>顏色</TableCell>
                <TableCell>排序</TableCell>
                <TableCell>狀態</TableCell>
                <TableCell align="right">操作</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {categories.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    尚無分類資料
                  </TableCell>
                </TableRow>
              ) : (
                categories.map((cat) => (
                  <TableRow key={cat.id} hover>
                    <TableCell>{cat.name}</TableCell>
                    <TableCell>
                      {cat.displayColor ? (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Box
                            sx={{ width: 16, height: 16, borderRadius: '50%',
                                  bgcolor: cat.displayColor, border: '1px solid #ccc' }}
                          />
                          {cat.displayColor}
                        </Box>
                      ) : '-'}
                    </TableCell>
                    <TableCell>{cat.sortOrder}</TableCell>
                    <TableCell>
                      <Chip
                        label={cat.active ? '啟用' : '停用'}
                        color={cat.active ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        size="small"
                        onClick={() => { setEditTarget(cat); setDialogOpen(true); }}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton size="small" color="error" onClick={() => handleDelete(cat.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* ===== 新增/編輯彈窗 ===== */}
      <CategoryDialog
        open={dialogOpen}
        initial={editTarget}
        onClose={() => { setDialogOpen(false); setEditTarget(null); }}
        onSave={handleSave}
      />
    </Box>
  );
};

export default CategoryListPage;
