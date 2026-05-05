/**
 * @file InventoryPage.tsx
 * @description POS 庫存管理介面 / POS Inventory Management Page
 * @description_en Handles stock levels and product catalog
 * @description_zh 處理各個終端的庫存水準與產品目錄
 */
import React, { useState } from 'react';
import { 
  Box, Typography, Button, TextField, InputAdornment, 
  IconButton, Table, TableBody, TableCell, TableContainer, 
  TableHead, TableRow, LinearProgress, Avatar, Pagination, 
  Card, CardContent, Select, MenuItem, FormControl
} from '@mui/material';
import { 
  Search, FileDownload, Add, FilterList, 
  Edit, DeleteOutline, TrendingUp, WarningAmber, 
  Autorenew
} from '@mui/icons-material';

// 模擬資料
const MOCK_INVENTORY = [
  { id: 1, name: 'Neo-Runner Pro X1', updated: '2h ago', sku: 'SKU-29402-A', category: 'Footwear', stock: 8, maxStock: 100, price: 189.00, img: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=100&q=80', status: 'low' },
  { id: 2, name: 'Precision Chrono S', updated: '5h ago', sku: 'SKU-88219-L', category: 'Accessories', stock: 142, maxStock: 200, price: 450.00, img: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=100&q=80', status: 'in_stock' },
  { id: 3, name: 'Aura Sonic Gen 2', updated: '12h ago', sku: 'SKU-10492-H', category: 'Electronics', stock: 0, maxStock: 50, price: 299.00, img: 'https://images.unsplash.com/photo-1543512214-318c7553f230?w=100&q=80', status: 'out_of_stock' },
  { id: 4, name: 'Vanguard Optic Frame', updated: '1d ago', sku: 'SKU-33201-F', category: 'Accessories', stock: 38, maxStock: 100, price: 125.00, img: 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=100&q=80', status: 'in_stock' }
];

const getStockColor = (status: string) => {
    switch(status) {
        case 'low': return '#FF9800'; // Orange
        case 'in_stock': return '#4CAF50'; // Green
        case 'out_of_stock': return '#F44336'; // Red
        default: return '#757575';
    }
};

const getStockText = (status: string, stock: number) => {
    switch(status) {
        case 'low': return `${stock} UNITS LEFT`;
        case 'in_stock': return `IN STOCK (${stock})`;
        case 'out_of_stock': return `OUT OF STOCK (0)`;
        default: return `UNKNOWN`;
    }
};

const InventoryPage: React.FC = () => {
    const [category, setCategory] = useState('All');
    const [status, setStatus] = useState('All');

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 4 }}>
            {/* Header */}
            <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'stretch', md: 'flex-start' }, gap: 2 }}>
                <Box>
                    <Typography variant="h4" fontWeight="bold" sx={{ mb: 1 }}>Inventory Management</Typography>
                    <Typography variant="body2" color="text.secondary">Manage stock levels and product catalog across all terminals.</Typography>
                </Box>
                <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, gap: 2 }}>
                    <Button variant="outlined" startIcon={<FileDownload />} sx={{ color: 'text.secondary', borderColor: 'rgba(255,255,255,0.2)', '&:hover': { borderColor: 'white', bgcolor: 'rgba(255,255,255,0.05)' } }}>
                        Export CSV
                    </Button>
                    <Button variant="contained" startIcon={<Add />} sx={{ background: 'linear-gradient(90deg, #7048E8 0%, #4D329A 100%)', color: 'white', fontWeight: 'bold' }}>
                        Add New Item
                    </Button>
                </Box>
            </Box>

            {/* Filter Bar */}
            <Box sx={{ 
                display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2, alignItems: { xs: 'stretch', md: 'center' }, bgcolor: 'background.paper', p: 2, borderRadius: 3 
            }}>
                <TextField 
                    placeholder="Search by name, SKU or category..."
                    variant="outlined"
                    size="small"
                    sx={{ flexGrow: 1, '& fieldset': { border: 'none' }, bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2 }}
                    InputProps={{
                        startAdornment: <InputAdornment position="start"><Search /></InputAdornment>,
                    }}
                />
                
                <FormControl size="small" sx={{ minWidth: 160 }}>
                    <Select value={category} onChange={(e) => setCategory(e.target.value)}
                        displayEmpty
                        sx={{ bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2, '& fieldset': { border: 'none' } }}
                        renderValue={(selected) => <Typography variant="body2">Category: <b>{selected}</b></Typography>}
                    >
                        <MenuItem value="All">All</MenuItem>
                        <MenuItem value="Electronics">Electronics</MenuItem>
                        <MenuItem value="Accessories">Accessories</MenuItem>
                        <MenuItem value="Footwear">Footwear</MenuItem>
                    </Select>
                </FormControl>

                <FormControl size="small" sx={{ minWidth: 140 }}>
                    <Select value={status} onChange={(e) => setStatus(e.target.value)}
                        displayEmpty
                        sx={{ bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2, '& fieldset': { border: 'none' } }}
                        renderValue={(selected) => <Typography variant="body2">Status: <b>{selected}</b></Typography>}
                    >
                        <MenuItem value="All">All</MenuItem>
                        <MenuItem value="In Stock">In Stock</MenuItem>
                        <MenuItem value="Low Stock">Low Stock</MenuItem>
                        <MenuItem value="Out of Stock">Out of Stock</MenuItem>
                    </Select>
                </FormControl>

                <IconButton sx={{ bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2, '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' } }}>
                    <FilterList />
                </IconButton>
            </Box>

            {/* Table */}
            <TableContainer sx={{ flexGrow: 1, overflow: 'auto' }}>
                <Table sx={{ minWidth: 800 }}>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.05)', fontWeight: 'bold' }}>ITEM NAME</TableCell>
                            <TableCell sx={{ color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.05)', fontWeight: 'bold' }}>SKU</TableCell>
                            <TableCell sx={{ color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.05)', fontWeight: 'bold' }}>CATEGORY</TableCell>
                            <TableCell sx={{ color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.05)', fontWeight: 'bold' }}>STOCK LEVEL</TableCell>
                            <TableCell sx={{ color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.05)', fontWeight: 'bold' }}>PRICE</TableCell>
                            <TableCell sx={{ color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.05)', fontWeight: 'bold', textAlign: 'right' }}>ACTIONS</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {MOCK_INVENTORY.map((row) => (
                            <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                        <Avatar src={row.img} variant="rounded" sx={{ width: 48, height: 48, bgcolor: 'background.paper', borderRadius: 2 }} />
                                        <Box>
                                            <Typography variant="body1" fontWeight="bold">{row.name}</Typography>
                                            <Typography variant="caption" color="text.secondary">Last updated {row.updated}</Typography>
                                        </Box>
                                    </Box>
                                </TableCell>
                                <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                    <Box sx={{ bgcolor: 'rgba(255,255,255,0.05)', px: 1, py: 0.5, borderRadius: 1, display: 'inline-block' }}>
                                        <Typography variant="caption" fontFamily="monospace" color="text.secondary">{row.sku}</Typography>
                                    </Box>
                                </TableCell>
                                <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                    <Typography variant="body2" color="text.secondary">{row.category}</Typography>
                                </TableCell>
                                <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', width: 180 }}>
                                    <LinearProgress 
                                        variant="determinate" 
                                        value={(row.stock / row.maxStock) * 100} 
                                        sx={{ 
                                            height: 6, 
                                            borderRadius: 3, 
                                            bgcolor: 'rgba(255,255,255,0.1)',
                                            '& .MuiLinearProgress-bar': { bgcolor: getStockColor(row.status) },
                                            mb: 1
                                        }} 
                                    />
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: getStockColor(row.status) }}>
                                        {row.status === 'low' && <WarningAmber sx={{ fontSize: 14 }} />}
                                        <Typography variant="caption" fontWeight="bold">
                                            {getStockText(row.status, row.stock)}
                                        </Typography>
                                    </Box>
                                </TableCell>
                                <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                    <Typography variant="body1" fontWeight="bold">${row.price.toFixed(2)}</Typography>
                                </TableCell>
                                <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', textAlign: 'right' }}>
                                    <IconButton size="small" sx={{ color: 'text.secondary' }}><Edit fontSize="small" /></IconButton>
                                    <IconButton size="small" sx={{ color: 'text.secondary' }}><DeleteOutline fontSize="small" /></IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Pagination */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="body2" color="text.secondary">
                    Showing <Typography component="span" color="text.primary" fontWeight="bold">1-4</Typography> of <Typography component="span" color="text.primary" fontWeight="bold">128</Typography> items
                </Typography>
                <Pagination count={32} page={1} shape="rounded" color="primary" sx={{
                    '& .MuiPaginationItem-root': { color: 'text.secondary' },
                    '& .Mui-selected': { color: 'white' }
                }}/>
            </Box>

            {/* Summary Cards */}
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' }, gap: 3, mt: 'auto', pt: 1 }}>
                <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, borderLeft: '4px solid #7048E8', boxShadow: 'none' }}>
                    <CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                            <Box sx={{ bgcolor: 'rgba(112, 72, 232, 0.1)', p: 1, borderRadius: 2, display: 'flex', color: '#7048E8' }}>
                                <TrendingUp />
                            </Box>
                            <Typography variant="subtitle2" fontWeight="bold">Total Asset Value</Typography>
                        </Box>
                        <Typography variant="h4" fontWeight="bold" sx={{ mb: 1 }}>$248,392.50</Typography>
                        <Typography variant="caption" color="success.main" fontWeight="bold">↑ +4.2% from last month</Typography>
                    </CardContent>
                </Card>

                <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, borderLeft: '4px solid #FF9800', boxShadow: 'none' }}>
                    <CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                            <Box sx={{ bgcolor: 'rgba(255, 152, 0, 0.1)', p: 1, borderRadius: 2, display: 'flex', color: '#FF9800' }}>
                                <WarningAmber />
                            </Box>
                            <Typography variant="subtitle2" fontWeight="bold">Low Stock Items</Typography>
                        </Box>
                        <Typography variant="h4" fontWeight="bold" sx={{ mb: 1 }}>12</Typography>
                        <Typography variant="caption" color="text.secondary">Requires replenishment within 48h</Typography>
                    </CardContent>
                </Card>

                <Card sx={{ bgcolor: 'background.paper', borderRadius: 3, borderLeft: '4px solid #4CAF50', boxShadow: 'none' }}>
                    <CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                            <Box sx={{ bgcolor: 'rgba(76, 175, 80, 0.1)', p: 1, borderRadius: 2, display: 'flex', color: '#4CAF50' }}>
                                <Autorenew />
                            </Box>
                            <Typography variant="subtitle2" fontWeight="bold">Turnover Rate</Typography>
                        </Box>
                        <Typography variant="h4" fontWeight="bold" sx={{ mb: 1 }}>8.4x</Typography>
                        <Typography variant="caption" color="text.secondary">Annual average for category</Typography>
                    </CardContent>
                </Card>
            </Box>
        </Box>
    );
}

export default InventoryPage;
