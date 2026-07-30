import { create } from 'zustand';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

interface NotificationStore {
  notifications: Notification[];
  showSuccess: (msg: string) => void;
  showError: (msg: string) => void;
  showWarning: (msg: string) => void;
  showInfo: (msg: string) => void;
  dismiss: (id: string) => void;
}

const generateId = () => Math.random().toString(36).substring(2, 9);

export const useNotificationStore = create<NotificationStore>((set, get) => ({
  notifications: [],
  
  showSuccess: (message) => {
    const id = generateId();
    set((state) => ({ notifications: [...state.notifications, { id, type: 'success', message, duration: 3000 }] }));
    setTimeout(() => get().dismiss(id), 3000);
  },
  
  showError: (message) => {
    const id = generateId();
    set((state) => ({ notifications: [...state.notifications, { id, type: 'error', message, duration: 5000 }] }));
    setTimeout(() => get().dismiss(id), 5000);
  },
  
  showWarning: (message) => {
    const id = generateId();
    set((state) => ({ notifications: [...state.notifications, { id, type: 'warning', message }] }));
  },

  showInfo: (message) => {
    const id = generateId();
    set((state) => ({ notifications: [...state.notifications, { id, type: 'info', message, duration: 4000 }] }));
    setTimeout(() => get().dismiss(id), 4000);
  },
  
  dismiss: (id) => {
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    }));
  },
}));
