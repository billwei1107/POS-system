/**
 * @file Cart.tsx
 * @description POS 購物車面板 / POS cart panel
 * @description_en Displays active order items, totals and checkout actions
 * @description_zh 顯示目前訂單品項、金額彙總與結帳操作
 */
import React, { useMemo } from 'react';
import { Box, Typography, Button, IconButton, Divider, Chip } from '@mui/material';
import { DeleteOutline, Add, Remove, PersonAdd, LocalOffer, PauseCircleOutline } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { calculateCartTotals, calculateItemCount, useCartStore } from '../store/cartStore';
import { formatMoney } from '@shared/utils';

const Cart: React.FC = () => {
    const navigate = useNavigate();
    const lines = useCartStore((state) => state.lines);
    const increase = useCartStore((state) => state.increase);
    const decrease = useCartStore((state) => state.decrease);
    const remove = useCartStore((state) => state.remove);
    const clear = useCartStore((state) => state.clear);
    const taxRate = useCartStore((state) => state.taxRate);
    const totals = useMemo(() => calculateCartTotals(lines, taxRate), [lines, taxRate]);
    const itemCount = useMemo(() => calculateItemCount(lines), [lines]);
    
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', p: 2.5, minHeight: 0 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2.5, gap: 2 }}>
                <Box>
                    <Typography variant="h6" fontWeight={900} sx={{ lineHeight: 1.1 }}>
                        目前訂單
                    </Typography>
                    <Typography variant="caption" color="text.secondary" fontFamily="monospace">
                        #POS-0428-001 · 內用
                    </Typography>
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <IconButton size="small" sx={{ color: 'text.secondary', bgcolor: 'rgba(255,255,255,0.04)', borderRadius: 1.5 }}>
                        <PauseCircleOutline fontSize="small" />
                    </IconButton>
                    <IconButton
                        aria-label="清空購物車"
                        size="small"
                        disabled={lines.length === 0}
                        onClick={clear}
                        sx={{ color: 'error.main', bgcolor: 'rgba(255,82,82,0.1)', borderRadius: 1.5 }}
                    >
                        <DeleteOutline fontSize="small" />
                    </IconButton>
                </Box>
            </Box>

            <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                <Chip label="A7 桌" size="small" sx={{ bgcolor: 'rgba(112,72,232,0.18)', color: '#CDBDFF', fontWeight: 800 }} />
                <Chip label="2 位" size="small" sx={{ bgcolor: 'rgba(255,255,255,0.06)', color: 'text.secondary', fontWeight: 700 }} />
            </Box>

            <Box sx={{ flexGrow: 1, overflowY: 'auto', mb: 2, pr: 0.5, minHeight: 0 }}>
                {lines.length === 0 && (
                    <Box sx={{
                        minHeight: 220,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        textAlign: 'center',
                        gap: 1,
                        color: 'text.secondary'
                    }}>
                        <Typography fontWeight={900} color="text.primary">尚未加入商品</Typography>
                        <Typography variant="body2">從左側商品清單點選商品即可加入目前訂單。</Typography>
                    </Box>
                )}

                {lines.map((item) => (
                    <Box
                        key={item.itemId}
                        sx={{
                            mb: 1.5,
                            p: 1.5,
                            borderRadius: 2,
                            bgcolor: 'rgba(255,255,255,0.035)',
                            border: '1px solid rgba(255,255,255,0.05)'
                        }}
                    >
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1.5, mb: 1.25 }}>
                            <Typography variant="body1" fontWeight={800} sx={{ lineHeight: 1.25 }}>
                                {item.name}
                            </Typography>
                            <Typography variant="body1" fontWeight={900} sx={{ fontVariantNumeric: 'tabular-nums' }}>
                                {formatMoney(item.unitPrice)}
                            </Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 1.5 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <IconButton
                                    aria-label={`減少 ${item.name}`}
                                    size="small"
                                    onClick={() => decrease(item.itemId)}
                                    sx={{ bgcolor: 'rgba(255,255,255,0.06)', borderRadius: 1.5, width: 32, height: 32 }}
                                >
                                    <Remove fontSize="small" />
                                </IconButton>
                                <Typography sx={{ minWidth: 20, textAlign: 'center', fontWeight: 'bold' }}>{item.quantity}</Typography>
                                <IconButton
                                    aria-label={`增加 ${item.name}`}
                                    size="small"
                                    onClick={() => increase(item.itemId)}
                                    sx={{ bgcolor: 'rgba(255,255,255,0.06)', borderRadius: 1.5, width: 32, height: 32 }}
                                >
                                    <Add fontSize="small" />
                                </IconButton>
                            </Box>
                            <IconButton
                                aria-label={`移除 ${item.name}`}
                                size="small"
                                onClick={() => remove(item.itemId)}
                                sx={{ color: 'error.main' }}
                            >
                                <DeleteOutline fontSize="small" />
                            </IconButton>
                        </Box>
                    </Box>
                ))}
            </Box>

            <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)', mb: 2 }} />

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'text.secondary' }}>
                    <Typography>小計</Typography>
                    <Typography>{formatMoney(totals.subtotal)}</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'text.secondary' }}>
                    <Typography>稅額 (5%)</Typography>
                    <Typography>{formatMoney(totals.tax)}</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'success.main' }}>
                    <Typography>折扣</Typography>
                    <Typography>-{formatMoney(totals.discount)}</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1, alignItems: 'center' }}>
                    <Typography variant="h5" fontWeight={900}>總計</Typography>
                    <Typography variant="h4" fontWeight={900} color="secondary.main" sx={{ fontVariantNumeric: 'tabular-nums' }}>
                        {formatMoney(totals.total)}
                    </Typography>
                </Box>
            </Box>

            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <Button variant="outlined" fullWidth startIcon={<LocalOffer />} sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.05)' }}>
                    折扣
                </Button>
                <Button variant="outlined" fullWidth startIcon={<PersonAdd />} sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.05)' }}>
                    會員
                </Button>
            </Box>

            <Button 
                variant="contained" 
                color="secondary" 
                fullWidth 
                onClick={() => navigate('/pos/checkout')}
                disabled={itemCount === 0}
                sx={{
                    py: 2,
                    mb: 2,
                    borderRadius: 2,
                    display: 'flex',
                    justifyContent: 'space-between',
                    px: 3,
                    boxShadow: '0 14px 26px rgba(255,109,0,0.25)'
                }}
            >
                <Typography variant="h6" fontWeight="bold">立即結帳</Typography>
                <Typography variant="h6" fontWeight="bold">{formatMoney(totals.total)}</Typography>
            </Button>

            <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="outlined" fullWidth sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)' }}>$10</Button>
                <Button variant="outlined" fullWidth sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)' }}>$20</Button>
                <Button variant="outlined" fullWidth sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)' }}>$50</Button>
            </Box>
        </Box>
    );
};

export default Cart;
