import React, { useState } from 'react';
import { axiosClient } from '../api/axiosClient';

export const AcademicSalaryPage: React.FC = () => {
  const [thang, setThang] = useState<number>(new Date().getMonth() + 1);
  const [nam, setNam] = useState<number>(new Date().getFullYear());
  const [loading, setLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleExportExcel = async () => {
    let objectUrl: string | null = null;
    try {
      setLoading(true);
      setErrorMessage(null);
      const response = await axiosClient.get('/luong/export/excel', {
        params: { thang, nam },
        responseType: 'blob',
      });
      
      objectUrl = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = objectUrl;
      link.setAttribute('download', `bang_luong_thang_${String(thang).padStart(2, '0')}_${nam}.xlsx`);
      document.body.appendChild(link);
      link.click();
      if (link.parentNode) {
        link.parentNode.removeChild(link);
      }
    } catch (error) {
      setErrorMessage('Chưa có dữ liệu bảng lương hoặc có lỗi khi tải Excel.');
    } finally {
      setLoading(false);
      if (objectUrl) {
        window.URL.revokeObjectURL(objectUrl);
      }
    }
  };

  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Quản Lý Thù Lao & Xuất Bảng Lương Excel</h1>
      <div style={{ padding: 16, backgroundColor: '#fef3c7', color: '#92400e', borderRadius: 8, border: '1px solid #fde68a', marginBottom: 20 }}>
        <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền <code>ROLE_GIAO_VU</code> hoặc <code>ROLE_ADMIN</code>.
      </div>

      {errorMessage && (
        <div style={{ padding: '12px 16px', backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: 8, border: '1px solid #fecaca', marginBottom: 20 }}>
          ⚠️ {errorMessage}
        </div>
      )}

      <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0' }}>
        <h3 style={{ marginTop: 0, color: '#1e293b' }}>Xuất Báo Cáo Bảng Lương & Thù Lao Giảng Dạy</h3>
        <p style={{ color: '#64748b' }}>
          Giáo vụ và Quản trị viên có thể chốt lương giảng viên theo từng tháng và xuất danh sách bảng lương tổng hợp ra file Excel.
        </p>

        <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginTop: 16, flexWrap: 'wrap' }}>
          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: '#334155', display: 'block', marginBottom: 4 }}>Tháng</label>
            <select
              value={thang}
              onChange={(e) => setThang(Number(e.target.value))}
              style={{ padding: '8px 12px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 14 }}
            >
              {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                <option key={m} value={m}>Tháng {m}</option>
              ))}
            </select>
          </div>

          <div>
            <label style={{ fontSize: 13, fontWeight: 600, color: '#334155', display: 'block', marginBottom: 4 }}>Năm</label>
            <input
              type="number"
              value={nam}
              onChange={(e) => setNam(Number(e.target.value))}
              style={{ padding: '8px 12px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 14, width: 100 }}
            />
          </div>

          <div style={{ alignSelf: 'flex-end' }}>
            <button
              onClick={handleExportExcel}
              disabled={loading}
              style={{
                padding: '9px 16px',
                backgroundColor: '#16a34a',
                color: '#ffffff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 600,
                cursor: loading ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: 8,
              }}
            >
              {loading ? 'Đang xuất Excel...' : '📊 Xuất file Excel'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
