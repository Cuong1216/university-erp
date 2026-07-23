import React, { useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { AutoScheduleModal } from '../components/schedule/AutoScheduleModal';
import type { ScheduledSlot } from '../api/scheduleOptimizationApi';

export const SchedulePage: React.FC = () => {
  const { roles } = useAuthStore();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [scheduledSlots, setScheduledSlots] = useState<ScheduledSlot[]>([
    // Một vài dữ liệu mẫu ban đầu để hiển thị khi chưa chạy AI
    { maLopHp: 'HP-CNTT01', tenMon: 'Cấu trúc dữ liệu & Giải thuật', tenGiangVien: 'TS. Nguyễn Văn Hùng', phongHoc: 'A1-101', thuTrongTuan: 2, tietBatDau: 1, tietKetThuc: 3 },
    { maLopHp: 'HP-CNTT03', tenMon: 'Trí tuệ nhân tạo (AI)', tenGiangVien: 'PGS.TS Lê Thị Mai', phongHoc: 'B2-202', thuTrongTuan: 3, tietBatDau: 4, tietKetThuc: 6 },
    { maLopHp: 'HP-NN01', tenMon: 'Tiếng Anh Chuyên ngành CNTT', tenGiangVien: 'ThS. Hoàng Thu Thủy', phongHoc: 'C3-301', thuTrongTuan: 5, tietBatDau: 7, tietKetThuc: 9 },
  ]);

  const [selectedDayFilter, setSelectedDayFilter] = useState<number | 'ALL'>('ALL');
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  const isAdminOrGiaoVu = roles.some((r) => r === 'ROLE_ADMIN' || r === 'ROLE_GIAO_VU');

  const handleApplySchedule = (newSlots: ScheduledSlot[]) => {
    setScheduledSlots(newSlots);
    setSuccessBanner(`Đã áp dụng thành công ${newSlots.length} lớp học phần được xếp tự động bởi Google OR-Tools AI Solver!`);
    setTimeout(() => setSuccessBanner(null), 7000);
  };

  const getDayName = (day: number) => (day === 8 ? 'Chủ nhật' : `Thứ ${day}`);

  const getPeriodName = (startP: number) => {
    if (startP === 1) return 'Ca 1 (Tiết 1-3 | 07:00 - 09:25)';
    if (startP === 4) return 'Ca 2 (Tiết 4-6 | 09:35 - 12:00)';
    if (startP === 7) return 'Ca 3 (Tiết 7-9 | 13:00 - 15:25)';
    if (startP === 10) return 'Ca 4 (Tiết 10-12 | 15:35 - 18:00)';
    return `Tiết ${startP}-${startP + 2}`;
  };

  const filteredSlots = selectedDayFilter === 'ALL'
    ? scheduledSlots
    : scheduledSlots.filter((slot) => slot.thuTrongTuan === selectedDayFilter);

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16, marginBottom: 24 }}>
        <div>
          <h1 style={{ margin: 0, color: '#0f172a', fontSize: 26, fontWeight: 800 }}>
            Quản Lý Lịch Học & Sắp Xếp Lịch Tự Động
          </h1>
          <p style={{ margin: '6px 0 0 0', color: '#64748b', fontSize: 14 }}>
            Xem thời gian biểu các lớp học phần và kích hoạt công cụ tối ưu hóa ràng buộc AI.
          </p>
        </div>

        {isAdminOrGiaoVu && (
          <button
            onClick={() => setIsModalOpen(true)}
            style={{
              padding: '12px 22px',
              borderRadius: '50px',
              backgroundColor: '#2563eb',
              color: '#ffffff',
              border: 'none',
              fontWeight: 700,
              fontSize: 14,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              boxShadow: '0 10px 15px -3px rgba(37, 99, 235, 0.3)',
              transition: 'transform 0.2s, background-color 0.2s',
            }}
            onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.03)')}
            onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1)')}
          >
            <span style={{ fontSize: 20 }}>⚡</span>
            <span>Tự Động Xếp Lịch AI (OR-Tools Constraint Solver)</span>
          </button>
        )}
      </div>

      {/* Success Notification Banner */}
      {successBanner && (
        <div
          style={{
            padding: '14px 20px',
            backgroundColor: '#dcfce7',
            border: '1px solid #86efac',
            color: '#166534',
            borderRadius: 12,
            marginBottom: 20,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontWeight: 600,
            boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <span style={{ fontSize: 20 }}>🎉</span>
            <span>{successBanner}</span>
          </div>
          <button
            onClick={() => setSuccessBanner(null)}
            style={{ background: 'transparent', border: 'none', color: '#166534', fontSize: 18, cursor: 'pointer' }}
          >
            ✕
          </button>
        </div>
      )}

      {/* Filter and Status Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, backgroundColor: '#ffffff', padding: 16, borderRadius: 12, border: '1px solid #e2e8f0', marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span style={{ fontSize: 13, fontWeight: 700, color: '#334155' }}>🗓️ Lọc theo thứ:</span>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            <button
              onClick={() => setSelectedDayFilter('ALL')}
              style={{
                padding: '6px 12px',
                borderRadius: 6,
                border: selectedDayFilter === 'ALL' ? '2px solid #2563eb' : '1px solid #cbd5e1',
                backgroundColor: selectedDayFilter === 'ALL' ? '#eff6ff' : '#ffffff',
                color: selectedDayFilter === 'ALL' ? '#1d4ed8' : '#64748b',
                fontWeight: selectedDayFilter === 'ALL' ? 700 : 500,
                fontSize: 12,
                cursor: 'pointer',
              }}
            >
              Tất Cả
            </button>
            {[2, 3, 4, 5, 6, 7].map((day) => (
              <button
                key={day}
                onClick={() => setSelectedDayFilter(day)}
                style={{
                  padding: '6px 12px',
                  borderRadius: 6,
                  border: selectedDayFilter === day ? '2px solid #2563eb' : '1px solid #cbd5e1',
                  backgroundColor: selectedDayFilter === day ? '#eff6ff' : '#ffffff',
                  color: selectedDayFilter === day ? '#1d4ed8' : '#64748b',
                  fontWeight: selectedDayFilter === day ? 700 : 500,
                  fontSize: 12,
                  cursor: 'pointer',
                }}
              >
                {getDayName(day)}
              </button>
            ))}
          </div>
        </div>

        <div style={{ fontSize: 13, color: '#64748b', fontWeight: 500 }}>
          Hiển thị: <strong style={{ color: '#0f172a' }}>{filteredSlots.length}</strong> lớp học phần
        </div>
      </div>

      {/* Timetable Grid / List */}
      {filteredSlots.length === 0 ? (
        <div style={{ backgroundColor: '#ffffff', padding: 48, borderRadius: 12, border: '1px dashed #cbd5e1', textAlign: 'center' }}>
          <span style={{ fontSize: 36, display: 'block', marginBottom: 12 }}>📭</span>
          <h4 style={{ margin: '0 0 6px 0', color: '#334155', fontSize: 16 }}>Chưa có lịch học nào cho ngày này</h4>
          <p style={{ margin: 0, color: '#64748b', fontSize: 13 }}>
            {isAdminOrGiaoVu ? 'Hãy nhấn nút "Tự Động Xếp Lịch AI" ở trên để tạo thời gian biểu.' : 'Vui lòng chọn ngày khác hoặc liên hệ Giao vụ khoa.'}
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 20 }}>
          {filteredSlots.map((slot, index) => (
            <div
              key={index}
              style={{
                backgroundColor: '#ffffff',
                borderRadius: 12,
                border: '1px solid #e2e8f0',
                padding: 20,
                boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                position: 'relative',
                overflow: 'hidden',
              }}
            >
              <div style={{ position: 'absolute', top: 0, left: 0, width: 6, height: '100%', backgroundColor: slot.thuTrongTuan % 2 === 0 ? '#3b82f6' : '#10b981' }} />
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                  <span style={{ backgroundColor: '#eff6ff', color: '#1d4ed8', padding: '3px 10px', borderRadius: 20, fontSize: 12, fontWeight: 700, border: '1px solid #bfdbfe' }}>
                    {slot.maLopHp}
                  </span>
                  <span style={{ backgroundColor: '#f1f5f9', color: '#0f172a', padding: '3px 10px', borderRadius: 6, fontSize: 12, fontWeight: 700 }}>
                    {getDayName(slot.thuTrongTuan)}
                  </span>
                </div>

                <h3 style={{ margin: '0 0 10px 0', color: '#0f172a', fontSize: 17, fontWeight: 700, lineHeight: 1.4 }}>
                  {slot.tenMon}
                </h3>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 13, color: '#475569' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span>👨‍🏫 Giảng viên:</span>
                    <strong style={{ color: '#1e293b' }}>{slot.tenGiangVien || slot.maGv || 'Chưa phân công'}</strong>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span>🏫 Phòng học:</span>
                    <strong style={{ color: '#059669', backgroundColor: '#ecfdf5', padding: '2px 8px', borderRadius: 4, border: '1px solid #a7f3d0' }}>
                      {slot.phongHoc}
                    </strong>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span>⏰ Thời gian:</span>
                    <strong style={{ color: '#2563eb' }}>{getPeriodName(slot.tietBatDau)}</strong>
                  </div>
                </div>
              </div>

              <div style={{ marginTop: 16, paddingTop: 12, borderTop: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 11, color: '#94a3b8' }}>
                <span>Tuần học: 1 - 10</span>
                <span>Trạng thái: Đã xếp</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Auto Schedule AI Modal */}
      <AutoScheduleModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onApplySchedule={handleApplySchedule}
      />
    </div>
  );
};
