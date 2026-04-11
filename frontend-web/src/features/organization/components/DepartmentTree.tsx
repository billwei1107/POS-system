import { Box, Typography, Button } from '@mui/material';
import type { DepartmentTreeDTO } from '../types';

interface DepartmentTreeProps {
    data: DepartmentTreeDTO[];
    onSelect: (id: string) => void;
}

export const DepartmentTree = ({ data, onSelect }: DepartmentTreeProps) => {

    const renderTree = (nodes: DepartmentTreeDTO[]) => {
        return nodes.map(node => (
            <Box key={node.id} sx={{ ml: 2, mt: 1 }}>
                <Button
                    variant="text"
                    onClick={() => onSelect(node.id)}
                    sx={{ textTransform: 'none', textAlign: 'left', display: 'block', width: '100%' }}
                >
                    {node.name} ({node.code})
                </Button>
                {node.children && node.children.length > 0 && renderTree(node.children)}
            </Box>
        ));
    };

    return (
        <Box sx={{ p: 2, borderRight: '1px solid #eee', minHeight: '100vh', backgroundColor: '#f9fafb' }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 2 }}>組織結構</Typography>
            {data && data.length > 0 ? renderTree(data) : <Typography color="textSecondary">無組織資料</Typography>}
        </Box>
    );
};
