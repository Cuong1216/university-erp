import React from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

export const TuitionResultPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const status = searchParams.get('status') || 'UNKNOWN';
  const txnRef = searchParams.get('txnRef') || '';
  const amountStr = searchParams.get('amount') || '0';
  const code = searchParams.get('code') || '';

  const isSuccess = status === 'SUCCESS' || code === '00';
  const amountVal = (Number(amountStr) / 100).toLocaleString('vi-VN'); // VNPay trả về amount nhân 100

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif', maxWidth: 650, margin: '40px auto', backgroundColor: '#fff', borderRadius: 16, border: '1px solid #e2e8f0', boxShadow: '0 25px 50px -12px rgba(0,0,0,0.1)', overflow: 'hidden' }}>
      <div style={{ padding: '30px 24px', backgroundColor: isSuccess ? '#0f172a' : '#991b1b', color: '#fff', textAlign: 'center' }}>
        <span style={{ fontSize: 56, display: 'block', marginBottom: 10 }}>
          {isSuccess ? '🎉' : '❌'}
        </span>
        <h2 style={{ margin: 0, fontSize: 24, fontWeight: 800 }}>
          {isSuccess ? 'Giao Dịch Thanh Toán Học Phí Thành Công' : 'Thanh Toán Không Thành Công'}
        </h2>
        <p style={{ margin: '8px 0 0 0', fontSize: 14, color: isSuccess ? '#86efac' : '#fecaca' }}>
          {isSuccess
            ? 'Hệ thống đã tự động đối soát và gạch nợ học phí của bạn thông qua Webhook Idempotent.'
            : 'Giao dịch bị hủy hoặc xảy ra lỗi chốt giao dịch với cổng thanh toán VNPay.'}
        </p>
      </div>

      <div style={{ padding: 28, display: 'flex', flexDirection: 'column', gap: 16, fontSize: 14 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: 12, borderBottom: '1px solid #f1f5f9' }}>
          <span style={{ color: '#64748b' }}>Mã tham chiếu giao dịch (TxnRef):</span>
          <strong style={{ color: '#0f172a', fontFamily: 'monospace' }}>{txnRef}</strong>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: 12, borderBottom: '1px solid #f1f5f9' }}>
          <span style={{ color: '#64748b' }}>Số tiền thanh toán:</span>
          <strong style={{ color: isSuccess ? '#16a34a' : '#dc2626', fontSize: 16 }}>{amountVal} VNĐ</strong>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: 12, borderBottom: '1px solid #f1f5f9' }}>
          <span style={{ color: '#64748b' }}>Mã phản hồi cổng thanh toán:</span>
          <strong style={{ color: '#334155' }}>{code || status}</strong>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ color: '#64748b' }}>Cơ chế bảo mật Webhook:</span>
          <span style={{ backgroundColor: '#eff6ff', color: '#1d4ed8', padding: '2px 8px', borderRadius: 6, fontWeight: 600, fontSize: 12 }}>
            🔒 SERIALIZABLE + UNIQUE Idempotent
          </span>
        </div>

        <div style={{ marginTop: 20, textAlign: 'center' }}>
          <button
            onClick={() => navigate('/tuition')}
            style={{ padding: '12px 32px', backgroundColor: '#2563eb', color: '#fff', border: 'none', borderRadius: 8, fontWeight: 700, cursor: 'pointer', fontSize: 15 }}
          >
            Quay Lại Trang Tra Cứu Học Phí
          </button>
        </div>
      </div>
    </div>
  );
};
