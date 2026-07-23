import { create } from 'zustand';
import { dashboardApi } from '../api/dashboardApi';
import type { SalaryStatsResponse } from '../types/dashboard.types';

interface DashboardState {
  loading: boolean;
  error: string | null;
  stats: SalaryStatsResponse | null;
  selectedMonth: number | undefined;
  selectedYear: number | undefined;
  activeTab: 'KHOA' | 'BOMON';
  setSelectedPeriod: (month?: number, year?: number) => void;
  setActiveTab: (tab: 'KHOA' | 'BOMON') => void;
  fetchSalaryStats: (thang?: number, nam?: number) => Promise<void>;
}

export const useDashboardStore = create<DashboardState>((set, get) => ({
  loading: false,
  error: null,
  stats: null,
  selectedMonth: undefined,
  selectedYear: undefined,
  activeTab: 'KHOA',

  setSelectedPeriod: (month, year) => {
    set({ selectedMonth: month, selectedYear: year });
    get().fetchSalaryStats(month, year);
  },

  setActiveTab: (tab) => set({ activeTab: tab }),

  fetchSalaryStats: async (thang, nam) => {
    set({ loading: true, error: null });
    try {
      const data = await dashboardApi.getSalaryStats(thang, nam);
      set({
        stats: data,
        loading: false,
        selectedMonth: data.currentMonth,
        selectedYear: data.currentYear,
      });
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const err = error as any;
      const msg = err?.response?.data?.message || err?.message || 'Không thể tải dữ liệu thống kê Dashboard';
      set({ error: msg, loading: false });
    }
  },
}));
