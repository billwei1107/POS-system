import React, { useEffect, useState } from 'react';
import { Box, TextField, InputAdornment, Button, Card, CardMedia, CardContent, Typography } from '@mui/material';
import { Search } from '@mui/icons-material';
import { createPortal } from 'react-dom';
import Cart from '../components/Cart';

const MOCK_ITEMS = [
    { title: 'Iced Oat Latte', price: 6.50, category: 'Beverage', tags: '12oz', image: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&q=80&w=400' },
    { title: 'Butter Croissant', price: 4.75, category: 'Bakery', tags: 'Fresh', image: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&q=80&w=400' },
    { title: 'Double Espresso', price: 3.50, category: 'Beverage', tags: '2oz', image: 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&q=80&w=400' },
    { title: 'Avocado Toast', price: 12.00, category: 'Kitchen', tags: 'All day', image: 'https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&q=80&w=400' },
    { title: 'Origin Pour Over', price: 7.00, category: 'Beverage', tags: 'V60', image: 'https://images.unsplash.com/photo-1444418776041-9c7e33cc5a9c?auto=format&fit=crop&q=80&w=400' },
    { title: 'Peach Iced Tea', price: 5.25, category: 'Beverage', tags: '16oz', image: 'https://images.unsplash.com/photo-1499636136210-6f4ee915583e?auto=format&fit=crop&q=80&w=400' },
];

const RegisterPage: React.FC = () => {
    const categories = ['All Items', 'Coffee', 'Pastries', 'Sandwiches', 'Tea & Drinks'];
    
    // Attempt to portal the cart into the layout slot AFTER initial render
    const [cartNodes, setCartNodes] = useState<{ desktop: Element | null, mobile: Element | null }>({ desktop: null, mobile: null });

    useEffect(() => {
        setCartNodes({
            desktop: document.getElementById('cart-root'),
            mobile: document.getElementById('cart-root-mobile')
        });
    }, []);

    return (
        <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            {/* Top Bar (Search + Categories) */}
            <Box sx={{ mb: 4, display: 'flex', gap: 2, alignItems: 'center', overflowX: 'auto', pb: 1, '&::-webkit-scrollbar': { display: 'none' } }}>
                <TextField 
                    placeholder="Search menu items..."
                    variant="outlined"
                    size="small"
                    sx={{ minWidth: 260, bgcolor: 'background.paper', borderRadius: 2, '& fieldset': { border: 'none' } }}
                    InputProps={{
                        startAdornment: <InputAdornment position="start"><Search /></InputAdornment>,
                    }}
                />
                
                {categories.map((cat, idx) => (
                    <Button 
                        key={idx}
                        variant={idx === 0 ? "contained" : "contained"}
                        color={idx === 0 ? "primary" : "inherit"}
                        sx={{ 
                            borderRadius: 2, 
                            whiteSpace: 'nowrap',
                            bgcolor: idx === 0 ? '#B2C6FF' : 'background.paper', 
                            color: idx === 0 ? '#1A1C23' : 'text.primary',
                            fontWeight: idx === 0 ? 'bold' : 'normal',
                            px: 3,
                            py: 1,
                            boxShadow: 'none',
                            '&:hover': { bgcolor: idx === 0 ? '#9FB7FF' : 'rgba(255,255,255,0.1)', boxShadow: 'none' }
                        }}
                    >
                        {cat}
                    </Button>
                ))}
            </Box>

            {/* Menu Grid - Using CSS Grid for robust reliable layouts */}
            <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 3 }}>
                {MOCK_ITEMS.map((item, idx) => (
                    <Card key={idx} sx={{ 
                        bgcolor: 'background.paper', 
                        borderRadius: 3, 
                        cursor: 'pointer',
                        transition: 'transform 0.1s',
                        '&:active': { transform: 'scale(0.98)' },
                        '&:hover': { boxShadow: '0 8px 16px rgba(0,0,0,0.2)' }
                    }}>
                        <Box sx={{ p: 2, pb: 0 }}>
                           <CardMedia
                               component="img"
                               height="160"
                               image={item.image}
                               alt={item.title}
                               sx={{ borderRadius: 2 }}
                           />
                        </Box>
                        <CardContent>
                            <Typography variant="h6" fontWeight="bold" sx={{ fontSize: '1rem', mb: 0.5 }}>
                                {item.title} <Typography component="span" color="primary.main" fontWeight="bold" sx={{ float: 'right' }}>${item.price.toFixed(2)}</Typography>
                            </Typography>
                            <Typography variant="caption" color="text.secondary" display="block">
                                {item.category} •
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                                {item.tags}
                            </Typography>
                        </CardContent>
                    </Card>
                ))}
            </Box>

            {/* Portals for Cart */}
            {cartNodes.desktop && createPortal(<Cart />, cartNodes.desktop)}
            {cartNodes.mobile && createPortal(<Cart />, cartNodes.mobile)}
        </Box>
    );
};

export default RegisterPage;
