import React, { useEffect, useState } from 'react';
import { paymentApi } from '../api/paymentApi';
import type { TuitionResponse } from '../api/paymentApi';
import { useAuthStore } from '../store/useAuthStore';

export const TuitionPage: React.FC = () => {
  const { roles } = useAuthStore();
  const [tuitions, setTuitions] = useState<TuitionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingId, setProcessingId] = useState<string | null>(null);

  const isAdminOrGiaoVu = roles.some((r) => ['ROLE_ADMIN', 'ROLE_GIAO_VU'].includes(r));

  const fetchTuitions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = isAdminOrGiaoVu ? await paymentApi.getAllTuitions() : await paymentApi.getMyTuitions();
      setTuitions(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Lỗi kết nối tới hệ thống tài chính học phí');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTuitions();
  }, [isAdminOrGiaoVu]);

  const handlePay = async (item: TuitionResponse) => {
    setProcessingId(item.maHocPhi);
    try {
      const resp = await paymentApi.createPaymentUrl({
        maHocPhi: item.maHocPhi,
        amount: item.soTienConLai,
        returnUrl: window.location.origin + '/tuition/result',
      });
      if (resp && resp.paymentUrl) {
        window.location.href = resp.paymentUrl;
      } else {
        alert('Không nhận được URL thanh toán VNPay.');
        setProcessingId(null);
      }
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Lỗi khởi tạo cổng thanh toán VNPay.');
      setProcessingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DA_NOP_DU':
        return <span style={{ backgroundColor: '#dcfce7', color: '#166534', padding: '4px 12px', borderRadius: 20, fontWeight: 700, fontSize: 12 }}>✓ Đã Nộp Đủ</span>;
      case 'NOP_MOT_PHAN':
        return <span style={{ backgroundColor: '#fef9c3', color: '#854d0e', padding: '4px 12px', borderRadius: 20, fontWeight: 700, fontSize: 12 }}>⏳ Nộp Một Phần</span>;
      default:
        return <span style={{ backgroundColor: '#fee2e2', color: '#991b1b', padding: '4px 12px', borderRadius: 20, fontWeight: 700, fontSize: 12 }}>⚠️ Chưa Nộp</span>;
    }
  };

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ margin: 0, color: '#0f172a', fontSize: 26, fontWeight: 800 }}>
            💳 Cổng Tra Cứu & Thanh Toán Học Phí Trực Tuyến
          </h1>
          <p style={{ margin: '6px 0 0 0', color: '#64748b', fontSize: 14 }}>
            Tích hợp cổng thanh toán VNPay với cơ chế phòng thủ Webhook Idempotency & chữ ký bảo mật HMAC SHA512.
          </p>
        </div>
        <button
          onClick={fetchTuitions}
          style={{ padding: '8px 16px', backgroundColor: '#3b82f6', color: '#fff', border: 'none', borderRadius: 8, fontWeight: 600, cursor: 'pointer' }}
        >
          🔄 Làm mới dữ liệu
        </button>
      </div>

      {loading ? (
        <div style={{ padding: 60, textAlign: 'center', color: '#64748b', fontSize: 15 }}>Đang tải danh sách học phí...</div>
      ) : error ? (
        <div style={{ padding: 24, backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: 12, color: '#991b1b' }}>
          <h4>⚠️ Lỗi tải dữ liệu học phí</h4>
          <p>{error}</p>
        </div>
      ) : tuitions.length === 0 ? (
        <div style={{ padding: 48, textAlign: 'center', backgroundColor: '#fff', borderRadius: 12, border: '1px dashed #cbd5e1', color: '#64748b' }}>
          Chưa có thông báo nộp học phí nào trong học kỳ này.
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: 20 }}>
          {tuitions.map((t) => (
            <div key={t.maHocPhi} style={{ backgroundColor: '#fff', padding: 22, borderRadius: 12, border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                  <span style={{ fontSize: 13, fontWeight: 700, color: '#3b82f6', backgroundColor: '#eff6ff', padding: '3px 10px', borderRadius: 6 }}>{t.maHocPhi}</span>
                  {getStatusBadge(t.trangThai)}
                </div>
                <h3 style={{ margin: '0 0 4px 0', fontSize: 18, color: '#0f172a' }}>Học Kỳ {t.hocKy} - Năm học {t.namHoc}</h3>
                <p style={{ margin: '0 0 16px 0', fontSize: 13, color: '#64748b' }}>Mã SV: <strong style={{ color: '#1e293b' }}>{t.maSv}</strong></p>

                <div style={{ backgroundColor: '#f8fafc', padding: 14, borderRadius: 8, border: '1px solid #f1f5f9', display: 'flex', flexDirection: 'column', gap: 8, fontSize: 13 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#64748b' }}>Tổng học phí:</span>
                    <strong style={{ color: '#0f172a' }}>{Number(t.soTienPhaiNop).toLocaleString('vi-VN')} VNĐ</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#64748b' }}>Đã thanh toán:</span>
                    <strong style={{ color: '#16a34a' }}>{Number(t.soTienDaNop).toLocaleString('vi-VN')} VNĐ</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px dashed #cbd5e1', paddingTop: 8, fontSize: 14 }}>
                    <span style={{ color: '#334155', fontWeight: 600 }}>Cần nộp tiếp:</span>
                    <strong style={{ color: t.soTienConLai > 0 ? '#dc2626' : '#16a34a' }}>{Number(t.soTienConLai).toLocaleString('vi-VN')} VNĐ</strong>
                  </div>
                </div>
              </div>

              <div style={{ marginTop: 20 }}>
                {t.soTienConLai > 0 ? (
                  <button
                    onClick={() => handlePay(t)}
                    disabled={processingId === t.maHocPhi}
                    style={{ width: '100%', padding: '12px', backgroundColor: processingId === t.maHocPhi ? '#94a3b8' : '#2563eb', color: '#fff', border: 'none', borderRadius: 8, fontWeight: 700, cursor: processingId === t.maHocPhi ? 'not-allowed' : 'pointer', fontSize: 14, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 8 }}
                  >
                    {processingId === t.maHocPhi ? '🔄 Đang chuyển tới VNPay...' : '💳 Thanh Toán Qua VNPay Ngay'}
                  </button>
                ) : (
                  <div style={{ textAlign: 'center', padding: '10px', backgroundColor: '#f0fdf4', color: '#166534', borderRadius: 8, fontWeight: 600, fontSize: 13 }}>
                    🎉 Bạn đã nộp đủ học phí học kỳ này!
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
