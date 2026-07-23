import { axiosClient } from './axiosClient';

export interface HistoricalPoint {
  ds: string;
  month: number;
  year: number;
  amount: number;
}

export interface ForecastPoint {
  ds: string;
  yhat: number;
  yhatLower: number;
  yhatUpper: number;
}

export interface SalaryForecastResponse {
  modelUsed: string;
  historicalActuals: HistoricalPoint[];
  forecastPoints: ForecastPoint[];
}

export const analyticsApi = {
  getSalaryForecast: async (periods = 6): Promise<SalaryForecastResponse> => {
    const response = await axiosClient.get<SalaryForecastResponse>('/analytics/salary-forecast', {
      params: { periods },
    });
    return response.data;
  },
};
