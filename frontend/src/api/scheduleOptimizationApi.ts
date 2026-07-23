import { axiosClient } from './axiosClient';

export interface ClassRequirement {
  maLopHp: string;
  tenMon: string;
  maGv?: string;
  tenGiangVien?: string;
  soTiet?: number;
  danhSachTuan?: number[];
}

export interface ScheduledSlot {
  maLopHp: string;
  tenMon: string;
  maGv?: string;
  tenGiangVien?: string;
  phongHoc: string;
  thuTrongTuan: number;
  tietBatDau: number;
  tietKetThuc: number;
  danhSachTuan?: number[];
}

export interface ScheduleOptimizationRequest {
  classesToSchedule?: ClassRequirement[];
  availableRooms?: string[];
  availableDays?: number[];
  startPeriods?: number[];
}

export interface ScheduleOptimizationResponse {
  status: string;
  solverEngine: string;
  solveTimeSeconds: number;
  totalClassesScheduled: number;
  scheduledSlots: ScheduledSlot[];
  message: string;
}

export const scheduleOptimizationApi = {
  getSampleClasses: async (): Promise<ClassRequirement[]> => {
    const response = await axiosClient.get<ClassRequirement[]>('/schedule/sample-classes');
    return response.data;
  },

  optimizeSchedule: async (
    request: ScheduleOptimizationRequest
  ): Promise<ScheduleOptimizationResponse> => {
    const response = await axiosClient.post<ScheduleOptimizationResponse>(
      '/schedule/optimize',
      request
    );
    return response.data;
  },
};
