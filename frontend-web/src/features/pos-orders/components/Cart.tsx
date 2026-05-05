import React from 'react';
import { Box, Typography, Button, IconButton, Divider } from '@mui/material';
import { DeleteOutline, Add, Remove, PersonAdd, LocalOffer } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const Cart: React.FC = () => {
    const navigate = useNavigate();
    
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h6" fontWeight="bold">Current Order</Typography>
                <IconButton size="small" sx={{ color: 'text.secondary' }}>
                   <DeleteOutline />
                </IconButton>
            </Box>

            {/* Order Items List */}
            <Box sx={{ flexGrow: 1, overflowY: 'auto', mb: 2 }}>
                {[
                    {name: 'Iced Oat Latte', price: 6.50, qty: 1, note: 'EXTRA ICE'},
                    {name: 'Butter Croissant', price: 9.50, qty: 2, note: 'WARMED'},
                ].map((item, index) => (
                    <Box key={index} sx={{ mb: 3 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                            <Typography variant="body1" fontWeight={600}>{item.name}</Typography>
                            <Typography variant="body1" fontWeight={600}>${item.price.toFixed(2)}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <IconButton size="small" sx={{ bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 1 }}><Remove fontSize="small" /></IconButton>
                                <Typography sx={{ minWidth: 20, textAlign: 'center', fontWeight: 'bold' }}>{item.qty}</Typography>
                                <IconButton size="small" sx={{ bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 1 }}><Add fontSize="small" /></IconButton>
                            </Box>
                            <Typography variant="caption" color="text.secondary" sx={{ letterSpacing: 1 }}>{item.note}</Typography>
                        </Box>
                    </Box>
                ))}
            </Box>

            <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)', mb: 2 }} />

            {/* Totals */}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'text.secondary' }}>
                    <Typography>Subtotal</Typography>
                    <Typography>$16.00</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'text.secondary' }}>
                    <Typography>Tax (8%)</Typography>
                    <Typography>$1.28</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: 'success.main' }}>
                    <Typography>Discount</Typography>
                    <Typography>-$0.00</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1, alignItems: 'center' }}>
                    <Typography variant="h5" fontWeight="bold">Total</Typography>
                    <Typography variant="h4" fontWeight="bold">$17.28</Typography>
                </Box>
            </Box>

            {/* Actions */}
            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <Button variant="outlined" fullWidth startIcon={<LocalOffer />} sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.05)' }}>
                    Discount
                </Button>
                <Button variant="outlined" fullWidth startIcon={<PersonAdd />} sx={{ color: 'text.primary', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.05)' }}>
                    Customer
                </Button>
            </Box>

            <Button 
                variant="contained" 
                color="secondary" 
                fullWidth 
                onClick={() => navigate('/pos/checkout')}
                sx={{ py: 2, mb: 2, borderRadius: 2, display: 'flex', justifyContent: 'space-between', px: 3 }}
            >
                <Typography variant="h6" fontWeight="bold">PAY NOW</Typography>
                <Typography variant="h6" fontWeight="bold">$17.28</Typography>
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
