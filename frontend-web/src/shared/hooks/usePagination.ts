/**
 * @file usePagination.ts
 * @description 分頁狀態管理 Hook / Pagination state management hook
 */
import { useState, useCallback } from 'react';

interface PaginationState {
  page: number;
  pageSize: number;
  total: number;
}

interface UsePaginationReturn extends PaginationState {
  setPage: (page: number) => void;
  setPageSize: (size: number) => void;
  setTotal: (total: number) => void;
  reset: () => void;
  pageCount: number;
}

export function usePagination(initialPageSize = 10): UsePaginationReturn {
  const [state, setState] = useState<PaginationState>({
    page: 0,
    pageSize: initialPageSize,
    total: 0,
  });

  const setPage = useCallback((page: number) => {
    setState(prev => ({ ...prev, page }));
  }, []);

  const setPageSize = useCallback((pageSize: number) => {
    setState(prev => ({ ...prev, pageSize, page: 0 }));
  }, []);

  const setTotal = useCallback((total: number) => {
    setState(prev => ({ ...prev, total }));
  }, []);

  const reset = useCallback(() => {
    setState(prev => ({ ...prev, page: 0 }));
  }, []);

  return {
    ...state,
    setPage,
    setPageSize,
    setTotal,
    reset,
    pageCount: Math.ceil(state.total / state.pageSize),
  };
}
