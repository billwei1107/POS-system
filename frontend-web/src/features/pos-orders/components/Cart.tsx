/**
 * @file Cart.tsx
 * @description POS 購物車面板 / POS cart panel
 * @description_en Displays active order items, totals and checkout actions
 * @description_zh 顯示目前訂單品項、金額彙總與結帳操作
 */
import React, { useEffect, useMemo, useState } from 'react';
import {
    Alert, Box, Typography, Button, CircularProgress, IconButton, Divider, Chip, Dialog, DialogActions,
    DialogContent, DialogTitle, TextField
} from '@mui/material';
import { DeleteOutline, Add, Remove, PersonAdd, LocalOffer, PauseCircleOutline } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { calculateCartTotals, calculateItemCount, useCartStore, type CartMember, type HeldOrder } from '../store/cartStore';
import { formatMoney, formatTime } from '@shared/utils';
import { heldOrderApi } from '../api/orderApi';
import type { HeldOrderResponse } from '../types';
import { memberApi } from '../../pos-crm/api/memberApi';
import type { Member } from '../../pos-crm/types';
import { promotionApi } from '../../pos-promotion/api/promotionApi';
import { getActivePosContext } from '../posSession';

const mapMemberToCartMember = (member: Member): CartMember => ({
    id: member.id,
    memberNo: member.memberNo,
    name: member.name,
    phoneMasked: member.phoneMasked,
    tier: member.tierLabel,
    points: member.pointsBalance,
    discountPercent: Number(member.discountPercent),
});

const Cart: React.FC = () => {
    const navigate = useNavigate();
    const lines = useCartStore((state) => state.lines);
    const increase = useCartStore((state) => state.increase);
    const decrease = useCartStore((state) => state.decrease);
    const remove = useCartStore((state) => state.remove);
    const clear = useCartStore((state) => state.clear);
    const taxRate = useCartStore((state) => state.taxRate);
    const discountAmount = useCartStore((state) => state.discountAmount);
    const discountSource = useCartStore((state) => state.discountSource);
    const selectedMember = useCartStore((state) => state.selectedMember);
    const appliedPromotion = useCartStore((state) => state.appliedPromotion);
    const heldOrders = useCartStore((state) => state.heldOrders);
    const setDiscountAmount = useCartStore((state) => state.setDiscountAmount);
    const clearDiscount = useCartStore((state) => state.clearDiscount);
    const setMember = useCartStore((state) => state.setMember);
    const clearMember = useCartStore((state) => state.clearMember);
    const setPromotionDiscount = useCartStore((state) => state.setPromotionDiscount);
    const clearPromotionDiscount = useCartStore((state) => state.clearPromotionDiscount);
    const holdCurrentOrder = useCartStore((state) => state.holdCurrentOrder);
    const replaceHeldOrders = useCartStore((state) => state.replaceHeldOrders);
    const replaceHeldOrder = useCartStore((state) => state.replaceHeldOrder);
    const restoreHeldOrder = useCartStore((state) => state.restoreHeldOrder);
    const removeHeldOrder = useCartStore((state) => state.removeHeldOrder);
    const [discountOpen, setDiscountOpen] = useState(false);
    const [memberOpen, setMemberOpen] = useState(false);
    const [holdOpen, setHoldOpen] = useState(false);
    const [discountInput, setDiscountInput] = useState('');
    const [memberQuery, setMemberQuery] = useState('');
    const [memberCandidates, setMemberCandidates] = useState<CartMember[]>([]);
    const [memberLoading, setMemberLoading] = useState(false);
    const [memberError, setMemberError] = useState('');
    const [promotionCodeInput, setPromotionCodeInput] = useState('');
    const [promotionLoading, setPromotionLoading] = useState(false);
    const [promotionError, setPromotionError] = useState('');
    const posContext = useMemo(() => getActivePosContext(), []);
    const totals = useMemo(() => calculateCartTotals(lines, taxRate, discountAmount), [discountAmount, lines, taxRate]);
    const itemCount = useMemo(() => calculateItemCount(lines), [lines]);

    const discountLabel = useMemo(() => {
        if (discountSource === 'member' && selectedMember) return `${selectedMember.discountPercent}% 會員折扣`;
        if (discountSource === 'promotion' && appliedPromotion) return appliedPromotion.name;
        if (discountSource === 'manual') return '手動折扣';
        return '折扣';
    }, [appliedPromotion, discountSource, selectedMember]);

    // ========================================
    // 折扣設定 / Discount Controls
    // ========================================
    const openDiscountDialog = () => {
        setDiscountInput(totals.discount > 0 ? String(totals.discount) : '');
        setDiscountOpen(true);
    };

    const applyDiscountAmount = (amount: number) => {
        setDiscountAmount(amount);
        setDiscountInput(String(Math.min(totals.subtotal, Math.max(0, amount))));
    };

    const applyDiscountPercent = (percent: number) => {
        applyDiscountAmount(Math.round(totals.subtotal * percent) / 100);
    };

    const confirmDiscount = () => {
        const value = Number(discountInput);
        setDiscountAmount(Number.isFinite(value) ? value : 0);
        setDiscountOpen(false);
    };

    const handleClearDiscount = () => {
        clearDiscount();
        setDiscountInput('');
        setPromotionCodeInput('');
        setPromotionError('');
        setDiscountOpen(false);
    };

    // ========================================
    // 自動促銷試算 / Automatic Promotion Evaluation
    // ========================================
    useEffect(() => {
        if (discountSource === 'manual' || discountSource === 'member') return;

        if (lines.length === 0 || totals.subtotal <= 0) {
            clearPromotionDiscount();
            setPromotionError('');
            return;
        }

        let cancelled = false;
        const activeCode = appliedPromotion?.code ?? null;
        setPromotionLoading(true);

        promotionApi.evaluate({
            storeId: posContext.storeId,
            subtotal: totals.subtotal,
            code: activeCode,
        })
            .then((response) => {
                if (cancelled) return;
                const result = response.data;
                if (result?.applied && result.ruleId && result.discountAmount > 0) {
                    setPromotionDiscount({
                        ruleId: result.ruleId,
                        name: result.name ?? '促銷折扣',
                        code: result.code,
                        discountAmount: Number(result.discountAmount),
                    });
                    setPromotionError('');
                } else {
                    clearPromotionDiscount();
                }
            })
            .catch(() => {
                if (!cancelled && discountSource === 'promotion') {
                    clearPromotionDiscount();
                    setPromotionError('促銷試算失敗，請稍後再試。');
                }
            })
            .finally(() => {
                if (!cancelled) setPromotionLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [
        appliedPromotion?.code,
        clearPromotionDiscount,
        discountSource,
        lines,
        posContext.storeId,
        setPromotionDiscount,
        totals.subtotal,
    ]);

    const handleApplyPromotionCode = async () => {
        const code = promotionCodeInput.trim();
        if (!code || totals.subtotal <= 0) return;

        setPromotionLoading(true);
        setPromotionError('');
        try {
            const response = await promotionApi.evaluate({
                storeId: posContext.storeId,
                subtotal: totals.subtotal,
                code,
            });
            const result = response.data;
            if (result?.applied && result.ruleId && result.discountAmount > 0) {
                setPromotionDiscount({
                    ruleId: result.ruleId,
                    name: result.name ?? '促銷折扣',
                    code: result.code,
                    discountAmount: Number(result.discountAmount),
                });
                setPromotionCodeInput(result.code ?? code.toUpperCase());
                setDiscountOpen(false);
            } else {
                setPromotionError('沒有可套用的優惠碼。');
            }
        } catch {
            setPromotionError('優惠碼試算失敗，請稍後再試。');
        } finally {
            setPromotionLoading(false);
        }
    };

    // ========================================
    // 會員綁定 / Member Binding
    // ========================================
    useEffect(() => {
        if (!memberOpen) return;
        const normalizedQuery = memberQuery.trim();
        if (!normalizedQuery) return;

        let cancelled = false;

        const timeoutId = window.setTimeout(() => {
            memberApi.search(normalizedQuery, 20)
                .then((response) => {
                    if (cancelled) return;
                    setMemberCandidates((response.data ?? []).map(mapMemberToCartMember));
                })
                .catch(() => {
                    if (cancelled) return;
                    setMemberCandidates([]);
                    setMemberError('會員查詢失敗，請稍後再試。');
                })
                .finally(() => {
                    if (!cancelled) setMemberLoading(false);
                });
        }, 250);

        return () => {
            cancelled = true;
            window.clearTimeout(timeoutId);
        };
    }, [memberOpen, memberQuery]);

    const openMemberDialog = () => {
        setMemberQuery('');
        setMemberCandidates([]);
        setMemberError('');
        setMemberOpen(true);
    };

    const handleMemberQueryChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const nextQuery = event.target.value;
        setMemberQuery(nextQuery);
        if (!nextQuery.trim()) {
            setMemberCandidates([]);
            setMemberError('');
            setMemberLoading(false);
        } else {
            setMemberError('');
            setMemberLoading(true);
        }
    };

    const handleSelectMember = (member: CartMember) => {
        setMember(member);
        setMemberOpen(false);
    };

    const handleClearMember = () => {
        clearMember();
        setMemberOpen(false);
    };

    const mapHeldOrderResponse = (response: HeldOrderResponse): HeldOrder | null => {
        try {
            const payload = JSON.parse(response.payload) as HeldOrder;
            return {
                ...payload,
                id: response.id,
                displayNo: response.label || payload.displayNo,
                createdAt: response.heldAt || payload.createdAt,
            };
        } catch {
            return null;
        }
    };

    useEffect(() => {
        if (!holdOpen) return;
        let cancelled = false;

        heldOrderApi.list(posContext.storeId, posContext.terminalId)
            .then((response) => {
                if (cancelled || !response.success || !response.data) return;
                const mapped = response.data
                    .map(mapHeldOrderResponse)
                    .filter((order): order is HeldOrder => order !== null);
                replaceHeldOrders(mapped);
            })
            .catch(() => {
                // localStorage 掛單保留作為 API 暫時不可用時的備援。
            });

        return () => {
            cancelled = true;
        };
    }, [holdOpen, posContext.storeId, posContext.terminalId, replaceHeldOrders]);

    // ========================================
    // 掛單與取單 / Hold And Restore Orders
    // ========================================
    const handleHoldCurrentOrder = async () => {
        const heldOrder = holdCurrentOrder();
        if (!heldOrder) return;

        try {
            const response = await heldOrderApi.create({
                storeId: posContext.storeId,
                terminalId: posContext.terminalId,
                label: heldOrder.displayNo,
                payload: JSON.stringify(heldOrder),
            });
            if (response.success && response.data) {
                const mapped = mapHeldOrderResponse(response.data);
                if (mapped) replaceHeldOrder(heldOrder.id, mapped);
            }
        } catch {
            // API 失敗時保留本地掛單，避免收銀現場丟失目前訂單。
        }
        setHoldOpen(false);
    };

    const handleRestoreHeldOrder = async (heldOrderId: string) => {
        restoreHeldOrder(heldOrderId);
        if (!heldOrderId.startsWith('hold-')) {
            try {
                await heldOrderApi.remove(heldOrderId);
            } catch {
                // 取回後若刪除 API 暫時失敗，本地狀態仍以已取回為準。
            }
        }
        setHoldOpen(false);
    };

    const handleRemoveHeldOrder = async (heldOrderId: string) => {
        removeHeldOrder(heldOrderId);
        if (!heldOrderId.startsWith('hold-')) {
            try {
                await heldOrderApi.remove(heldOrderId);
            } catch {
                // 本地先移除，後端失敗時下次開啟 Dialog 會重新同步可見資料。
            }
        }
    };

    const formatHeldTime = (createdAt: string) => formatTime(createdAt);
    
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
                    <IconButton
                        aria-label="掛單與取單"
                        onClick={() => setHoldOpen(true)}
                        sx={{ color: heldOrders.length > 0 ? 'secondary.main' : 'text.secondary', bgcolor: 'rgba(255,255,255,0.04)', borderRadius: 1.5, width: 44, height: 44 }}
                    >
                        <PauseCircleOutline fontSize="small" />
                    </IconButton>
                    <IconButton
                        aria-label="清空購物車"
                        disabled={lines.length === 0}
                        onClick={clear}
                        sx={{ color: 'error.main', bgcolor: 'rgba(255,82,82,0.1)', borderRadius: 1.5, width: 44, height: 44 }}
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
                                    onClick={() => decrease(item.itemId)}
                                    sx={{ bgcolor: 'rgba(255,255,255,0.06)', borderRadius: 1.5, width: 44, height: 44 }}
                                >
                                    <Remove fontSize="small" />
                                </IconButton>
                                <Typography sx={{ minWidth: 28, textAlign: 'center', fontWeight: 'bold', fontSize: '1.05rem' }}>{item.quantity}</Typography>
                                <IconButton
                                    aria-label={`增加 ${item.name}`}
                                    onClick={() => increase(item.itemId)}
                                    sx={{ bgcolor: 'rgba(255,255,255,0.06)', borderRadius: 1.5, width: 44, height: 44 }}
                                >
                                    <Add fontSize="small" />
                                </IconButton>
                            </Box>
                            <IconButton
                                aria-label={`移除 ${item.name}`}
                                onClick={() => remove(item.itemId)}
                                sx={{ color: 'error.main', width: 44, height: 44 }}
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
                    <Typography>{discountLabel}</Typography>
                    <Typography>-{formatMoney(totals.discount)}</Typography>
                </Box>
                {promotionLoading && discountSource !== 'manual' && discountSource !== 'member' && (
                    <Typography variant="caption" color="text.secondary">促銷試算中...</Typography>
                )}
                {promotionError && (
                    <Typography variant="caption" color="warning.main">{promotionError}</Typography>
                )}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1, alignItems: 'center' }}>
                    <Typography variant="h5" fontWeight={900}>總計</Typography>
                    <Typography variant="h4" fontWeight={900} color="secondary.main" sx={{ fontVariantNumeric: 'tabular-nums' }}>
                        {formatMoney(totals.total)}
                    </Typography>
                </Box>
            </Box>

            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <Button
                    variant="outlined"
                    fullWidth
                    startIcon={<LocalOffer />}
                    disabled={itemCount === 0}
                    onClick={openDiscountDialog}
                    sx={{
                        color: 'text.primary',
                        borderColor: totals.discount > 0 ? 'rgba(0,230,118,0.42)' : 'rgba(255,255,255,0.1)',
                        bgcolor: totals.discount > 0 ? 'rgba(0,230,118,0.08)' : 'rgba(255,255,255,0.05)'
                    }}
                >
                    {totals.discount > 0 ? `${discountLabel} ${formatMoney(totals.discount)}` : '折扣'}
                </Button>
                <Button
                    variant="outlined"
                    fullWidth
                    startIcon={<PersonAdd />}
                    onClick={openMemberDialog}
                    sx={{
                        color: 'text.primary',
                        borderColor: selectedMember ? 'rgba(255,138,101,0.45)' : 'rgba(255,255,255,0.1)',
                        bgcolor: selectedMember ? 'rgba(255,138,101,0.08)' : 'rgba(255,255,255,0.05)'
                    }}
                >
                    {selectedMember ? `${selectedMember.tier} ${selectedMember.name}` : '會員'}
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
                <Button variant="outlined" fullWidth sx={{ minHeight: 56, color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)' }}>$10</Button>
                <Button variant="outlined" fullWidth sx={{ minHeight: 56, color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)' }}>$20</Button>
                <Button variant="outlined" fullWidth sx={{ minHeight: 56, color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)' }}>$50</Button>
            </Box>

            <Dialog
                open={discountOpen}
                onClose={() => setDiscountOpen(false)}
                fullWidth
                maxWidth="xs"
                PaperProps={{
                    sx: {
                        bgcolor: 'background.paper',
                        color: 'text.primary',
                        border: '1px solid rgba(255,255,255,0.08)'
                    }
                }}
            >
                <DialogTitle sx={{ fontWeight: 900 }}>套用折扣</DialogTitle>
                <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
                    {appliedPromotion && (
                        <Alert severity="success">
                            已套用 {appliedPromotion.name}，折抵 {formatMoney(appliedPromotion.discountAmount)}
                        </Alert>
                    )}
                    <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 1 }}>
                        {[5, 10, 20].map((percent) => (
                            <Button
                                key={percent}
                                variant="outlined"
                                onClick={() => applyDiscountPercent(percent)}
                                sx={{ minHeight: 44, color: 'text.primary', borderColor: 'rgba(255,255,255,0.12)' }}
                            >
                                {percent}%
                            </Button>
                        ))}
                    </Box>
                    <TextField
                        label="折扣金額"
                        type="number"
                        value={discountInput}
                        onChange={(event) => setDiscountInput(event.target.value)}
                        inputProps={{ min: 0, max: totals.subtotal, step: 1 }}
                        helperText={`最多可折 ${formatMoney(totals.subtotal)}`}
                        fullWidth
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'text.secondary' }}>
                        <Typography variant="body2">折扣後小計</Typography>
                        <Typography variant="body2">{formatMoney(Math.max(0, totals.subtotal - (Number(discountInput) || 0)))}</Typography>
                    </Box>
                    <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />
                    <TextField
                        label="優惠碼"
                        value={promotionCodeInput}
                        onChange={(event) => {
                            setPromotionCodeInput(event.target.value);
                            setPromotionError('');
                        }}
                        placeholder="例如 CAFE20"
                        helperText="優惠碼會取代目前手動或會員折扣"
                        fullWidth
                    />
                    {promotionError && (
                        <Alert severity="warning" onClose={() => setPromotionError('')}>
                            {promotionError}
                        </Alert>
                    )}
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={handleClearDiscount} sx={{ color: 'text.secondary' }}>
                        清除
                    </Button>
                    <Box sx={{ flexGrow: 1 }} />
                    <Button onClick={() => setDiscountOpen(false)} sx={{ color: 'text.secondary' }}>
                        取消
                    </Button>
                    <Button
                        variant="outlined"
                        disabled={!promotionCodeInput.trim() || promotionLoading}
                        onClick={handleApplyPromotionCode}
                        sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.12)' }}
                    >
                        套優惠碼
                    </Button>
                    <Button variant="contained" color="secondary" onClick={confirmDiscount}>
                        套用
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog
                open={holdOpen}
                onClose={() => setHoldOpen(false)}
                fullWidth
                maxWidth="sm"
                PaperProps={{
                    sx: {
                        bgcolor: 'background.paper',
                        color: 'text.primary',
                        border: '1px solid rgba(255,255,255,0.08)'
                    }
                }}
            >
                <DialogTitle sx={{ fontWeight: 900 }}>掛單與取單</DialogTitle>
                <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
                    {itemCount > 0 && (
                        <Box
                            sx={{
                                p: 2,
                                borderRadius: 2,
                                bgcolor: 'rgba(255,109,0,0.08)',
                                border: '1px solid rgba(255,109,0,0.22)',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                gap: 2
                            }}
                        >
                            <Box>
                                <Typography fontWeight={900}>目前訂單</Typography>
                                <Typography variant="caption" color="text.secondary">
                                    {itemCount} 項 · {formatMoney(totals.total)}
                                    {selectedMember ? ` · ${selectedMember.name}` : ''}
                                </Typography>
                            </Box>
                            <Button variant="contained" color="secondary" onClick={handleHoldCurrentOrder}>
                                掛起
                            </Button>
                        </Box>
                    )}

                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                        {heldOrders.map((order) => (
                            <Box
                                key={order.id}
                                sx={{
                                    p: 1.5,
                                    borderRadius: 2,
                                    bgcolor: 'rgba(255,255,255,0.03)',
                                    border: '1px solid rgba(255,255,255,0.08)',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    gap: 1.5
                                }}
                            >
                                <Box sx={{ minWidth: 0 }}>
                                    <Typography fontWeight={900}>{order.displayNo}</Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        {formatHeldTime(order.createdAt)} · {order.itemCount} 項 · {formatMoney(order.total)}
                                        {order.selectedMember ? ` · ${order.selectedMember.name}` : ''}
                                    </Typography>
                                </Box>
                                <Box sx={{ display: 'flex', gap: 1, flexShrink: 0 }}>
                                    <Button
                                        variant="outlined"
                                        disabled={itemCount > 0}
                                        onClick={() => handleRestoreHeldOrder(order.id)}
                                        sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.12)' }}
                                    >
                                        取回
                                    </Button>
                                    <IconButton
                                        aria-label={`刪除掛單 ${order.displayNo}`}
                                        size="small"
                                        onClick={() => handleRemoveHeldOrder(order.id)}
                                        sx={{ color: 'error.main' }}
                                    >
                                        <DeleteOutline fontSize="small" />
                                    </IconButton>
                                </Box>
                            </Box>
                        ))}
                    </Box>

                    {heldOrders.length === 0 && (
                        <Box sx={{ py: 4, textAlign: 'center', color: 'text.secondary' }}>
                            <Typography fontWeight={900} color="text.primary">目前沒有掛單</Typography>
                            <Typography variant="body2">有品項的訂單可先掛起，稍後再取回結帳。</Typography>
                        </Box>
                    )}
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={() => setHoldOpen(false)} sx={{ color: 'text.secondary' }}>
                        關閉
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog
                open={memberOpen}
                onClose={() => setMemberOpen(false)}
                fullWidth
                maxWidth="sm"
                PaperProps={{
                    sx: {
                        bgcolor: 'background.paper',
                        color: 'text.primary',
                        border: '1px solid rgba(255,255,255,0.08)'
                    }
                }}
            >
                <DialogTitle sx={{ fontWeight: 900 }}>綁定會員</DialogTitle>
                <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
                    <TextField
                        label="手機、姓名或會員編號"
                        value={memberQuery}
                        onChange={handleMemberQueryChange}
                        helperText="輸入關鍵字後查詢 CRM 會員資料"
                        fullWidth
                    />
                    {memberError && (
                        <Alert severity="error" onClose={() => setMemberError('')}>
                            {memberError}
                        </Alert>
                    )}
                    {memberLoading && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                            <CircularProgress size={24} />
                        </Box>
                    )}
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                        {!memberLoading && memberCandidates.map((member) => (
                            <Button
                                key={member.id}
                                variant="outlined"
                                onClick={() => handleSelectMember(member)}
                                sx={{
                                    p: 1.5,
                                    justifyContent: 'space-between',
                                    textAlign: 'left',
                                    color: 'text.primary',
                                    borderColor: selectedMember?.id === member.id ? 'rgba(255,138,101,0.72)' : 'rgba(255,255,255,0.1)',
                                    bgcolor: selectedMember?.id === member.id ? 'rgba(255,138,101,0.1)' : 'rgba(255,255,255,0.03)'
                                }}
                            >
                                <Box sx={{ minWidth: 0 }}>
                                    <Typography fontWeight={900}>{member.name}</Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        {member.memberNo} · {member.phoneMasked}
                                    </Typography>
                                </Box>
                                <Box sx={{ textAlign: 'right', flexShrink: 0 }}>
                                    <Typography fontWeight={900} color="secondary.main">
                                        {member.discountPercent > 0 ? `${member.discountPercent}%` : '無折扣'}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        {member.tier} · {member.points.toLocaleString()} 點
                                    </Typography>
                                </Box>
                            </Button>
                        ))}
                    </Box>
                    {!memberLoading && memberQuery.trim() && memberCandidates.length === 0 && !memberError && (
                        <Box sx={{ py: 4, textAlign: 'center', color: 'text.secondary' }}>
                            <Typography fontWeight={900} color="text.primary">找不到會員</Typography>
                            <Typography variant="body2">請確認手機、姓名或會員編號。</Typography>
                        </Box>
                    )}
                    {!memberQuery.trim() && (
                        <Box sx={{ py: 4, textAlign: 'center', color: 'text.secondary' }}>
                            <Typography fontWeight={900} color="text.primary">輸入關鍵字開始搜尋</Typography>
                            <Typography variant="body2">可輸入手機、姓名、會員編號、卡號或條碼。</Typography>
                        </Box>
                    )}
                    {selectedMember && (
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'text.secondary' }}>
                            <Typography variant="body2">目前會員折扣</Typography>
                            <Typography variant="body2">-{formatMoney(totals.discount)}</Typography>
                        </Box>
                    )}
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={handleClearMember} disabled={!selectedMember} sx={{ color: 'text.secondary' }}>
                        解除
                    </Button>
                    <Box sx={{ flexGrow: 1 }} />
                    <Button onClick={() => setMemberOpen(false)} sx={{ color: 'text.secondary' }}>
                        關閉
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default Cart;
