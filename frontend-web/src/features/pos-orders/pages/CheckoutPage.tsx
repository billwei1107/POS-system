/**
 * @file CheckoutPage.tsx
 * @description POS 結帳與付款選擇頁面 / POS Checkout and Payment Selection
 */
import React, { useMemo, useState } from 'react';
import { Box, Typography, Button, Avatar, Divider, Chip } from '@mui/material';
import { 
    Payments, CreditCard, AccountBalanceWallet, QrCode, 
    Nfc, MoreHoriz, ArrowBack 
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { calculateCartTotals, useCartStore } from '../store/cartStore';
import { formatMoney } from '@shared/utils';

const CheckoutPage: React.FC = () => {
    const navigate = useNavigate();
    const [selectedMethod, setSelectedMethod] = useState('cash');
    const orderItems = useCartStore((state) => state.lines);
    const taxRate = useCartStore((state) => state.taxRate);
    const totals = useMemo(() => calculateCartTotals(orderItems, taxRate), [orderItems, taxRate]);

    const paymentMethods = [
        { id: 'cash', label: '現金', icon: <Payments sx={{ fontSize: 32 }} />, color: '#4CAF50', bg: 'rgba(76, 175, 80, 0.15)' },
        { id: 'credit', label: '信用卡', icon: <CreditCard sx={{ fontSize: 32 }} />, color: '#5C67FF', bg: 'rgba(92, 103, 255, 0.15)' },
        { id: 'linepay', label: 'LINE Pay', icon: <AccountBalanceWallet sx={{ fontSize: 32 }} />, color: '#00C300', bg: 'rgba(0, 195, 0, 0.15)' },
        { id: 'jkopay', label: '街口支付', icon: <QrCode sx={{ fontSize: 32 }} />, color: '#E2263C', bg: 'rgba(226, 38, 60, 0.15)' },
        { id: 'easycard', label: '悠遊卡', icon: <Nfc sx={{ fontSize: 32 }} />, color: '#FF7D00', bg: 'rgba(255, 125, 0, 0.15)' },
        { id: 'others', label: '其他', icon: <MoreHoriz sx={{ fontSize: 32 }} />, color: '#9CA3AF', bg: 'rgba(156, 163, 175, 0.15)' },
    ];

    return (
        <Box sx={{ 
            height: '100%', 
            display: 'flex', 
            flexDirection: { xs: 'column', md: 'row' },
            bgcolor: 'background.paper',
            borderRadius: 3, 
            overflow: 'auto',
            border: '1px solid rgba(255,255,255,0.05)'
        }}>
            
            {/* 訂單摘要 / Order summary */}
            <Box sx={{ 
                width: { xs: '100%', md: '45%' },
                minHeight: { xs: 'auto', md: '100%' },
                p: { xs: 3, md: 5 }, 
                display: 'flex', 
                flexDirection: 'column',
                borderRight: { xs: 'none', md: '1px solid rgba(255,255,255,0.05)' },
                borderBottom: { xs: '1px solid rgba(255,255,255,0.05)', md: 'none' }
            }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 5 }}>
                    <Typography variant="h6" fontWeight="bold">訂單摘要</Typography>
                    <Chip label="#INV-8842" size="small" sx={{ bgcolor: 'rgba(255,255,255,0.06)', color: 'text.secondary', fontFamily: 'monospace', fontWeight: 800 }} />
                </Box>

                {/* 品項列表 / Items list */}
                <Box sx={{ flexGrow: 1, overflowY: 'auto' }}>
                    {orderItems.length === 0 && (
                        <Box sx={{ py: 8, textAlign: 'center', color: 'text.secondary' }}>
                            <Typography fontWeight={900} color="text.primary">購物車是空的</Typography>
                            <Typography variant="body2">返回收銀台加入商品後再選擇付款方式。</Typography>
                        </Box>
                    )}

                    {orderItems.map((item) => (
                        <Box key={item.itemId} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                <Avatar variant="rounded" src={item.imageUrl ?? undefined} sx={{ width: 56, height: 56, borderRadius: 2 }}>
                                    {item.sku.slice(0, 2)}
                                </Avatar>
                                <Box>
                                    <Typography variant="body1" fontWeight="bold" sx={{ mb: 0.5 }}>{item.name}</Typography>
                                    <Typography variant="body2" color="text.secondary">數量：{item.quantity}</Typography>
                                </Box>
                            </Box>
                            <Typography variant="body1" fontWeight="bold" color="text.secondary">
                                {formatMoney(item.unitPrice * item.quantity)}
                            </Typography>
                        </Box>
                    ))}
                </Box>

                {/* 金額彙總 / Totals */}
                <Box sx={{ mt: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: 'text.secondary' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">小計</Typography>
                        <Typography variant="body2">{formatMoney(totals.subtotal)}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: 'text.secondary' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">稅額 (5%)</Typography>
                        <Typography variant="body2">{formatMoney(totals.tax)}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: '#FF8A65' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">會員折扣</Typography>
                        <Typography variant="body2">-{formatMoney(totals.discount)}</Typography>
                    </Box>
                    
                    <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)', mb: 3 }} />
                    
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                        <Typography variant="subtitle1" fontWeight="bold" letterSpacing={1} sx={{ color: 'text.secondary', mb: 1 }}>應收金額</Typography>
                        <Typography variant="h2" fontWeight="bold" sx={{ color: '#E0E7FF' }}>{formatMoney(totals.total)}</Typography>
                    </Box>
                </Box>
            </Box>


            {/* 付款選項 / Payment options */}
            <Box sx={{ 
                width: { xs: '100%', md: '55%' },
                p: { xs: 3, md: 5 }, 
                display: 'flex', 
                flexDirection: 'column' 
            }}>
                <Box sx={{ mb: 5 }}>
                    <Typography variant="h6" fontWeight="bold" sx={{ mb: 0.5 }}>選擇付款方式</Typography>
                    <Typography variant="body2" color="text.secondary">請選擇顧客偏好的付款方式</Typography>
                </Box>

                {/* 付款方式格狀選單 / Payment option grid */}
                <Box sx={{ 
                    flexGrow: 1, 
                    display: 'grid', 
                    gridTemplateColumns: 'repeat(2, 1fr)', 
                    gap: 3 
                }}>
                    {paymentMethods.map((method) => (
                        <Button
                            key={method.id}
                            variant="outlined"
                            onClick={() => setSelectedMethod(method.id)}
                            sx={{
                                border: selectedMethod === method.id ? '1px solid rgba(255,109,0,0.75)' : '1px solid rgba(255,255,255,0.05)',
                                bgcolor: selectedMethod === method.id ? 'rgba(255,109,0,0.12)' : 'rgba(255,255,255,0.02)',
                                borderRadius: 3,
                                minHeight: { xs: 128, md: 'auto' },
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                gap: 2,
                                color: 'text.primary',
                                textTransform: 'none',
                                transition: 'all 0.2s',
                                '&:hover': {
                                    bgcolor: selectedMethod === method.id ? 'rgba(255,109,0,0.18)' : 'rgba(255,255,255,0.06)',
                                    borderColor: 'rgba(255,255,255,0.1)',
                                    transform: 'translateY(-2px)'
                                }
                            }}
                        >
                            <Box sx={{ 
                                bgcolor: method.bg, 
                                p: 1.5, 
                                borderRadius: 3, 
                                color: method.color,
                                display: 'flex'
                            }}>
                                {method.icon}
                            </Box>
                            <Typography variant="body1" fontWeight="bold">{method.label}</Typography>
                        </Button>
                    ))}
                </Box>

                {/* 底部操作 / Bottom actions */}
                <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
                    <Button 
                        variant="contained" 
                        startIcon={<ArrowBack />} 
                        onClick={() => navigate(-1)}
                        sx={{ 
                            flex: 1, 
                            py: 2, 
                            borderRadius: 2, 
                            bgcolor: 'rgba(255,255,255,0.08)', 
                            color: 'white',
                            boxShadow: 'none',
                            '&:hover': { bgcolor: 'rgba(255,255,255,0.12)' }
                        }}
                    >
                        返回購物車
                    </Button>
                    <Button 
                        variant="contained" 
                        disabled={orderItems.length === 0}
                        sx={{ 
                            flex: 2, 
                            py: 2, 
                            borderRadius: 2, 
                            background: 'linear-gradient(90deg, #FF7A1A 0%, #FF4F00 100%)',
                            color: 'white',
                            fontWeight: 'bold',
                            fontSize: '16px'
                        }}
                    >
                        確認付款方式
                    </Button>
                </Box>
            </Box>

        </Box>
    );
};

export default CheckoutPage;
