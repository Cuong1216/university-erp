export interface MonthlySalaryTrend {
  thang: number;
  nam: number;
  period: string;
  totalSalary: number;
}

export interface DepartmentSalary {
  maKhoaHoacBoMon: string;
  tenKhoaHoacBoMon: string;
  totalSalary: number;
  percentage: number;
}

export interface SalaryStatsResponse {
  monthlyTrends: MonthlySalaryTrend[];
  departmentDistributions: DepartmentSalary[];
  boMonDistributions: DepartmentSalary[];
  currentMonth: number;
  currentYear: number;
  totalSalaryCurrentMonth: number;
  totalSalaryPreviousMonth: number;
  monthlyGrowthRate: number;
  totalLecturersPaid: number;
}
