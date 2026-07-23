import React, { useState, useCallback } from 'react';
import { axiosClient } from '../api/axiosClient';
import { useWebSocket, type WebSocketNotification } from '../hooks/useWebSocket';

export const AcademicSalaryPage: React.FC = () => {
  const [thang, setThang] = useState<number>(new Date().getMonth() + 1);
  const [nam, setNam] = useState<number>(new Date().getFullYear());
  const [maGvInput, setMaGvInput] = useState<string>('');

  const [isProcessingPayroll, setIsProcessingPayroll] = useState<boolean>(false);
  const [exportLoading, setExportLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [notifications, setNotifications] = useState<WebSocketNotification[]>([]);

  const handleNotification = useCallback((notification: WebSocketNotification) => {
    console.log('🔔 Nhận thông báo mới:', notification);
    setNotifications((prev) => [notification, ...prev]);

    if (notification.type === 'CHOT_LUONG_SUCCESS') {
      setIsProcessingPayroll(false);
      alert(`✅ ${notification.message}`);
    } else if (notification.type === 'CHOT_LUONG_ERROR') {
      setIsProcessingPayroll(false);
      alert(`❌ ${notification.message}`);
    }
  }, []);

  useWebSocket({ onNotificationReceived: handleNotification });

  const handleChotLuongAsync = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!maGvInput.trim()) {
      alert('Vui lòng nhập mã giảng viên!');
      return;
    }

    try {
      setIsProcessingPayroll(true);
      setErrorMessage(null);
      const response = await axiosClient.post('/luong/chot-luong', {
        maGv: maGvInput.trim(),
        thang,
        nam,
      });

      alert(`⏳ ${response.data.message}\nBạn có thể tiếp tục làm việc khác, hệ thống sẽ gửi thông báo Real-time khi hoàn tất!`);
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const err = error as any;
      setIsProcessingPayroll(false);
      setErrorMessage(err.response?.data?.message || err.message || 'Lỗi gửi yêu cầu chốt lương');
      alert(`❌ Lỗi gửi yêu cầu: ${err.response?.data?.message || err.message}`);
    }
  };

  const handleExportExcel = async () => {
    let objectUrl: string | null = null;
    try {
      setExportLoading(true);
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
    } catch {
      setErrorMessage('Chưa có dữ liệu bảng lương hoặc có lỗi khi tải Excel.');
    } finally {
      setExportLoading(false);
      if (objectUrl) {
        window.URL.revokeObjectURL(objectUrl);
      }
    }
  };

  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Quản Lý Thù Lao & Chốt Lương Giảng Dạy</h1>

      <div style={{ padding: 16, backgroundColor: '#fef3c7', color: '#92400e', borderRadius: 8, border: '1px solid #fde68a', marginBottom: 20 }}>
        <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền <code>ROLE_GIAO_VU</code> hoặc <code>ROLE_ADMIN</code>.
        <br />
        <small>⚡ Kết nối Real-time WebSocket: <strong>Đang hoạt động</strong> (Tự động thông báo khi chốt lương xong).</small>
      </div>

      {errorMessage && (
        <div style={{ padding: '12px 16px', backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: 8, border: '1px solid #fecaca', marginBottom: 20 }}>
          ⚠️ {errorMessage}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: 20 }}>
        <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
          <h3 style={{ marginTop: 0, color: '#1e293b', borderBottom: '1px solid #f1f5f9', paddingBottom: 10 }}>
            🚀 Chốt Lương Cuối Tháng (Asynchronous)
          </h3>
          <p style={{ color: '#64748b', fontSize: 14 }}>
            Hệ thống sẽ tổng hợp số tiết dạy chưa thanh toán và tính toán bảng lương trong nền (Background Task). Màn hình <strong>không bị block/chờ đợi</strong>.
          </p>

          <form onSubmit={handleChotLuongAsync} style={{ display: 'flex', flexDirection: 'column', gap: 14, marginTop: 16 }}>
            <div>
              <label style={{ fontSize: 13, fontWeight: 600, color: '#334155', display: 'block', marginBottom: 4 }}>Mã Giảng Viên (GV)</label>
              <input
                type="text"
                placeholder="Ví dụ: GV001"
                value={maGvInput}
                onChange={(e) => setMaGvInput(e.target.value)}
                disabled={isProcessingPayroll}
                style={{ width: '100%', padding: '9px 12px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 14, boxSizing: 'border-box' }}
                required
              />
            </div>

            <div style={{ display: 'flex', gap: 12 }}>
              <div style={{ flex: 1 }}>
                <label style={{ fontSize: 13, fontWeight: 600, color: '#334155', display: 'block', marginBottom: 4 }}>Tháng</label>
                <select
                  value={thang}
                  onChange={(e) => setThang(Number(e.target.value))}
                  disabled={isProcessingPayroll}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 14 }}
                >
                  {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                    <option key={m} value={m}>Tháng {m}</option>
                  ))}
                </select>
              </div>

              <div style={{ flex: 1 }}>
                <label style={{ fontSize: 13, fontWeight: 600, color: '#334155', display: 'block', marginBottom: 4 }}>Năm</label>
                <input
                  type="number"
                  value={nam}
                  onChange={(e) => setNam(Number(e.target.value))}
                  disabled={isProcessingPayroll}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 14 }}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isProcessingPayroll}
              style={{
                marginTop: 6,
                padding: '11px 16px',
                backgroundColor: isProcessingPayroll ? '#94a3b8' : '#3b82f6',
                color: '#ffffff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 600,
                cursor: isProcessingPayroll ? 'not-allowed' : 'pointer',
                transition: 'background-color 0.2s',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: 8
              }}
            >
              {isProcessingPayroll ? (
                <>
                  <span>⏳ Đang xử lý chốt lương trong nền...</span>
                </>
              ) : (
                <>⚡ Chốt Lương Ngay (Non-blocking)</>
              )}
            </button>
          </form>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
            <h3 style={{ marginTop: 0, color: '#1e293b', borderBottom: '1px solid #f1f5f9', paddingBottom: 10 }}>
              📊 Xuất Báo Cáo Excel
            </h3>
            <p style={{ color: '#64748b', fontSize: 14 }}>
              Xuất danh sách bảng lương tổng hợp ra file Excel sau khi các giảng viên đã được chốt lương.
            </p>
            <button
              onClick={handleExportExcel}
              disabled={exportLoading}
              style={{
                marginTop: 8,
                padding: '9px 16px',
                backgroundColor: '#16a34a',
                color: '#ffffff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 600,
                cursor: exportLoading ? 'not-allowed' : 'pointer',
              }}
            >
              {exportLoading ? '⏳ Đang xuất Excel...' : '📥 Xuất file Excel Bảng Lương'}
            </button>
          </div>

          <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0', flex: 1 }}>
            <h4 style={{ marginTop: 0, color: '#1e293b', marginBottom: 12 }}>🔔 Lịch Sử Thông Báo Real-time</h4>
            {notifications.length === 0 ? (
              <div style={{ fontSize: 13, color: '#94a3b8', textAlign: 'center', padding: '20px 0' }}>
                Chưa có thông báo nào từ Server.
              </div>
            ) : (
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, maxHeight: 220, overflowY: 'auto' }}>
                {notifications.map((notif, idx) => (
                  <li key={idx} style={{
                    padding: '10px 12px',
                    borderRadius: 6,
                    marginBottom: 8,
                    fontSize: 13,
                    backgroundColor: notif.status === 'SUCCESS' ? '#f0fdf4' : '#fef2f2',
                    border: `1px solid ${notif.status === 'SUCCESS' ? '#bbf7d0' : '#fecaca'}`,
                    color: notif.status === 'SUCCESS' ? '#166534' : '#991b1b',
                  }}>
                    <div style={{ fontWeight: 600 }}>{notif.message}</div>
                    <div style={{ fontSize: 11, color: '#64748b', marginTop: 4 }}>
                      {new Date(notif.timestamp).toLocaleTimeString()} - {new Date(notif.timestamp).toLocaleDateString()}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
