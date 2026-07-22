import React from 'react';
import { useAuthStore } from '../store/useAuthStore';

export const StudentGradesPage: React.FC = () => {
  const { userInfo } = useAuthStore();

  return (
    <div>
      <h1 style={{ marginTop: 0, color: '#0f172a' }}>Xem Điểm & Kết Quả Học Tập</h1>
      <div style={{ padding: 16, backgroundColor: '#e0f2fe', color: '#0369a1', borderRadius: 8, border: '1px solid #bae6fd', marginBottom: 20 }}>
        <strong>✓ Hợp lệ:</strong> Bạn đang truy cập dưới quyền <code>ROLE_SINH_VIEN</code>.
      </div>

      <div style={{ backgroundColor: '#ffffff', padding: 20, borderRadius: 8, border: '1px solid #e2e8f0' }}>
        <h3 style={{ marginTop: 0, color: '#1e293b' }}>Bảng điểm của sinh viên: {userInfo?.fullName || userInfo?.username}</h3>
        <p style={{ color: '#64748b' }}>
          Tại đây hiển thị kết quả học tập từng học kỳ, điểm thành phần, điểm thi kết thúc học phần và điểm trung bình tích lũy (GPA).
        </p>
        
        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 16 }}>
          <thead>
            <tr style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0', textAlign: 'left' }}>
              <th style={{ padding: '12px' }}>Mã HP</th>
              <th style={{ padding: '12px' }}>Tên Học Phần</th>
              <th style={{ padding: '12px' }}>Số TC</th>
              <th style={{ padding: '12px' }}>Điểm CC</th>
              <th style={{ padding: '12px' }}>Điểm GK</th>
              <th style={{ padding: '12px' }}>Điểm CK</th>
              <th style={{ padding: '12px' }}>Điểm TK</th>
              <th style={{ padding: '12px' }}>Kết quả</th>
            </tr>
          </thead>
          <tbody>
            <tr style={{ borderBottom: '1px solid #e2e8f0' }}>
              <td style={{ padding: '12px' }}>CSC101</td>
              <td style={{ padding: '12px' }}>Nhập môn Lập trình</td>
              <td style={{ padding: '12px' }}>3</td>
              <td style={{ padding: '12px' }}>9.0</td>
              <td style={{ padding: '12px' }}>8.5</td>
              <td style={{ padding: '12px' }}>8.5</td>
              <td style={{ padding: '12px' }}>8.6</td>
              <td style={{ padding: '12px', color: '#16a34a', fontWeight: 'bold' }}>Đạt</td>
            </tr>
            <tr style={{ borderBottom: '1px solid #e2e8f0' }}>
              <td style={{ padding: '12px' }}>CSC102</td>
              <td style={{ padding: '12px' }}>Cấu trúc dữ liệu & Giải thuật</td>
              <td style={{ padding: '12px' }}>4</td>
              <td style={{ padding: '12px' }}>8.5</td>
              <td style={{ padding: '12px' }}>8.0</td>
              <td style={{ padding: '12px' }}>9.0</td>
              <td style={{ padding: '12px' }}>8.7</td>
              <td style={{ padding: '12px', color: '#16a34a', fontWeight: 'bold' }}>Đạt</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};
