import { axiosClient } from './axiosClient';
import type { SalaryStatsResponse } from '../types/dashboard.types';

export const dashboardApi = {
  /**
   * Gọi API GET /api/v1/dashboard/salary-stats để lấy thống kê chi phí lương cho Admin Dashboard
   * @param thang (Tuỳ chọn) Tháng cần thống kê
   * @param nam (Tuỳ chọn) Năm cần thống kê
   */
  getSalaryStats: async (thang?: number, nam?: number): Promise<SalaryStatsResponse> => {
    const params: Record<string, number> = {};
    if (thang !== undefined) params.thang = thang;
    if (nam !== undefined) params.nam = nam;
    const response = await axiosClient.get<SalaryStatsResponse>('/dashboard/salary-stats', { params });
    return response.data;
  },
};
