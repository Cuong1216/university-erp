import React, { useEffect } from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import { useDashboardStore } from '../../store/useDashboardStore';
import { ForecastChart } from './ForecastChart';

const COLORS = [

  '#3b82f6', // blue
  '#10b981', // emerald
  '#f59e0b', // amber
  '#ef4444', // red
  '#8b5cf6', // violet
  '#ec4899', // pink
  '#06b6d4', // cyan
  '#14b8a6', // teal
  '#f97316', // orange
  '#6366f1', // indigo
];

// Custom Tooltip cho Line Chart
const CustomLineTooltip: React.FC<any> = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          padding: '12px 16px',
          borderRadius: '8px',
          boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
          border: '1px solid #e2e8f0',
          backdropFilter: 'blur(8px)',
        }}
      >
        <p style={{ margin: '0 0 6px 0', fontWeight: 700, color: '#0f172a', fontSize: '14px' }}>
          {label} (Tháng {data.thang}/{data.nam})
        </p>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#3b82f6' }} />
          <span style={{ color: '#475569', fontSize: '13px' }}>Tổng chi phí lương:</span>
          <strong style={{ color: '#1e293b', fontSize: '14px' }}>
            {Number(data.totalSalary || 0).toLocaleString('vi-VN')} VNĐ
          </strong>
        </div>
      </div>
    );
  }
  return null;
};

// Custom Tooltip cho Pie Chart
const CustomPieTooltip: React.FC<any> = ({ active, payload }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          padding: '12px 16px',
          borderRadius: '8px',
          boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
          border: '1px solid #e2e8f0',
          backdropFilter: 'blur(8px)',
          maxWidth: '280px',
        }}
      >
        <p style={{ margin: '0 0 6px 0', fontWeight: 700, color: '#0f172a', fontSize: '14px' }}>
          {data.tenKhoaHoacBoMon}
        </p>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px', fontSize: '13px' }}>
          <span style={{ color: '#64748b' }}>Chi phí:</span>
          <strong style={{ color: '#1e293b' }}>
            {Number(data.totalSalary || 0).toLocaleString('vi-VN')} VNĐ
          </strong>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
          <span style={{ color: '#64748b' }}>Tỷ lệ:</span>
          <strong style={{ color: '#3b82f6' }}>{data.percentage}%</strong>
        </div>
      </div>
    );
  }
  return null;
};

export const AdminDashboard: React.FC = () => {
  const {
    loading,
    error,
    stats,
    selectedMonth,
    selectedYear,
    activeTab,
    setSelectedPeriod,
    setActiveTab,
    fetchSalaryStats,
  } = useDashboardStore();

  useEffect(() => {
    fetchSalaryStats();
  }, [fetchSalaryStats]);

  const handleRefresh = () => {
    fetchSalaryStats(selectedMonth, selectedYear);
  };

  const handleMonthChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const m = e.target.value ? Number(e.target.value) : undefined;
    setSelectedPeriod(m, selectedYear);
  };

  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const y = e.target.value ? Number(e.target.value) : undefined;
    setSelectedPeriod(selectedMonth, y);
  };

  if (loading && !stats) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh', flexDirection: 'column', gap: 12 }}>
        <div
          style={{
            width: 40,
            height: 40,
            border: '4px solid #e2e8f0',
            borderTopColor: '#3b82f6',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
          }}
        />
        <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}</style>
        <p style={{ color: '#64748b', fontWeight: 500 }}>Đang tải tổng quan chi phí lương...</p>
      </div>
    );
  }

  if (error && !stats) {
    return (
      <div style={{ padding: 24, backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: 12, color: '#991b1b' }}>
        <h3 style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 8 }}>
          <span>⚠️</span> Lỗi truy xuất dữ liệu Dashboard
        </h3>
        <p>{error}</p>
        <button
          onClick={handleRefresh}
          style={{
            marginTop: 12,
            padding: '8px 16px',
            backgroundColor: '#ef4444',
            color: '#fff',
            border: 'none',
            borderRadius: 6,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          Thử lại
        </button>
      </div>
    );
  }

  const distributionData = activeTab === 'KHOA' ? stats?.departmentDistributions || [] : stats?.boMonDistributions || [];

  // Tìm đơn vị chi phí cao nhất để hiển thị KPI
  const topUnit = distributionData.length > 0 ? distributionData[0] : null;

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      {/* Header & Filter */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16, marginBottom: 28 }}>
        <div>
          <h1 style={{ margin: 0, color: '#0f172a', fontSize: 26, fontWeight: 800, letterSpacing: '-0.02em' }}>
            Dashboard Tổng Quan Quản Trị ERP
          </h1>
          <p style={{ margin: '6px 0 0 0', color: '#64748b', fontSize: 14 }}>
            Thống kê biến động chi phí lương giảng viên và phân bổ ngân sách theo đơn vị.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, backgroundColor: '#ffffff', padding: '8px 14px', borderRadius: 10, border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
          <span style={{ fontSize: 13, fontWeight: 600, color: '#475569' }}>Kỳ thống kê:</span>
          <select
            value={selectedMonth || ''}
            onChange={handleMonthChange}
            style={{ padding: '6px 10px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 13, color: '#1e293b', fontWeight: 500, outline: 'none' }}
          >
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((m) => (
              <option key={m} value={m}>
                Tháng {m}
              </option>
            ))}
          </select>

          <select
            value={selectedYear || ''}
            onChange={handleYearChange}
            style={{ padding: '6px 10px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 13, color: '#1e293b', fontWeight: 500, outline: 'none' }}
          >
            {[2023, 2024, 2025, 2026, 2027].map((y) => (
              <option key={y} value={y}>
                Năm {y}
              </option>
            ))}
          </select>

          <button
            onClick={handleRefresh}
            title="Làm mới dữ liệu"
            style={{
              padding: '6px 12px',
              backgroundColor: '#3b82f6',
              color: '#ffffff',
              border: 'none',
              borderRadius: 6,
              fontSize: 13,
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              transition: 'background-color 0.2s',
            }}
          >
            🔄 Làm mới
          </button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 20, marginBottom: 32 }}>
        {/* Card 1: Tổng Chi Phí Lương */}
        <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)', position: 'relative', overflow: 'hidden' }}>
          <div style={{ position: 'absolute', top: 0, right: 0, width: 80, height: 80, background: 'radial-gradient(circle, rgba(59,130,246,0.1) 0%, rgba(255,255,255,0) 70%)' }} />
          <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Tổng chi phí lương (T{stats?.currentMonth}/{stats?.currentYear})
          </p>
          <h2 style={{ margin: '8px 0 4px 0', fontSize: 24, fontWeight: 800, color: '#0f172a' }}>
            {Number(stats?.totalSalaryCurrentMonth || 0).toLocaleString('vi-VN')} VNĐ
          </h2>
          <span style={{ fontSize: 12, color: '#64748b' }}>Dữ liệu chốt trên bảng lương chính thức</span>
        </div>

        {/* Card 2: Biến động so với tháng trước */}
        <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)' }}>
          <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Biến động so với tháng trước
          </p>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, margin: '8px 0 4px 0' }}>
            <h2
              style={{
                margin: 0,
                fontSize: 24,
                fontWeight: 800,
                color: (stats?.monthlyGrowthRate || 0) >= 0 ? '#10b981' : '#ef4444',
              }}
            >
              {(stats?.monthlyGrowthRate || 0) >= 0 ? '+' : ''}
              {stats?.monthlyGrowthRate || 0}%
            </h2>
            <span style={{ fontSize: 13, fontWeight: 600, color: (stats?.monthlyGrowthRate || 0) >= 0 ? '#10b981' : '#ef4444' }}>
              {(stats?.monthlyGrowthRate || 0) >= 0 ? '↗ Tăng' : '↘ Giảm'}
            </span>
          </div>
          <span style={{ fontSize: 12, color: '#64748b' }}>
            Tháng trước: {Number(stats?.totalSalaryPreviousMonth || 0).toLocaleString('vi-VN')} VNĐ
          </span>
        </div>

        {/* Card 3: Số giảng viên được thanh toán */}
        <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)' }}>
          <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Giảng viên được chi trả
          </p>
          <h2 style={{ margin: '8px 0 4px 0', fontSize: 24, fontWeight: 800, color: '#0f172a' }}>
            {Number(stats?.totalLecturersPaid || 0).toLocaleString('vi-VN')} <span style={{ fontSize: 16, fontWeight: 600, color: '#64748b' }}>người</span>
          </h2>
          <span style={{ fontSize: 12, color: '#64748b' }}>Đã hoàn tất quy trình chốt lương</span>
        </div>

        {/* Card 4: Đơn vị chi phí lớn nhất */}
        <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)' }}>
          <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Đơn vị chi phí lớn nhất
          </p>
          <h3 style={{ margin: '8px 0 4px 0', fontSize: 18, fontWeight: 700, color: '#3b82f6', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {topUnit ? topUnit.tenKhoaHoacBoMon : 'Chưa có dữ liệu'}
          </h3>
          <span style={{ fontSize: 12, color: '#64748b' }}>
            {topUnit ? `${Number(topUnit.totalSalary).toLocaleString('vi-VN')} VNĐ (${topUnit.percentage}%)` : '—'}
          </span>
        </div>
      </div>

      {/* Charts Grid: Line Chart + Pie Chart */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(480px, 1fr))', gap: 24 }}>
        {/* Line Chart Component */}
        <div style={{ backgroundColor: '#ffffff', padding: 24, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)', display: 'flex', flexDirection: 'column' }}>
          <div style={{ marginBottom: 20 }}>
            <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700, color: '#0f172a' }}>
              📈 Biến động chi phí lương 6 tháng gần nhất
            </h3>
            <p style={{ margin: '4px 0 0 0', fontSize: 13, color: '#64748b' }}>
              Theo dõi xu hướng chi thù lao giảng dạy toàn trường theo thời gian
            </p>
          </div>

          <div style={{ width: '100%', height: 340 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={stats?.monthlyTrends || []} margin={{ top: 10, right: 30, left: 20, bottom: 10 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis
                  dataKey="period"
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: '#64748b', fontSize: 12, fontWeight: 600 }}
                  dy={8}
                />
                <YAxis
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: '#64748b', fontSize: 12 }}
                  tickFormatter={(val) => {
                    if (val >= 1000000000) return `${(val / 1000000000).toFixed(1)}B`;
                    if (val >= 1000000) return `${(val / 1000000).toFixed(0)}M`;
                    return val.toLocaleString('vi-VN');
                  }}
                  dx={-8}
                />
                <Tooltip content={<CustomLineTooltip />} />
                <Legend verticalAlign="top" height={36} wrapperStyle={{ fontSize: '13px', fontWeight: 600 }} />
                <Line
                  type="monotone"
                  dataKey="totalSalary"
                  name="Chi phí lương (VNĐ)"
                  stroke="#3b82f6"
                  strokeWidth={3.5}
                  dot={{ r: 6, fill: '#3b82f6', strokeWidth: 2, stroke: '#ffffff' }}
                  activeDot={{ r: 8, strokeWidth: 0, fill: '#1d4ed8' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Pie Chart Component */}
        <div style={{ backgroundColor: '#ffffff', padding: 24, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12, marginBottom: 20 }}>
            <div>
              <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700, color: '#0f172a' }}>
                🥧 Phân bổ chi phí lương (T{stats?.currentMonth}/{stats?.currentYear})
              </h3>
              <p style={{ margin: '4px 0 0 0', fontSize: 13, color: '#64748b' }}>
                Tỷ trọng chi trả theo đơn vị quản lý
              </p>
            </div>

            {/* Switch Tab Theo Khoa / Theo Bộ môn */}
            <div style={{ display: 'flex', backgroundColor: '#f1f5f9', padding: 3, borderRadius: 8, border: '1px solid #e2e8f0' }}>
              <button
                onClick={() => setActiveTab('KHOA')}
                style={{
                  padding: '6px 14px',
                  borderRadius: 6,
                  border: 'none',
                  fontSize: 12,
                  fontWeight: 600,
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'KHOA' ? '#ffffff' : 'transparent',
                  color: activeTab === 'KHOA' ? '#0f172a' : '#64748b',
                  boxShadow: activeTab === 'KHOA' ? '0 1px 2px rgba(0,0,0,0.08)' : 'none',
                  transition: 'all 0.2s',
                }}
              >
                Theo Khoa
              </button>
              <button
                onClick={() => setActiveTab('BOMON')}
                style={{
                  padding: '6px 14px',
                  borderRadius: 6,
                  border: 'none',
                  fontSize: 12,
                  fontWeight: 600,
                  cursor: 'pointer',
                  backgroundColor: activeTab === 'BOMON' ? '#ffffff' : 'transparent',
                  color: activeTab === 'BOMON' ? '#0f172a' : '#64748b',
                  boxShadow: activeTab === 'BOMON' ? '0 1px 2px rgba(0,0,0,0.08)' : 'none',
                  transition: 'all 0.2s',
                }}
              >
                Theo Bộ Môn
              </button>
            </div>
          </div>

          <div style={{ width: '100%', height: 340 }}>
            {distributionData && distributionData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Tooltip content={<CustomPieTooltip />} />
                  <Pie
                    data={distributionData}
                    dataKey="totalSalary"
                    nameKey="tenKhoaHoacBoMon"
                    cx="50%"
                    cy="46%"
                    innerRadius={75}
                    outerRadius={115}
                    paddingAngle={3}
                  >
                    {distributionData.map((_entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Legend
                    verticalAlign="bottom"
                    layout="horizontal"
                    align="center"
                    wrapperStyle={{ fontSize: '12px', paddingTop: '16px' }}
                    formatter={(value: string) => (
                      <span style={{ color: '#334155', fontWeight: 500, marginRight: '12px' }}>{value}</span>
                    )}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%', color: '#94a3b8', fontSize: 14 }}>
                Chưa có dữ liệu phân bổ cho tháng {stats?.currentMonth}/{stats?.currentYear}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* C2: AI Predictive Salary Analytics (Time Series Forecast Chart) */}
      <ForecastChart />
    </div>
  );
};

