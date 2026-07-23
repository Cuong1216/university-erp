import React, { useEffect, useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { axiosClient } from '../api/axiosClient';

interface MySalaryRecord {
  maBangLuong: string;
  maGv: string;
  thang: number;
  nam: number;
  tongSoTietThucTe: number;
  heSoCdSnapshot: number;
  heSoHvSnapshot: number;
  luongCoBanSnapshot: number;
  donGiaTietSnapshot: number;
  tongTienLuong: number;
  trangThai: string;
  ngayChotLuong: string;
}

export const TeacherSalaryPage: React.FC = () => {
  const { userInfo } = useAuthStore();
  const [salaries, setSalaries] = useState<MySalaryRecord[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMySalary = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await axiosClient.get('/luong/my-salary');
        setSalaries(response.data || []);
      } catch (error) {
        const err = error as any;
        setError(err.response?.data?.message || 'Không thể tải dữ liệu thù lao giảng dạy.');
      } finally {
        setLoading(false);
      }
    };
    fetchMySalary();
  }, []);

  const latestSalary = salaries.length > 0 ? salaries[0] : null;

  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Thù Lao Giảng Dạy & Nhật Ký Buổi Dạy</h1>
      <div style={{ padding: 16, backgroundColor: '#dcfce7', color: '#166534', borderRadius: 8, border: '1px solid #bbf7d0', marginBottom: 20 }}>
        <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền <code>ROLE_GIANG_VIEN</code>.
      </div>

      {error && (
        <div style={{ padding: '12px 16px', backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: 8, border: '1px solid #fecaca', marginBottom: 20 }}>
          ⚠️ {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: 40, textAlign: 'center', color: '#64748b' }}>Đang tải dữ liệu thù lao...</div>
      ) : (
        <>
          <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0', marginBottom: 24 }}>
            <h3 style={{ marginTop: 0, color: '#1e293b' }}>
              Thù lao mới nhất của Giảng viên: {userInfo?.fullName || userInfo?.username}
              {latestSalary && <span style={{ fontSize: 14, fontWeight: 'normal', color: '#64748b', marginLeft: 8 }}>(Tháng {latestSalary.thang}/{latestSalary.nam})</span>}
            </h3>
            <p style={{ color: '#64748b' }}>
              Theo dõi tổng số tiết thực giảng, định mức giờ chuẩn và chi tiết thù lao giảng dạy từng tháng/học kỳ.
            </p>

            {latestSalary ? (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16, marginTop: 16 }}>
                <div style={{ padding: 16, backgroundColor: '#f8fafc', borderRadius: 8, border: '1px solid #e2e8f0' }}>
                  <div style={{ fontSize: 13, color: '#64748b' }}>Tổng số tiết thực giảng</div>
                  <div style={{ fontSize: 24, fontWeight: 'bold', color: '#0f172a', marginTop: 4 }}>{latestSalary.tongSoTietThucTe} tiết</div>
                </div>
                <div style={{ padding: 16, backgroundColor: '#f8fafc', borderRadius: 8, border: '1px solid #e2e8f0' }}>
                  <div style={{ fontSize: 13, color: '#64748b' }}>Đơn giá thù lao/tiết</div>
                  <div style={{ fontSize: 24, fontWeight: 'bold', color: '#3b82f6', marginTop: 4 }}>
                    {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(latestSalary.donGiaTietSnapshot)}
                  </div>
                </div>
                <div style={{ padding: 16, backgroundColor: '#f0fdf4', borderRadius: 8, border: '1px solid #bbf7d0' }}>
                  <div style={{ fontSize: 13, color: '#166534' }}>Tổng tiền thù lao</div>
                  <div style={{ fontSize: 24, fontWeight: 'bold', color: '#15803d', marginTop: 4 }}>
                    {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(latestSalary.tongTienLuong)}
                  </div>
                </div>
              </div>
            ) : (
              <div style={{ padding: 20, backgroundColor: '#f8fafc', borderRadius: 8, color: '#64748b', marginTop: 16 }}>
                Chưa có bảng lương nào được chốt cho bạn trong hệ thống.
              </div>
            )}
          </div>

          {salaries.length > 1 && (
            <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0' }}>
              <h4 style={{ marginTop: 0, color: '#1e293b', marginBottom: 12 }}>Lịch sử bảng lương các tháng trước</h4>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #e2e8f0', textAlign: 'left', color: '#475569' }}>
                    <th style={{ padding: '8px 12px' }}>Thời gian</th>
                    <th style={{ padding: '8px 12px' }}>Mã bảng lương</th>
                    <th style={{ padding: '8px 12px' }}>Tiết dạy</th>
                    <th style={{ padding: '8px 12px' }}>Lương cơ bản</th>
                    <th style={{ padding: '8px 12px' }}>Tổng thù lao</th>
                    <th style={{ padding: '8px 12px' }}>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {salaries.map((sal) => (
                    <tr key={sal.maBangLuong} style={{ borderBottom: '1px solid #f1f5f9' }}>
                      <td style={{ padding: '10px 12px', fontWeight: 600 }}>Tháng {sal.thang}/{sal.nam}</td>
                      <td style={{ padding: '10px 12px', color: '#64748b' }}>{sal.maBangLuong}</td>
                      <td style={{ padding: '10px 12px' }}>{sal.tongSoTietThucTe} tiết</td>
                      <td style={{ padding: '10px 12px' }}>
                        {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(sal.luongCoBanSnapshot)}
                      </td>
                      <td style={{ padding: '10px 12px', fontWeight: 'bold', color: '#15803d' }}>
                        {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(sal.tongTienLuong)}
                      </td>
                      <td style={{ padding: '10px 12px' }}>
                        <span style={{ padding: '2px 8px', borderRadius: 12, fontSize: 12, backgroundColor: '#dcfce7', color: '#166534' }}>
                          {sal.trangThai}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
};
