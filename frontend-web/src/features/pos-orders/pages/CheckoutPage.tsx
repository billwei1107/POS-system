/**
 * @file CheckoutPage.tsx
 * @description POS 結帳與付款選擇頁面 / POS Checkout and Payment Selection
 */
import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { Box, Typography, Button, Avatar, Divider, Chip, TextField } from '@mui/material';
import { 
    Payments, CreditCard, AccountBalanceWallet, QrCode, 
    Nfc, MoreHoriz, ArrowBack, ReceiptLong
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import {
    calculateCartTotals,
    useCartStore,
    type CartDiscountSource,
    type CartLine,
    type CartMember,
    type CartPromotion,
    type CartTotals,
} from '../store/cartStore';
import { formatMoney } from '@shared/utils';
import { orderApi } from '../api/orderApi';
import type { Order, OrderDiscountSource, OrderType } from '../types';
import { DEFAULT_GUEST_COUNT, DEFAULT_TABLE_NO } from '../config';
import { getActivePosContext } from '../posSession';

const PAYMENT_METHOD_LABEL: Record<string, string> = {
    cash: 'CASH',
    credit: 'CREDIT_CARD',
    linepay: 'LINE_PAY',
    jkopay: 'JKOPAY',
    easycard: 'EASYCARD',
    others: 'OTHER',
};

type ApiErrorBody = {
    message?: string;
};

interface CheckoutReceiptSnapshot {
    lines: CartLine[];
    totals: CartTotals;
    discountSource: CartDiscountSource | null;
    selectedMember: CartMember | null;
    appliedPromotion: CartPromotion | null;
    discountLabel: string;
}

const toOrderDiscountSource = (source: 'manual' | 'member' | 'promotion' | null): OrderDiscountSource | undefined => {
    if (source === 'manual') return 'MANUAL';
    if (source === 'member') return 'MEMBER';
    if (source === 'promotion') return 'PROMOTION';
    return undefined;
};

const CheckoutPage: React.FC = () => {
    const navigate = useNavigate();
    const [selectedMethod, setSelectedMethod] = useState('cash');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [completedOrder, setCompletedOrder] = useState<Order | null>(null);
    const [receiptSnapshot, setReceiptSnapshot] = useState<CheckoutReceiptSnapshot | null>(null);
    const [cashTendered, setCashTendered] = useState('');
    const orderItems = useCartStore((state) => state.lines);
    const taxRate = useCartStore((state) => state.taxRate);
    const discountAmount = useCartStore((state) => state.discountAmount);
    const discountSource = useCartStore((state) => state.discountSource);
    const selectedMember = useCartStore((state) => state.selectedMember);
    const appliedPromotion = useCartStore((state) => state.appliedPromotion);
    const clearCart = useCartStore((state) => state.clear);
    const posContext = useMemo(() => getActivePosContext(), []);
    const totals = useMemo(() => calculateCartTotals(orderItems, taxRate, discountAmount), [discountAmount, orderItems, taxRate]);
    const cashTenderedAmount = useMemo(() => {
        const value = Number(cashTendered);
        return Number.isFinite(value) ? value : 0;
    }, [cashTendered]);
    const discountLabel = useMemo(() => {
        if (discountSource === 'member' && selectedMember) return '會員折扣';
        if (discountSource === 'promotion' && appliedPromotion) return appliedPromotion.name;
        if (discountSource === 'manual') return '手動折扣';
        return '折扣';
    }, [appliedPromotion, discountSource, selectedMember]);
    const summaryLines = completedOrder && receiptSnapshot ? receiptSnapshot.lines : orderItems;
    const summaryTotals = completedOrder && receiptSnapshot ? receiptSnapshot.totals : totals;
    const summaryMember = completedOrder && receiptSnapshot ? receiptSnapshot.selectedMember : selectedMember;
    const summaryDiscountSource = completedOrder && receiptSnapshot ? receiptSnapshot.discountSource : discountSource;
    const summaryPromotion = completedOrder && receiptSnapshot ? receiptSnapshot.appliedPromotion : appliedPromotion;
    const summaryDiscountLabel = completedOrder && receiptSnapshot ? receiptSnapshot.discountLabel : discountLabel;
    const paymentCompleted = Boolean(completedOrder);
    const cashShortfall = Math.max(0, summaryTotals.total - cashTenderedAmount);
    const changeDue = Math.max(0, cashTenderedAmount - summaryTotals.total);
    const cashPaymentInvalid = selectedMethod === 'cash' && !paymentCompleted && cashShortfall > 0;

    const paymentMethods = [
        { id: 'cash', label: '現金', icon: <Payments sx={{ fontSize: 32 }} />, color: '#4CAF50', bg: 'rgba(76, 175, 80, 0.15)' },
        { id: 'credit', label: '信用卡', icon: <CreditCard sx={{ fontSize: 32 }} />, color: '#5C67FF', bg: 'rgba(92, 103, 255, 0.15)' },
        { id: 'linepay', label: 'LINE Pay', icon: <AccountBalanceWallet sx={{ fontSize: 32 }} />, color: '#00C300', bg: 'rgba(0, 195, 0, 0.15)' },
        { id: 'jkopay', label: '街口支付', icon: <QrCode sx={{ fontSize: 32 }} />, color: '#E2263C', bg: 'rgba(226, 38, 60, 0.15)' },
        { id: 'easycard', label: '悠遊卡', icon: <Nfc sx={{ fontSize: 32 }} />, color: '#FF7D00', bg: 'rgba(255, 125, 0, 0.15)' },
        { id: 'others', label: '其他', icon: <MoreHoriz sx={{ fontSize: 32 }} />, color: '#9CA3AF', bg: 'rgba(156, 163, 175, 0.15)' },
    ];

    useEffect(() => {
        if (completedOrder) return;
        setCashTendered(totals.total > 0 ? String(totals.total) : '');
    }, [completedOrder, totals.total]);

    // ========================================
    // 現金收款 / Cash Tendering
    // ========================================
    const addCashTendered = (amount: number) => {
        setCashTendered(String(Math.max(0, cashTenderedAmount + amount)));
    };

    // ========================================
    // 確認付款 / Confirm Payment
    // ========================================
    const handleConfirmPayment = async () => {
        if (orderItems.length === 0 || submitting) return;
        if (cashPaymentInvalid) {
            setError('現金收款金額不足，請確認收款金額。');
            return;
        }

        setSubmitting(true);
        setError(null);
        setCompletedOrder(null);
        setReceiptSnapshot(null);
        const pendingReceiptSnapshot: CheckoutReceiptSnapshot = {
            lines: orderItems.map((item) => ({ ...item })),
            totals,
            discountSource,
            selectedMember: selectedMember ? { ...selectedMember } : null,
            appliedPromotion: appliedPromotion ? { ...appliedPromotion } : null,
            discountLabel,
        };
        try {
            const createResponse = await orderApi.create({
                storeId: posContext.storeId,
                terminalId: posContext.terminalId,
                employeeId: posContext.employeeId,
                orderType: 'DINE_IN' as OrderType,
                tableNo: DEFAULT_TABLE_NO,
                guestCount: DEFAULT_GUEST_COUNT,
                taxIncluded: false,
                discountAmount: totals.discount,
                discountSource: totals.discount > 0 ? toOrderDiscountSource(discountSource) : undefined,
                discountLabel: totals.discount > 0 ? discountLabel : undefined,
                promotionRuleId: discountSource === 'promotion' && appliedPromotion ? appliedPromotion.ruleId : undefined,
                promotionCode: discountSource === 'promotion' && appliedPromotion?.code ? appliedPromotion.code : undefined,
                memberId: selectedMember?.id,
                items: orderItems.map((item) => ({
                    itemId: item.itemId,
                    itemNameSnapshot: item.name,
                    skuSnapshot: item.sku,
                    unitPrice: item.unitPrice,
                    quantity: item.quantity,
                    modifierPriceAdjustment: 0,
                    note: item.note,
                })),
            });

            if (!createResponse.success || !createResponse.data) {
                throw new Error(createResponse.message || '建立訂單失敗。');
            }

            const completeResponse = await orderApi.complete(
                createResponse.data.id,
                PAYMENT_METHOD_LABEL[selectedMethod] ?? selectedMethod.toUpperCase(),
                selectedMethod === 'cash' ? cashTenderedAmount : undefined
            );

            if (!completeResponse.success || !completeResponse.data) {
                throw new Error(completeResponse.message || '付款完成失敗。');
            }

            setReceiptSnapshot(pendingReceiptSnapshot);
            setCompletedOrder(completeResponse.data);
            clearCart();
        } catch (err) {
            if (axios.isAxiosError<ApiErrorBody>(err)) {
                setError(err.response?.data?.message || err.message || '付款流程失敗，請稍後再試。');
            } else {
                setError(err instanceof Error ? err.message : '付款流程失敗，請稍後再試。');
            }
        } finally {
            setSubmitting(false);
        }
    };

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
                    <Box>
                        <Typography variant="h6" fontWeight="bold">訂單摘要</Typography>
                        {summaryMember && (
                            <Typography variant="caption" color="text.secondary">
                                {summaryMember.tier} · {summaryMember.name} · {summaryMember.points.toLocaleString()} 點
                            </Typography>
                        )}
                    </Box>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
                        <Chip
                            label={completedOrder ? completedOrder.orderNo : '尚未送出'}
                            size="small"
                            sx={{ bgcolor: 'rgba(255,255,255,0.06)', color: 'text.secondary', fontFamily: 'monospace', fontWeight: 800 }}
                        />
                        {summaryDiscountSource === 'member' && summaryMember && (
                            <Chip
                                label={`${summaryMember.discountPercent}% 會員折扣`}
                                size="small"
                                sx={{ bgcolor: 'rgba(255,138,101,0.12)', color: '#FFAB91', fontWeight: 800 }}
                            />
                        )}
                        {summaryDiscountSource === 'promotion' && summaryPromotion && (
                            <Chip
                                label={summaryPromotion.code ? `${summaryPromotion.name} · ${summaryPromotion.code}` : summaryPromotion.name}
                                size="small"
                                sx={{ bgcolor: 'rgba(0,230,118,0.12)', color: '#7CFFB2', fontWeight: 800 }}
                            />
                        )}
                    </Box>
                </Box>

                {error && (
                    <Box sx={{ mb: 3, p: 2, borderRadius: 2, bgcolor: 'rgba(255,82,82,0.1)', color: 'error.main', border: '1px solid rgba(255,82,82,0.22)' }}>
                        <Typography variant="body2" fontWeight={800}>{error}</Typography>
                    </Box>
                )}

                {completedOrder && (
                    <Box sx={{ mb: 3, p: 2, borderRadius: 2, bgcolor: 'rgba(76,175,80,0.12)', color: 'success.main', border: '1px solid rgba(76,175,80,0.24)', display: 'flex', gap: 1.5, alignItems: 'center' }}>
                        <ReceiptLong />
                        <Box>
                            <Typography fontWeight={900}>付款完成</Typography>
                            <Typography variant="body2">訂單 {completedOrder.orderNo} 已完成，購物車已清空。</Typography>
                        </Box>
                    </Box>
                )}

                {/* 品項列表 / Items list */}
                <Box sx={{ flexGrow: 1, overflowY: 'auto' }}>
                    {summaryLines.length === 0 && (
                        <Box sx={{ py: 8, textAlign: 'center', color: 'text.secondary' }}>
                            <Typography fontWeight={900} color="text.primary">購物車是空的</Typography>
                            <Typography variant="body2">返回收銀台加入商品後再選擇付款方式。</Typography>
                        </Box>
                    )}

                    {summaryLines.map((item) => (
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
                        <Typography variant="body2">{formatMoney(summaryTotals.subtotal)}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: 'text.secondary' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">稅額 (5%)</Typography>
                        <Typography variant="body2">{formatMoney(summaryTotals.tax)}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: '#FF8A65' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">{summaryDiscountLabel}</Typography>
                        <Typography variant="body2">-{formatMoney(summaryTotals.discount)}</Typography>
                    </Box>
                    
                    <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)', mb: 3 }} />
                    
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                        <Typography variant="subtitle1" fontWeight="bold" letterSpacing={1} sx={{ color: 'text.secondary', mb: 1 }}>應收金額</Typography>
                        <Typography variant="h2" fontWeight="bold" sx={{ color: '#E0E7FF' }}>{formatMoney(summaryTotals.total)}</Typography>
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
                            disabled={paymentCompleted}
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

                {selectedMethod === 'cash' && (
                    <Box
                        sx={{
                            mt: 4,
                            p: 2.5,
                            borderRadius: 2,
                            bgcolor: 'rgba(255,255,255,0.03)',
                            border: '1px solid rgba(255,255,255,0.06)',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: 2
                        }}
                    >
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 2 }}>
                            <Typography fontWeight={900}>現金收款</Typography>
                            <Typography variant="body2" color="text.secondary">
                                應收 {formatMoney(summaryTotals.total)}
                            </Typography>
                        </Box>
                        <TextField
                            label="收款金額"
                            type="number"
                            value={cashTendered}
                            onChange={(event) => setCashTendered(event.target.value)}
                            inputProps={{ min: 0, step: 1 }}
                            disabled={paymentCompleted}
                            fullWidth
                        />
                        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: 'repeat(2, 1fr)', sm: 'repeat(4, 1fr)' }, gap: 1 }}>
                            <Button
                                variant="outlined"
                                onClick={() => setCashTendered(String(totals.total))}
                                disabled={paymentCompleted}
                                sx={{ minHeight: 56, color: 'text.primary', borderColor: 'rgba(255,255,255,0.12)' }}
                            >
                                剛好
                            </Button>
                            {[10, 20, 50].map((amount) => (
                                <Button
                                    key={amount}
                                    variant="outlined"
                                    onClick={() => addCashTendered(amount)}
                                    disabled={paymentCompleted}
                                    sx={{ minHeight: 56, color: 'text.primary', borderColor: 'rgba(255,255,255,0.12)' }}
                                >
                                    +{formatMoney(amount)}
                                </Button>
                            ))}
                        </Box>
                        <Divider sx={{ borderColor: 'rgba(255,255,255,0.06)' }} />
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', color: cashPaymentInvalid ? 'error.main' : 'success.main' }}>
                            <Typography fontWeight={900}>{cashPaymentInvalid ? '不足' : '找零'}</Typography>
                            <Typography fontWeight={900}>
                                {formatMoney(cashPaymentInvalid ? cashShortfall : changeDue)}
                            </Typography>
                        </Box>
                    </Box>
                )}

                {/* 底部操作 / Bottom actions */}
                <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
                    <Button 
                        variant="contained" 
                        startIcon={<ArrowBack />} 
                        onClick={() => {
                            if (paymentCompleted) {
                                navigate('/pos/register');
                                return;
                            }
                            navigate(-1);
                        }}
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
                        {paymentCompleted ? '返回收銀台' : '返回購物車'}
                    </Button>
                    <Button 
                        variant="contained" 
                        disabled={paymentCompleted || orderItems.length === 0 || submitting || cashPaymentInvalid}
                        onClick={handleConfirmPayment}
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
                        {paymentCompleted ? '付款已完成' : submitting ? '付款處理中' : '確認付款方式'}
                    </Button>
                </Box>
                {completedOrder && (
                    <Button
                        variant="outlined"
                        onClick={() => navigate('/pos/orders')}
                        sx={{ mt: 2, color: 'text.primary', borderColor: 'rgba(255,255,255,0.12)' }}
                    >
                        查看訂單列表
                    </Button>
                )}
            </Box>

        </Box>
    );
};

export default CheckoutPage;
