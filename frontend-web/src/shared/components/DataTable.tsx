/**
 * @file DataTable.tsx
 * @description 通用資料表格組件 / Generic data table component
 */
import {
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TablePagination, Paper,
  CircularProgress, Box, Typography,
} from '@mui/material';

export interface Column<T> {
  key: keyof T | string;
  label: string;
  width?: number | string;
  align?: 'left' | 'center' | 'right';
  render?: (row: T) => React.ReactNode;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  loading?: boolean;
  total?: number;
  page?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  emptyMessage?: string;
  rowKey?: (row: T) => string | number;
}

export function DataTable<T extends Record<string, unknown>>({
  columns,
  rows,
  loading = false,
  total = 0,
  page = 0,
  pageSize = 10,
  onPageChange,
  onPageSizeChange,
  emptyMessage = '目前無資料',
  rowKey,
}: DataTableProps<T>) {
  const getCell = (row: T, col: Column<T>) => {
    if (col.render) return col.render(row);
    const value = row[col.key as keyof T];
    return value !== undefined && value !== null ? String(value) : '-';
  };

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <TableContainer>
        <Table stickyHeader size="small">
          <TableHead>
            <TableRow>
              {columns.map(col => (
                <TableCell
                  key={String(col.key)}
                  align={col.align ?? 'left'}
                  sx={{ width: col.width, fontWeight: 700, whiteSpace: 'nowrap' }}
                >
                  {col.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 6 }}>
                  <CircularProgress size={32} />
                </TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 6 }}>
                  <Typography color="text.secondary">{emptyMessage}</Typography>
                </TableCell>
              </TableRow>
            ) : (
              rows.map((row, idx) => (
                <TableRow
                  key={rowKey ? rowKey(row) : idx}
                  hover
                  sx={{ '&:last-child td': { border: 0 } }}
                >
                  {columns.map(col => (
                    <TableCell key={String(col.key)} align={col.align ?? 'left'}>
                      {getCell(row, col)}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      {onPageChange && (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <TablePagination
            component="div"
            count={total}
            page={page}
            rowsPerPage={pageSize}
            onPageChange={(_, p) => onPageChange(p)}
            onRowsPerPageChange={e => onPageSizeChange?.(parseInt(e.target.value, 10))}
            rowsPerPageOptions={[10, 25, 50]}
            labelRowsPerPage="每頁筆數"
          />
        </Box>
      )}
    </Paper>
  );
}
