import React, { useEffect, useState, useCallback } from 'react';
import { paymentApi } from '../api/paymentApi';
import type { TuitionResponse } from '../api/paymentApi';
import { useAuthStore } from '../store/useAuthStore';
import { Badge } from '../components/common/Badge';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';

export const TuitionPage: React.FC = () => {
  const { roles } = useAuthStore();
  const [tuitions, setTuitions] = useState<TuitionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const isAdminOrGiaoVu = roles.some((r) => ['ROLE_ADMIN', 'ROLE_GIAO_VU'].includes(r));

  const fetchTuitions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = isAdminOrGiaoVu 
        ? await paymentApi.getAllTuitions() 
        : await paymentApi.getMyTuitions();
      setTuitions(data);
      setTotalPages(1);
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const err = error as any;
      setError(err?.response?.data?.message || 'Lỗi kết nối tới hệ thống tài chính học phí');
    } finally {
      setLoading(false);
    }
  }, [isAdminOrGiaoVu, currentPage, pageSize]);

  useEffect(() => {
    // eslint-disable-next-line
    fetchTuitions();
  }, [fetchTuitions]);

  const handlePay = async (item: TuitionResponse) => {
    setProcessingId(item.maHocPhi);
    try {
      const resp = await paymentApi.createPaymentUrl({
        maHocPhi: item.maHocPhi,
        amount: item.soTienConLai,
        returnUrl: window.location.origin + '/tuition/result',
      });
      if (resp && resp.paymentUrl) {
        window.location.assign(resp.paymentUrl);
      } else {
        alert('Không nhận được URL thanh toán VNPay.');
        setProcessingId(null);
      }
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const err = error as any;
      alert(err?.response?.data?.message || 'Lỗi khởi tạo cổng thanh toán VNPay.');
      setProcessingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'DA_NOP_DU':
        return <Badge variant="success">✓ Đã Nộp Đủ</Badge>;
      case 'NOP_MOT_PHAN':
        return <Badge variant="warning">⏳ Nộp Một Phần</Badge>;
      default:
        return <Badge variant="danger">⚠️ Chưa Nộp</Badge>;
    }
  };

  return (
    <div style={{ fontFamily: 'var(--sans)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-lg)', flexWrap: 'wrap', gap: 'var(--space-md)' }}>
        <div>
          <h1 style={{ margin: 0, color: 'var(--color-text-primary)', fontSize: 'var(--font-size-xl)', fontWeight: 800 }}>
            💳 Cổng Tra Cứu & Thanh Toán Học Phí Trực Tuyến
          </h1>
          <p style={{ margin: 'var(--space-xs) 0 0 0', color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-base)' }}>
            Tích hợp cổng thanh toán VNPay với cơ chế phòng thủ Webhook Idempotency & chữ ký bảo mật HMAC SHA512.
          </p>
        </div>
        <Button onClick={fetchTuitions}>
          🔄 Làm mới dữ liệu
        </Button>
      </div>

      {loading ? (
        <div style={{ padding: '60px', textAlign: 'center', color: 'var(--color-text-secondary)', fontSize: '15px' }}>Đang tải danh sách học phí...</div>
      ) : error ? (
        <div style={{ padding: 'var(--space-lg)', backgroundColor: 'var(--color-danger-light)', border: '1px solid #fecaca', borderRadius: 'var(--radius-lg)', color: 'var(--color-danger)' }}>
          <h4>⚠️ Lỗi tải dữ liệu học phí</h4>
          <p>{error}</p>
        </div>
      ) : tuitions.length === 0 ? (
        <div style={{ padding: '48px', textAlign: 'center', backgroundColor: 'var(--color-bg-card)', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--color-border)', color: 'var(--color-text-secondary)' }}>
          Chưa có thông báo nộp học phí nào trong học kỳ này.
        </div>
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
            {tuitions.map((t) => (
              <Card key={t.maHocPhi}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontSize: 'var(--font-size-sm)', fontWeight: 700, color: 'var(--color-primary)', backgroundColor: 'var(--color-primary-light)', padding: '3px 10px', borderRadius: 'var(--radius-sm)' }}>{t.maHocPhi}</span>
                    {getStatusBadge(t.trangThai)}
                  </div>
                  <h3 style={{ margin: '0 0 4px 0', fontSize: 'var(--font-size-lg)', color: 'var(--color-text-primary)' }}>Học Kỳ {t.hocKy} - Năm học {t.namHoc}</h3>
                  <p style={{ margin: '0 0 16px 0', fontSize: 'var(--font-size-sm)', color: 'var(--color-text-secondary)' }}>Mã SV: <strong style={{ color: '#1e293b' }}>{t.maSv}</strong></p>

                  <div style={{ backgroundColor: 'var(--color-bg-page)', padding: '14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-bg-page)', display: 'flex', flexDirection: 'column', gap: '8px', fontSize: 'var(--font-size-sm)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: 'var(--color-text-secondary)' }}>Tổng học phí:</span>
                      <strong style={{ color: 'var(--color-text-primary)' }}>{Number(t.soTienPhaiNop).toLocaleString('vi-VN')} VNĐ</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: 'var(--color-text-secondary)' }}>Đã thanh toán:</span>
                      <strong style={{ color: 'var(--color-success)' }}>{Number(t.soTienDaNop).toLocaleString('vi-VN')} VNĐ</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px dashed var(--color-border)', paddingTop: '8px', fontSize: 'var(--font-size-base)' }}>
                      <span style={{ color: 'var(--color-text-primary)', fontWeight: 600 }}>Cần nộp tiếp:</span>
                      <strong style={{ color: t.soTienConLai > 0 ? 'var(--color-danger)' : 'var(--color-success)' }}>{Number(t.soTienConLai).toLocaleString('vi-VN')} VNĐ</strong>
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: '20px' }}>
                  {t.soTienConLai > 0 ? (
                    <Button
                      onClick={() => handlePay(t)}
                      loading={processingId === t.maHocPhi}
                      style={{ width: '100%' }}
                    >
                      {processingId === t.maHocPhi ? '🔄 Đang chuyển tới VNPay...' : '💳 Thanh Toán Qua VNPay Ngay'}
                    </Button>
                  ) : (
                    <div style={{ textAlign: 'center', padding: '10px', backgroundColor: 'var(--color-success-light)', color: 'var(--color-success)', borderRadius: 'var(--radius-md)', fontWeight: 600, fontSize: 'var(--font-size-sm)' }}>
                      🎉 Bạn đã nộp đủ học phí học kỳ này!
                    </div>
                  )}
                </div>
              </Card>
            ))}
          </div>

          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: '32px', gap: '16px' }}>
            <Button
              variant="secondary"
              onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
              disabled={currentPage === 0}
            >
              Previous
            </Button>
            <span style={{ fontSize: '15px', fontWeight: 600, color: 'var(--color-text-secondary)' }}>
              Page {currentPage + 1} of {totalPages === 0 ? 1 : totalPages}
            </span>
            <Button
              variant="secondary"
              onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
              disabled={currentPage >= totalPages - 1}
            >
              Next
            </Button>
          </div>
        </>
      )}
    </div>
  );
};
