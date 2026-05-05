/**
 * @file CheckoutPage.tsx
 * @description POS 結帳與付款選擇頁面 / POS Checkout and Payment Selection
 */
import React from 'react';
import { Box, Typography, Button, Avatar, Divider } from '@mui/material';
import { 
    Payments, CreditCard, AccountBalanceWallet, QrCode, 
    Nfc, MoreHoriz, ArrowBack 
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const CheckoutPage: React.FC = () => {
    const navigate = useNavigate();

    const orderItems = [
        { name: 'Miso Salmon Grain Bowl', qty: 2, price: 760, img: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100&q=80' },
        { name: 'Artisan Caffe Latte', qty: 2, price: 320, img: 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=100&q=80' }
    ];

    const paymentMethods = [
        { id: 'cash', label: 'Cash', icon: <Payments sx={{ fontSize: 32 }} />, color: '#4CAF50', bg: 'rgba(76, 175, 80, 0.15)' },
        { id: 'credit', label: 'Credit Card', icon: <CreditCard sx={{ fontSize: 32 }} />, color: '#5C67FF', bg: 'rgba(92, 103, 255, 0.15)' },
        { id: 'linepay', label: 'LINE Pay', icon: <AccountBalanceWallet sx={{ fontSize: 32 }} />, color: '#00C300', bg: 'rgba(0, 195, 0, 0.15)' },
        { id: 'jkopay', label: 'JKO Pay', icon: <QrCode sx={{ fontSize: 32 }} />, color: '#E2263C', bg: 'rgba(226, 38, 60, 0.15)' },
        { id: 'easycard', label: 'EasyCard', icon: <Nfc sx={{ fontSize: 32 }} />, color: '#FF7D00', bg: 'rgba(255, 125, 0, 0.15)' },
        { id: 'others', label: 'Others', icon: <MoreHoriz sx={{ fontSize: 32 }} />, color: '#9CA3AF', bg: 'rgba(156, 163, 175, 0.15)' },
    ];

    return (
        <Box sx={{ 
            height: '100%', 
            display: 'flex', 
            bgcolor: 'background.paper', // #1E1F26 based on our paper background
            borderRadius: 3, 
            overflow: 'hidden',
            border: '1px solid rgba(255,255,255,0.05)'
        }}>
            
            {/* Left Column: Order Summary */}
            <Box sx={{ 
                width: '45%', 
                p: { xs: 3, md: 5 }, 
                display: 'flex', 
                flexDirection: 'column',
                borderRight: '1px solid rgba(255,255,255,0.05)'
            }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 5 }}>
                    <Typography variant="h6" fontWeight="bold">Order Summary</Typography>
                    <Box sx={{ bgcolor: 'rgba(255,255,255,0.05)', px: 1.5, py: 0.5, borderRadius: 1 }}>
                        <Typography variant="caption" color="text.secondary" fontFamily="monospace">#INV-8842</Typography>
                    </Box>
                </Box>

                {/* Items List */}
                <Box sx={{ flexGrow: 1, overflowY: 'auto' }}>
                    {orderItems.map((item, idx) => (
                        <Box key={idx} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                <Avatar variant="rounded" src={item.img} sx={{ width: 56, height: 56, borderRadius: 2 }} />
                                <Box>
                                    <Typography variant="body1" fontWeight="bold" sx={{ mb: 0.5 }}>{item.name}</Typography>
                                    <Typography variant="body2" color="text.secondary">Qty: {item.qty}</Typography>
                                </Box>
                            </Box>
                            <Typography variant="body1" fontWeight="bold" color="text.secondary">NT${item.price}</Typography>
                        </Box>
                    ))}
                </Box>

                {/* Totals */}
                <Box sx={{ mt: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: 'text.secondary' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">SUBTOTAL</Typography>
                        <Typography variant="body2">NT$1080</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: 'text.secondary' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">TAX (5%)</Typography>
                        <Typography variant="body2">NT$54</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, color: '#FF8A65' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">MEMBER DISCOUNT</Typography>
                        <Typography variant="body2">-NT$100</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 4, color: '#64B5F6' }}>
                        <Typography variant="body2" letterSpacing={1} fontWeight="bold">PROMO: WELCOME24</Typography>
                        <Typography variant="body2">-NT$50</Typography>
                    </Box>
                    
                    <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)', mb: 3 }} />
                    
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                        <Typography variant="subtitle1" fontWeight="bold" letterSpacing={1} sx={{ color: 'text.secondary', mb: 1 }}>TOTAL AMOUNT</Typography>
                        <Typography variant="h2" fontWeight="bold" sx={{ color: '#E0E7FF' }}>NT$984</Typography>
                    </Box>
                </Box>
            </Box>


            {/* Right Column: Payment Options */}
            <Box sx={{ 
                width: '55%', 
                p: { xs: 3, md: 5 }, 
                display: 'flex', 
                flexDirection: 'column' 
            }}>
                <Box sx={{ mb: 5 }}>
                    <Typography variant="h6" fontWeight="bold" sx={{ mb: 0.5 }}>Select Payment</Typography>
                    <Typography variant="body2" color="text.secondary">Please choose the customer's preferred method</Typography>
                </Box>

                {/* Grid of options */}
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
                            sx={{
                                border: '1px solid rgba(255,255,255,0.05)',
                                bgcolor: 'rgba(255,255,255,0.02)',
                                borderRadius: 4,
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                gap: 2,
                                color: 'text.primary',
                                textTransform: 'none',
                                transition: 'all 0.2s',
                                '&:hover': {
                                    bgcolor: 'rgba(255,255,255,0.06)',
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

                {/* Bottom Actions */}
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
                        Back to Cart
                    </Button>
                    <Button 
                        variant="contained" 
                        sx={{ 
                            flex: 2, 
                            py: 2, 
                            borderRadius: 2, 
                            background: 'linear-gradient(90deg, #7048E8 0%, #4D329A 100%)',
                            color: 'white',
                            fontWeight: 'bold',
                            fontSize: '16px'
                        }}
                    >
                        Confirm Selection
                    </Button>
                </Box>
            </Box>

        </Box>
    );
};

export default CheckoutPage;
