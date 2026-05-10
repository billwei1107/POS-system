/**
 * @file RegisterPage.tsx
 * @description POS 收銀點單頁面 / POS register ordering page
 * @description_en Provides product browsing, category filtering and cart portal mounting
 * @description_zh 提供商品瀏覽、分類篩選與購物車掛載功能
 */
import React, { useEffect, useState } from 'react';
import {
    Box, TextField, InputAdornment, Button, Card, CardMedia, CardContent,
    Typography, Chip, IconButton, Tooltip
} from '@mui/material';
import { AccessTime, Bolt, GridView, LocalOffer, QrCodeScanner, Search } from '@mui/icons-material';
import { createPortal } from 'react-dom';
import Cart from '../components/Cart';

const MOCK_ITEMS = [
    { title: '冰燕麥拿鐵', price: 6.50, category: '咖啡', tags: '12oz', prepTime: '3 分鐘', image: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&q=80&w=400' },
    { title: '奶油可頌', price: 4.75, category: '烘焙', tags: '現烤', prepTime: '1 分鐘', image: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&q=80&w=400' },
    { title: '雙份濃縮', price: 3.50, category: '咖啡', tags: '2oz', prepTime: '2 分鐘', image: 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&q=80&w=400' },
    { title: '酪梨吐司', price: 12.00, category: '廚房', tags: '全天供應', prepTime: '8 分鐘', image: 'https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&q=80&w=400' },
    { title: '單品手沖', price: 7.00, category: '咖啡', tags: 'V60', prepTime: '5 分鐘', image: 'https://images.unsplash.com/photo-1444418776041-9c7e33cc5a9c?auto=format&fit=crop&q=80&w=400' },
    { title: '蜜桃冰茶', price: 5.25, category: '茶飲', tags: '16oz', prepTime: '3 分鐘', image: 'https://images.unsplash.com/photo-1499636136210-6f4ee915583e?auto=format&fit=crop&q=80&w=400' },
];

const CATEGORIES = ['全部商品', '咖啡', '烘焙', '廚房', '茶飲'];

const QUICK_ACTIONS = [
    { label: '折扣', icon: <LocalOffer fontSize="small" /> },
    { label: '急單', icon: <Bolt fontSize="small" /> },
    { label: '格狀', icon: <GridView fontSize="small" /> },
];

const RegisterPage: React.FC = () => {
    const [activeCategory, setActiveCategory] = useState('全部商品');

    // ========================================
    // 購物車掛載 / Cart Portal Mounting
    // ========================================
    const [cartNodes, setCartNodes] = useState<{ desktop: Element | null, mobile: Element | null }>({ desktop: null, mobile: null });

    useEffect(() => {
        const updateCartNodes = () => {
            setCartNodes({
                desktop: document.getElementById('cart-root'),
                mobile: document.getElementById('cart-root-mobile')
            });
        };

        updateCartNodes();
        const observer = new MutationObserver(updateCartNodes);
        observer.observe(document.body, { childList: true, subtree: true });

        return () => observer.disconnect();
    }, []);

    const filteredItems = activeCategory === '全部商品'
        ? MOCK_ITEMS
        : MOCK_ITEMS.filter(item => item.category === activeCategory);

    return (
        <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', minHeight: 0 }}>
            <Box sx={{
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', md: '1fr auto' },
                gap: 2,
                mb: 3,
                alignItems: 'start'
            }}>
                <Box>
                    <Typography variant="h4" fontWeight={800} sx={{ mb: 0.5, letterSpacing: 0 }}>
                        收銀台
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        快速查找商品、觸控式點單，並即時掌握結帳狀態。
                    </Typography>
                </Box>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: { xs: 'flex-start', md: 'flex-end' } }}>
                    {QUICK_ACTIONS.map(action => (
                        <Button
                            key={action.label}
                            variant="outlined"
                            startIcon={action.icon}
                            sx={{
                                minHeight: 44,
                                color: 'text.primary',
                                borderColor: 'rgba(255,255,255,0.12)',
                                bgcolor: 'rgba(255,255,255,0.04)'
                            }}
                        >
                            {action.label}
                        </Button>
                    ))}
                </Box>
            </Box>

            <Box sx={{
                mb: 3,
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', md: 'minmax(260px, 360px) 1fr auto' },
                gap: 2,
                alignItems: 'center'
            }}>
                <TextField
                    placeholder="搜尋商品、SKU 或條碼"
                    variant="outlined"
                    size="small"
                    sx={{
                        bgcolor: 'background.paper',
                        borderRadius: 2,
                        '& fieldset': { border: '1px solid rgba(255,255,255,0.06)' }
                    }}
                    InputProps={{
                        startAdornment: <InputAdornment position="start"><Search /></InputAdornment>,
                    }}
                />

                <Box sx={{
                    display: 'flex',
                    gap: 1,
                    overflowX: 'auto',
                    pb: 0.5,
                    '&::-webkit-scrollbar': { display: 'none' }
                }}>
                    {CATEGORIES.map(cat => {
                        const isActive = activeCategory === cat;
                        return (
                            <Button
                                key={cat}
                                onClick={() => setActiveCategory(cat)}
                                variant="contained"
                                sx={{
                                    borderRadius: 2,
                                    whiteSpace: 'nowrap',
                                    flexShrink: 0,
                                    minHeight: 42,
                                    bgcolor: isActive ? '#B2C6FF' : 'background.paper',
                                    color: isActive ? '#151821' : 'text.primary',
                                    fontWeight: isActive ? 800 : 600,
                                    px: 2.5,
                                    boxShadow: 'none',
                                    border: '1px solid rgba(255,255,255,0.06)',
                                    '&:hover': {
                                        bgcolor: isActive ? '#9FB7FF' : 'rgba(255,255,255,0.08)',
                                        boxShadow: 'none'
                                    }
                                }}
                            >
                                {cat}
                            </Button>
                        );
                    })}
                </Box>

                <Tooltip title="掃描條碼">
                    <IconButton sx={{
                        minWidth: 44,
                        minHeight: 44,
                        bgcolor: 'rgba(255,109,0,0.14)',
                        color: 'secondary.main',
                        borderRadius: 2,
                        '&:hover': { bgcolor: 'rgba(255,109,0,0.22)' }
                    }}>
                        <QrCodeScanner />
                    </IconButton>
                </Tooltip>
            </Box>

            <Box sx={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))',
                gap: 2.5,
                overflowY: 'auto',
                pr: { xs: 0, md: 0.5 },
                pb: 2,
                minHeight: 0
            }}>
                {filteredItems.map((item) => (
                    <Card key={item.title} sx={{
                        bgcolor: 'background.paper',
                        borderRadius: 3,
                        cursor: 'pointer',
                        transition: 'transform 0.14s ease, box-shadow 0.14s ease, border-color 0.14s ease',
                        border: '1px solid rgba(255,255,255,0.06)',
                        boxShadow: 'none',
                        overflow: 'hidden',
                        '&:active': { transform: 'scale(0.98)' },
                        '&:hover': {
                            transform: 'translateY(-2px)',
                            borderColor: 'rgba(178,198,255,0.32)',
                            boxShadow: '0 18px 40px rgba(0,0,0,0.22)'
                        }
                    }}>
                        <Box sx={{ position: 'relative', p: 1.5, pb: 0 }}>
                            <CardMedia
                                component="img"
                                height="148"
                                image={item.image}
                                alt={item.title}
                                sx={{ borderRadius: 2, objectFit: 'cover' }}
                            />
                            <Chip
                                size="small"
                                label={item.category}
                                sx={{
                                    position: 'absolute',
                                    left: 24,
                                    top: 24,
                                    bgcolor: 'rgba(15,18,27,0.82)',
                                    color: 'white',
                                    fontWeight: 700,
                                    backdropFilter: 'blur(8px)'
                                }}
                            />
                        </Box>
                        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1.5, mb: 1 }}>
                                <Typography variant="h6" fontWeight={800} sx={{ fontSize: '1rem', lineHeight: 1.25 }}>
                                    {item.title}
                                </Typography>
                                <Typography color="secondary.main" fontWeight={900} sx={{ fontVariantNumeric: 'tabular-nums' }}>
                                    ${item.price.toFixed(2)}
                                </Typography>
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="caption" color="text.secondary">
                                    {item.tags}
                                </Typography>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary' }}>
                                    <AccessTime sx={{ fontSize: 14 }} />
                                    <Typography variant="caption">{item.prepTime}</Typography>
                                </Box>
                            </Box>
                        </CardContent>
                    </Card>
                ))}
            </Box>

            {/* 購物車傳送門 / Cart portals */}
            {cartNodes.desktop && createPortal(<Cart />, cartNodes.desktop)}
            {cartNodes.mobile && createPortal(<Cart />, cartNodes.mobile)}
        </Box>
    );
};

export default RegisterPage;
