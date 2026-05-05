/**
 * @file useConfirm.ts
 * @description 確認對話框狀態 Hook / Confirm dialog state hook
 */
import { useState, useCallback } from 'react';

interface ConfirmState {
  open: boolean;
  title: string;
  message: string;
  onConfirm: (() => void) | null;
}

export function useConfirm() {
  const [state, setState] = useState<ConfirmState>({
    open: false,
    title: '',
    message: '',
    onConfirm: null,
  });

  const confirm = useCallback(
    (title: string, message: string): Promise<boolean> => {
      return new Promise(resolve => {
        setState({
          open: true,
          title,
          message,
          onConfirm: () => resolve(true),
        });
      });
    },
    []
  );

  const handleClose = useCallback((confirmed: boolean) => {
    setState(prev => {
      if (confirmed && prev.onConfirm) prev.onConfirm();
      return { open: false, title: '', message: '', onConfirm: null };
    });
  }, []);

  return { confirmState: state, confirm, handleClose };
}
