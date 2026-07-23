import React, { useEffect, useState } from 'react';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import { analyticsApi } from '../../api/analyticsApi';
import type { SalaryForecastResponse } from '../../api/analyticsApi';

const CustomForecastTooltip: React.FC<any> = ({ active, payload, label }) => {
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
        }}
      >
        <p style={{ margin: '0 0 8px 0', fontWeight: 700, color: '#0f172a', fontSize: '14px' }}>
          📅 Kỳ chi phí: {label}
        </p>
        {data.actual !== undefined && data.actual !== null && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#3b82f6' }} />
            <span style={{ color: '#475569', fontSize: '13px' }}>Thực tế đã chi:</span>
            <strong style={{ color: '#1e293b', fontSize: '14px' }}>
              {Number(data.actual || 0).toLocaleString('vi-VN')} VNĐ
            </strong>
          </div>
        )}
        {data.forecast !== undefined && data.forecast !== null && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#f59e0b' }} />
            <span style={{ color: '#475569', fontSize: '13px' }}>Dự báo AI (yhat):</span>
            <strong style={{ color: '#d97706', fontSize: '14px' }}>
              {Number(data.forecast || 0).toLocaleString('vi-VN')} VNĐ
            </strong>
          </div>
        )}
        {data.lower !== undefined && data.upper !== null && (
          <div style={{ fontSize: '12px', color: '#64748b', marginTop: '4px', borderTop: '1px dashed #e2e8f0', paddingTop: '4px' }}>
            Dải tin cậy 95%: {Number(data.lower || 0).toLocaleString('vi-VN')} — {Number(data.upper || 0).toLocaleString('vi-VN')} VNĐ
          </div>
        )}
      </div>
    );
  }
  return null;
};

export const ForecastChart: React.FC = () => {
  const [data, setData] = useState<SalaryForecastResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchForecast = async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await analyticsApi.getSalaryForecast(6);
      setData(resp);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Không thể tải dữ liệu dự báo chi phí từ AI Service.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchForecast();
  }, []);

  if (loading) {
    return (
      <div style={{ backgroundColor: '#ffffff', padding: 24, borderRadius: 12, border: '1px solid #e2e8f0', display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
          <div
            style={{
              width: 32,
              height: 32,
              border: '3px solid #e2e8f0',
              borderTopColor: '#f59e0b',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
            }}
          />
          <span style={{ color: '#64748b', fontSize: 13, fontWeight: 500 }}>AI đang mô hình hóa dữ liệu chuỗi thời gian...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ backgroundColor: '#ffffff', padding: 24, borderRadius: 12, border: '1px solid #fecaca', color: '#991b1b' }}>
        <h4 style={{ margin: '0 0 6px 0', display: 'flex', alignItems: 'center', gap: 8 }}>
          <span>⚡</span> Lỗi AI Forecaster
        </h4>
        <p style={{ margin: 0, fontSize: 13 }}>{error}</p>
        <button
          onClick={fetchForecast}
          style={{ marginTop: 12, padding: '6px 14px', backgroundColor: '#ef4444', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 12, fontWeight: 600 }}
        >
          Thử lại
        </button>
      </div>
    );
  }

  // Kết hợp dữ liệu lịch sử và dữ liệu dự báo thành 1 list time-series
  const chartData: Array<{
    period: string;
    actual?: number;
    forecast?: number;
    lower?: number;
    upper?: number;
    range?: [number, number];
  }> = [];

  const history = data?.historicalActuals || [];
  const forecasts = data?.forecastPoints || [];

  history.forEach((h) => {
    chartData.push({
      period: `${h.month}/${h.year}`,
      actual: Number(h.amount),
    });
  });

  // Nếu có lịch sử, điểm cuối cùng của lịch sử cũng là điểm bắt đầu nối của đường dự báo
  if (history.length > 0 && forecasts.length > 0) {
    const lastH = history[history.length - 1];
    const lastItem = chartData[chartData.length - 1];

    if (lastItem) {
      lastItem.forecast = Number(lastH.amount);
      lastItem.lower = Number(lastH.amount);
      lastItem.upper = Number(lastH.amount);
      lastItem.range = [Number(lastH.amount), Number(lastH.amount)];
    }
  }

  forecasts.forEach((f) => {
    const parts = f.ds.split('-'); // YYYY-MM-DD
    const m = parseInt(parts[1], 10);
    const y = parseInt(parts[0], 10);
    const yhat = Number(f.yhat);
    const lower = Number(f.yhatLower);
    const upper = Number(f.yhatUpper);

    chartData.push({
      period: `${m}/${y}`,
      forecast: yhat,
      lower: lower,
      upper: upper,
      range: [lower, upper],
    });
  });

  return (
    <div style={{ backgroundColor: '#ffffff', padding: 24, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)', marginTop: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16, marginBottom: 20 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700, color: '#0f172a' }}>
              🔮 Dự báo Chi phí Lương 6 Tháng Tới (AI Predictive Analytics)
            </h3>
            <span
              style={{
                backgroundColor: '#fef3c7',
                color: '#d97706',
                border: '1px solid #fde68a',
                padding: '4px 10px',
                borderRadius: 20,
                fontSize: 12,
                fontWeight: 600,
              }}
            >
              Engine: {data?.modelUsed || 'Prophet Hybrid'}
            </span>
          </div>
          <p style={{ margin: '4px 0 0 0', fontSize: 13, color: '#64748b' }}>
            Mô hình chuỗi thời gian phân tích tính chu kỳ học kỳ và xu hướng quỹ thù lao giảng viên toàn trường.
          </p>
        </div>

        <button
          onClick={fetchForecast}
          style={{
            padding: '6px 12px',
            backgroundColor: '#f1f5f9',
            color: '#334155',
            border: '1px solid #cbd5e1',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
          }}
        >
          🔄 Cập nhật mô hình
        </button>
      </div>

      <div style={{ width: '100%', height: 360 }}>
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={chartData} margin={{ top: 10, right: 30, left: 20, bottom: 10 }}>
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
            <Tooltip content={<CustomForecastTooltip />} />
            <Legend verticalAlign="top" height={36} wrapperStyle={{ fontSize: '13px', fontWeight: 600 }} />

            {/* Vùng dải tin cậy 95% cho Dự báo (Area range) */}
            <Area
              type="monotone"
              dataKey="range"
              name="Dải tin cậy 95% (Dự báo)"
              fill="#fef3c7"
              stroke="none"
              fillOpacity={0.7}
            />

            {/* Đường thực tế (Solid Blue Line) */}
            <Line
              type="monotone"
              dataKey="actual"
              name="Chi phí Thực tế (Đã chốt)"
              stroke="#3b82f6"
              strokeWidth={3}
              dot={{ r: 5, fill: '#3b82f6', strokeWidth: 2, stroke: '#ffffff' }}
              activeDot={{ r: 7 }}
            />

            {/* Đường dự báo (Dashed Amber Line) */}
            <Line
              type="monotone"
              dataKey="forecast"
              name="Dự báo AI (yhat)"
              stroke="#f59e0b"
              strokeWidth={3}
              strokeDasharray="5 5"
              dot={{ r: 5, fill: '#f59e0b', strokeWidth: 2, stroke: '#ffffff' }}
              activeDot={{ r: 7 }}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
