/**
 * @file RegisterPage.tsx
 * @description POS 收銀點單頁面 / POS register ordering page
 * @description_en Provides API-backed product browsing, category filtering and cart portal mounting
 * @description_zh 提供串接 API 的商品瀏覽、分類篩選與購物車掛載功能
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
    Alert, Box, TextField, InputAdornment, Button, Card, CardContent,
    Typography, Chip, IconButton, Tooltip, CircularProgress
} from '@mui/material';
import { AddShoppingCart, Bolt, GridView, LocalOffer, QrCodeScanner, Search } from '@mui/icons-material';
import { createPortal } from 'react-dom';
import Cart from '../components/Cart';
import { productApi } from '../../pos-products/api/productApi';
import type { Category, ProductItem } from '../../pos-products/types';
import { useCartStore } from '../store/cartStore';
import { formatMoney } from '@shared/utils';
import { useAuthStore } from '@shared/store/authStore';

const QUICK_ACTIONS = [
    { label: '折扣', icon: <LocalOffer fontSize="small" /> },
    { label: '急單', icon: <Bolt fontSize="small" /> },
    { label: '格狀', icon: <GridView fontSize="small" /> },
];

const FEATURED_CATEGORY_NAMES = ['咖啡飲品', '烘焙點心'];
const GENERATED_TEST_TEXT_PATTERNS = [/^Smoke Test/i, /^API Debug/i, /^api-debug/i, /測試/, /退款測試/, /瀏覽器測試/];
const GENERATED_TEST_SKU_PREFIXES = ['SMOKE-', 'RF-', 'BAR-177', 'API-DEBUG'];

// ========================================
// 收銀排序規則 / Register Sort Rules
// ========================================
const isFeaturedCategory = (category: Category) => FEATURED_CATEGORY_NAMES.includes(category.name);

const isFeaturedProduct = (product: ProductItem) => product.sku.startsWith('DEMO-');

const includesGeneratedTestText = (value: string | null | undefined) =>
    GENERATED_TEST_TEXT_PATTERNS.some((pattern) => pattern.test(value ?? ''));

const isGeneratedTestCategory = (category: Category) =>
    includesGeneratedTestText(category.name);

const isGeneratedTestProduct = (product: ProductItem, categories: Category[]) => {
    const categoryName = getCategoryName(categories, product.categoryId);
    return (
        includesGeneratedTestText(product.name) ||
        includesGeneratedTestText(product.sku) ||
        includesGeneratedTestText(product.barcodePrimary) ||
        includesGeneratedTestText(categoryName) ||
        GENERATED_TEST_SKU_PREFIXES.some((prefix) => product.sku.startsWith(prefix))
    );
};

const getCategoryName = (categories: Category[], categoryId: string | null) => (
    categories.find((category) => category.id === categoryId)?.name ?? '未分類'
);

const sortCategoriesForRegister = (items: Category[]) => (
    [...items].sort((a, b) => {
        const featuredOrder = Number(isFeaturedCategory(b)) - Number(isFeaturedCategory(a));
        if (featuredOrder !== 0) return featuredOrder;

        const sortOrder = a.sortOrder - b.sortOrder;
        if (sortOrder !== 0) return sortOrder;

        return a.name.localeCompare(b.name, 'zh-Hant');
    })
);

const sortProductsForRegister = (items: ProductItem[], categories: Category[]) => (
    [...items].sort((a, b) => {
        const featuredProductOrder = Number(isFeaturedProduct(b)) - Number(isFeaturedProduct(a));
        if (featuredProductOrder !== 0) return featuredProductOrder;

        const aFeaturedCategory = FEATURED_CATEGORY_NAMES.includes(getCategoryName(categories, a.categoryId));
        const bFeaturedCategory = FEATURED_CATEGORY_NAMES.includes(getCategoryName(categories, b.categoryId));
        const featuredCategoryOrder = Number(bFeaturedCategory) - Number(aFeaturedCategory);
        if (featuredCategoryOrder !== 0) return featuredCategoryOrder;

        return a.name.localeCompare(b.name, 'zh-Hant');
    })
);

const RegisterPage: React.FC = () => {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const addProduct = useCartStore((state) => state.addProduct);
    const [activeCategory, setActiveCategory] = useState('');
    const [searchInput, setSearchInput] = useState('');
    const [keyword, setKeyword] = useState('');
    const [products, setProducts] = useState<ProductItem[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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

    // ========================================
    // 商品資料載入 / Product Loading
    // ========================================
    const loadRegisterProducts = useCallback(async () => {
        if (!isAuthenticated) {
            setProducts([]);
            setCategories([]);
            setError('請先完成 POS PIN 登入後再載入收銀台商品資料。');
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const [productResponse, categoryResponse] = await Promise.all([
                productApi.getProducts({
                    page: 0,
                    size: 60,
                    keyword: keyword || undefined,
                    categoryId: activeCategory || undefined,
                }),
                productApi.getCategories(),
            ]);

            const allCategories = categoryResponse.success ? categoryResponse.data : [];
            const nextCategories = categoryResponse.success
                ? sortCategoriesForRegister(allCategories.filter((category) => category.active && !isGeneratedTestCategory(category)))
                : [];

            setCategories(nextCategories);

            if (productResponse.success) {
                setProducts(sortProductsForRegister(
                    productResponse.data.content.filter((product) => (
                        product.sellable &&
                        product.active &&
                        !isGeneratedTestProduct(product, allCategories)
                    )),
                    allCategories
                ));
            }
        } catch {
            setError('載入商品資料失敗，請稍後重試。');
            setProducts([]);
            setCategories([]);
        } finally {
            setLoading(false);
        }
    }, [activeCategory, isAuthenticated, keyword]);

    useEffect(() => { loadRegisterProducts(); }, [loadRegisterProducts]);

    const activeCategoryName = useMemo(
        () => categories.find((category) => category.id === activeCategory)?.name,
        [activeCategory, categories]
    );

    const handleSearch = () => setKeyword(searchInput.trim());

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
                                minHeight: 52,
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
                    value={searchInput}
                    onChange={(event) => setSearchInput(event.target.value)}
                    onKeyDown={(event) => event.key === 'Enter' && handleSearch()}
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
                    {[{ id: '', name: '全部商品' }, ...categories].map(cat => {
                        const isActive = activeCategory === cat.id;
                        return (
                            <Button
                                key={cat.id || 'all'}
                                onClick={() => setActiveCategory(cat.id)}
                                variant="contained"
                                sx={{
                                    borderRadius: 2,
                                    whiteSpace: 'nowrap',
                                    flexShrink: 0,
                                    minHeight: 52,
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
                                {cat.name}
                            </Button>
                        );
                    })}
                </Box>

                <Tooltip title="掃描條碼">
                    <IconButton sx={{
                        minWidth: 52,
                        width: 52,
                        minHeight: 52,
                        height: 52,
                        justifySelf: { xs: 'stretch', md: 'stretch' },
                        bgcolor: 'rgba(255,109,0,0.14)',
                        color: 'secondary.main',
                        borderRadius: 2,
                        '&:hover': { bgcolor: 'rgba(255,109,0,0.22)' }
                    }}>
                        <QrCodeScanner />
                    </IconButton>
                </Tooltip>
            </Box>

            {error && <Alert severity="warning" sx={{ mb: 2 }}>{error}</Alert>}

            <Box sx={{
                display: 'grid',
                gridTemplateColumns: {
                    xs: '1fr',
                    sm: 'repeat(auto-fill, minmax(240px, 1fr))',
                    xl: 'repeat(auto-fill, minmax(260px, 1fr))'
                },
                gap: 2,
                alignItems: 'stretch',
                overflowY: 'auto',
                pr: { xs: 0, md: 0.5 },
                pb: { xs: 12, md: 2 },
                minHeight: 0
            }}>
                {loading && (
                    <Box sx={{ gridColumn: '1 / -1', py: 8, display: 'flex', justifyContent: 'center' }}>
                        <CircularProgress color="secondary" />
                    </Box>
                )}

                {!loading && products.length === 0 && (
                    <Box sx={{
                        gridColumn: '1 / -1',
                        minHeight: 260,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 1,
                        bgcolor: 'background.paper',
                        border: '1px solid rgba(255,255,255,0.06)',
                        borderRadius: 3,
                        textAlign: 'center',
                        px: 3
                    }}>
                        <Typography variant="h6" fontWeight={900}>
                            目前沒有可銷售商品
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {activeCategoryName ? `${activeCategoryName} 分類下沒有商品。` : '請先在商品管理建立商品，或調整搜尋條件。'}
                        </Typography>
                    </Box>
                )}

                {!loading && products.map((item) => (
                    <Card key={item.id} onClick={() => addProduct(item)} sx={{
                        bgcolor: 'background.paper',
                        borderRadius: 2,
                        cursor: 'pointer',
                        transition: 'transform 0.14s ease, box-shadow 0.14s ease, border-color 0.14s ease',
                        border: '1px solid rgba(255,255,255,0.06)',
                        boxShadow: 'none',
                        overflow: 'hidden',
                        minHeight: 232,
                        display: 'flex',
                        flexDirection: 'column',
                        '&:active': { transform: 'scale(0.98)' },
                        '&:hover': {
                            transform: 'translateY(-2px)',
                            borderColor: 'rgba(178,198,255,0.32)',
                            boxShadow: '0 18px 40px rgba(0,0,0,0.22)'
                        }
                    }}>
                        <Box sx={{
                            p: 2,
                            pb: 1,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            gap: 1
                        }}>
                            <Chip
                                size="small"
                                label={getCategoryName(categories, item.categoryId)}
                                sx={{
                                    bgcolor: 'rgba(15,18,27,0.82)',
                                    color: 'white',
                                    fontWeight: 700,
                                    maxWidth: '70%',
                                    '& .MuiChip-label': {
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis'
                                    }
                                }}
                            />
                            {isFeaturedProduct(item) && (
                                <Chip
                                    size="small"
                                    label="Demo"
                                    sx={{
                                        bgcolor: 'rgba(178,198,255,0.16)',
                                        color: '#B2C6FF',
                                        fontWeight: 800
                                    }}
                                />
                            )}
                        </Box>
                        <CardContent sx={{
                            p: 2,
                            pt: 0.5,
                            flexGrow: 1,
                            display: 'flex',
                            flexDirection: 'column',
                            '&:last-child': { pb: 2 }
                        }}>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{
                                    display: 'block',
                                    mb: 0.75,
                                    fontWeight: 700,
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    whiteSpace: 'nowrap'
                                }}
                            >
                                {item.sku}
                            </Typography>
                            <Typography
                                variant="h6"
                                fontWeight={900}
                                sx={{
                                    fontSize: '1.08rem',
                                    lineHeight: 1.25,
                                    minHeight: 54,
                                    display: '-webkit-box',
                                    WebkitLineClamp: 2,
                                    WebkitBoxOrient: 'vertical',
                                    overflow: 'hidden'
                                }}
                            >
                                {item.name}
                            </Typography>
                            <Box sx={{
                                mt: 'auto',
                                pt: 2,
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'flex-end',
                                gap: 1.5
                            }}>
                                <Box sx={{ minWidth: 0 }}>
                                    <Typography color="secondary.main" fontWeight={950} sx={{
                                        fontSize: '1.45rem',
                                        lineHeight: 1,
                                        fontVariantNumeric: 'tabular-nums',
                                        letterSpacing: 0
                                    }}>
                                        {formatMoney(item.basePrice)}
                                    </Typography>
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        sx={{
                                            display: 'block',
                                            mt: 0.75,
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis',
                                            whiteSpace: 'nowrap'
                                        }}
                                    >
                                        {item.barcodePrimary ?? item.unit}
                                    </Typography>
                                </Box>
                                <Box sx={{
                                    width: 52,
                                    height: 52,
                                    minWidth: 52,
                                    flexShrink: 0,
                                    borderRadius: 2,
                                    display: 'grid',
                                    placeItems: 'center',
                                    bgcolor: 'rgba(255,109,0,0.14)',
                                    color: 'secondary.main'
                                }}>
                                    <AddShoppingCart fontSize="small" />
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
