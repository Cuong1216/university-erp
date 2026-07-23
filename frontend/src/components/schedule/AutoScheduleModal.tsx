import React, { useEffect, useState } from 'react';
import { scheduleOptimizationApi } from '../../api/scheduleOptimizationApi';
import type { ClassRequirement, ScheduledSlot, ScheduleOptimizationResponse } from '../../api/scheduleOptimizationApi';

interface AutoScheduleModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApplySchedule: (slots: ScheduledSlot[]) => void;
}

export const AutoScheduleModal: React.FC<AutoScheduleModalProps> = ({ isOpen, onClose, onApplySchedule }) => {
  const [classes, setClasses] = useState<ClassRequirement[]>([]);
  const [loadingSample, setLoadingSample] = useState(false);
  const [solving, setSolving] = useState(false);
  const [result, setResult] = useState<ScheduleOptimizationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Config defaults
  const [selectedDays, setSelectedDays] = useState<number[]>([2, 3, 4, 5, 6, 7]);
  const [selectedPeriods, setSelectedPeriods] = useState<number[]>([1, 4, 7, 10]);

  const loadSampleClasses = async () => {
    setLoadingSample(true);
    try {
      const data = await scheduleOptimizationApi.getSampleClasses();
      setClasses(data);
    } catch (err) {
      console.error('Lỗi tải sample classes:', err);
    } finally {
      setLoadingSample(false);
    }
  };

  useEffect(() => {
    if (isOpen && classes.length === 0) {
      // eslint-disable-next-line
      loadSampleClasses();
    }
  }, [isOpen, classes.length]);

  if (!isOpen) return null;



  const handleToggleDay = (day: number) => {
    if (selectedDays.includes(day)) {
      if (selectedDays.length > 1) setSelectedDays(selectedDays.filter((d) => d !== day));
    } else {
      setSelectedDays([...selectedDays, day].sort());
    }
  };

  const handleTogglePeriod = (p: number) => {
    if (selectedPeriods.includes(p)) {
      if (selectedPeriods.length > 1) setSelectedPeriods(selectedPeriods.filter((item) => item !== p));
    } else {
      setSelectedPeriods([...selectedPeriods, p].sort());
    }
  };

  const handleRunOptimization = async () => {
    setSolving(true);
    setError(null);
    setResult(null);
    try {
      const resp = await scheduleOptimizationApi.optimizeSchedule({
        classesToSchedule: classes,
        availableDays: selectedDays,
        startPeriods: selectedPeriods,
      });
      setResult(resp);
    } catch (error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const err = error as any;
      setError(err?.response?.data?.message || 'Lỗi kết nối tới dịch vụ AI Schedule Optimization Solver.');
    } finally {
      setSolving(false);
    }
  };

  const getDayName = (day: number) => (day === 8 ? 'Chủ nhật' : `Thứ ${day}`);

  const getPeriodName = (startP: number) => {
    if (startP === 1) return 'Ca 1 (Tiết 1-3 - Sáng)';
    if (startP === 4) return 'Ca 2 (Tiết 4-6 - Sáng/Trưa)';
    if (startP === 7) return 'Ca 3 (Tiết 7-9 - Chiều)';
    if (startP === 10) return 'Ca 4 (Tiết 10-12 - Tối)';
    return `Tiết ${startP}-${startP + 2}`;
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(15, 23, 42, 0.65)',
        backdropFilter: 'blur(4px)',
        zIndex: 1100,
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
      }}
    >
      <div
        style={{
          backgroundColor: '#ffffff',
          borderRadius: 16,
          width: '950px',
          maxWidth: '96vw',
          maxHeight: '92vh',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
          overflow: 'hidden',
          border: '1px solid #e2e8f0',
        }}
      >
        {/* Modal Header */}
        <div
          style={{
            padding: '18px 24px',
            backgroundColor: '#0f172a',
            color: '#ffffff',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span style={{ fontSize: 24 }}>⚡</span>
            <div>
              <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700 }}>
                Trình Tự Động Xếp Lịch AI (Google OR-Tools Constraint Solver)
              </h3>
              <p style={{ margin: '2px 0 0 0', fontSize: 12, color: '#94a3b8' }}>
                Giải quyết bài toán thỏa mãn ràng buộc (CSP) không trùng phòng và không trùng lịch giảng viên
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{ background: 'transparent', border: 'none', color: '#cbd5e1', fontSize: 22, cursor: 'pointer' }}
          >
            ✕
          </button>
        </div>

        {/* Modal Body */}
        <div style={{ padding: 24, overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Section 1: Danh sách học phần cần xếp lịch */}
          <div style={{ backgroundColor: '#f8fafc', padding: 16, borderRadius: 12, border: '1px solid #e2e8f0' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <h4 style={{ margin: 0, color: '#1e293b', fontSize: 15, display: 'flex', alignItems: 'center', gap: 8 }}>
                <span>📚</span> Danh sách học phần chờ xếp thời gian biểu ({classes.length} lớp)
              </h4>
              <button
                onClick={loadSampleClasses}
                disabled={loadingSample || solving}
                style={{ padding: '4px 10px', fontSize: 12, borderRadius: 6, border: '1px solid #cbd5e1', backgroundColor: '#ffffff', cursor: 'pointer', fontWeight: 600 }}
              >
                🔄 Tải lại mẫu
              </button>
            </div>

            {loadingSample ? (
              <div style={{ padding: 20, textAlign: 'center', color: '#64748b', fontSize: 13 }}>Đang tải danh sách học phần...</div>
            ) : (
              <div style={{ maxHeight: 150, overflowY: 'auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 8 }}>
                {classes.map((c, i) => (
                  <div key={i} style={{ padding: '8px 12px', backgroundColor: '#ffffff', borderRadius: 8, border: '1px solid #e2e8f0', fontSize: 13 }}>
                    <strong style={{ color: '#2563eb' }}>{c.maLopHp}</strong> - {c.tenMon}
                    <div style={{ fontSize: 11, color: '#64748b', marginTop: 2 }}>
                      GV: {c.tenGiangVien} | {c.soTiet || 3} tiết/buổi
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Section 2: Cấu hình ràng buộc thời gian */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div style={{ padding: 14, backgroundColor: '#ffffff', borderRadius: 10, border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: 13, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 8 }}>
                🗓️ Các ngày cho phép xếp lịch:
              </label>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                {[2, 3, 4, 5, 6, 7].map((day) => {
                  const isSel = selectedDays.includes(day);
                  return (
                    <button
                      key={day}
                      type="button"
                      disabled={solving}
                      onClick={() => handleToggleDay(day)}
                      style={{
                        padding: '6px 12px',
                        borderRadius: 6,
                        border: isSel ? '2px solid #3b82f6' : '1px solid #cbd5e1',
                        backgroundColor: isSel ? '#eff6ff' : '#ffffff',
                        color: isSel ? '#1d4ed8' : '#64748b',
                        fontWeight: isSel ? 700 : 500,
                        fontSize: 12,
                        cursor: 'pointer',
                      }}
                    >
                      {getDayName(day)}
                    </button>
                  );
                })}
              </div>
            </div>

            <div style={{ padding: 14, backgroundColor: '#ffffff', borderRadius: 10, border: '1px solid #e2e8f0' }}>
              <label style={{ fontSize: 13, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 8 }}>
                ⏰ Các ca học cho phép:
              </label>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                {[
                  { id: 1, label: 'Sáng (1-3)' },
                  { id: 4, label: 'Trưa (4-6)' },
                  { id: 7, label: 'Chiều (7-9)' },
                  { id: 10, label: 'Tối (10-12)' },
                ].map((p) => {
                  const isSel = selectedPeriods.includes(p.id);
                  return (
                    <button
                      key={p.id}
                      type="button"
                      disabled={solving}
                      onClick={() => handleTogglePeriod(p.id)}
                      style={{
                        padding: '6px 12px',
                        borderRadius: 6,
                        border: isSel ? '2px solid #3b82f6' : '1px solid #cbd5e1',
                        backgroundColor: isSel ? '#eff6ff' : '#ffffff',
                        color: isSel ? '#1d4ed8' : '#64748b',
                        fontWeight: isSel ? 700 : 500,
                        fontSize: 12,
                        cursor: 'pointer',
                      }}
                    >
                      {p.label}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Execute Button */}
          <div style={{ textAlign: 'center' }}>
            <button
              onClick={handleRunOptimization}
              disabled={solving || classes.length === 0}
              style={{
                padding: '14px 32px',
                borderRadius: '50px',
                backgroundColor: solving ? '#94a3b8' : '#2563eb',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                fontSize: 15,
                cursor: solving || classes.length === 0 ? 'not-allowed' : 'pointer',
                boxShadow: '0 10px 15px -3px rgba(37, 99, 235, 0.3)',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 10,
                transition: 'all 0.2s',
              }}
            >
              {solving ? (
                <>
                  <span style={{ display: 'inline-block', width: 16, height: 16, border: '2px solid #fff', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
                  <span>AI đang giải bài toán ràng buộc CSP...</span>
                </>
              ) : (
                <>
                  <span style={{ fontSize: 18 }}>⚡</span>
                  <span>Bắt Đầu Tự Động Xếp Lịch AI (OR-Tools)</span>
                </>
              )}
            </button>
          </div>

          {error && (
            <div style={{ padding: 14, backgroundColor: '#fef2f2', border: '1px solid #fecaca', borderRadius: 8, color: '#991b1b', fontSize: 13 }}>
              ⚠️ {error}
            </div>
          )}

          {/* Section 3: Kết quả tối ưu hóa từ OR-Tools */}
          {result && (
            <div style={{ backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 12, padding: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12, marginBottom: 14 }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontSize: 20 }}>🎯</span>
                    <h4 style={{ margin: 0, color: '#166534', fontSize: 16 }}>
                      Hoàn Tất Xếp Lịch: {result.totalClassesScheduled} Lớp
                    </h4>
                    <span style={{ backgroundColor: '#22c55e', color: '#fff', padding: '2px 8px', borderRadius: 20, fontSize: 11, fontWeight: 700 }}>
                      {result.status}
                    </span>
                  </div>
                  <p style={{ margin: '4px 0 0 0', fontSize: 12, color: '#15803d' }}>{result.message}</p>
                </div>
                <div style={{ textAlign: 'right', fontSize: 12, color: '#166534' }}>
                  <div>Engine: <strong>{result.solverEngine}</strong></div>
                  <div>Thời gian suy luận: <strong>{result.solveTimeSeconds}s</strong></div>
                </div>
              </div>

              {/* Table of Scheduled Slots */}
              <div style={{ maxHeight: 260, overflowY: 'auto', border: '1px solid #bbf7d0', borderRadius: 8, backgroundColor: '#ffffff' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                  <thead>
                    <tr style={{ backgroundColor: '#dcfce7', color: '#166534', textAlign: 'left', borderBottom: '1px solid #bbf7d0' }}>
                      <th style={{ padding: '10px 12px' }}>Mã LHP</th>
                      <th style={{ padding: '10px 12px' }}>Tên Môn Học</th>
                      <th style={{ padding: '10px 12px' }}>Giảng Viên</th>
                      <th style={{ padding: '10px 12px' }}>Phòng Học</th>
                      <th style={{ padding: '10px 12px' }}>Thứ</th>
                      <th style={{ padding: '10px 12px' }}>Ca / Tiết</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.scheduledSlots.map((slot, idx) => (
                      <tr key={idx} style={{ borderBottom: '1px solid #f1f5f9', backgroundColor: idx % 2 === 0 ? '#ffffff' : '#f8fafc' }}>
                        <td style={{ padding: '10px 12px', fontWeight: 600, color: '#2563eb' }}>{slot.maLopHp}</td>
                        <td style={{ padding: '10px 12px', fontWeight: 500, color: '#1e293b' }}>{slot.tenMon}</td>
                        <td style={{ padding: '10px 12px', color: '#475569' }}>{slot.tenGiangVien || slot.maGv}</td>
                        <td style={{ padding: '10px 12px' }}>
                          <span style={{ backgroundColor: '#f1f5f9', padding: '3px 8px', borderRadius: 4, fontWeight: 600, color: '#334155', border: '1px solid #cbd5e1' }}>
                            {slot.phongHoc}
                          </span>
                        </td>
                        <td style={{ padding: '10px 12px', fontWeight: 700, color: '#0f172a' }}>{getDayName(slot.thuTrongTuan)}</td>
                        <td style={{ padding: '10px 12px', color: '#0369a1', fontWeight: 600 }}>{getPeriodName(slot.tietBatDau)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div
          style={{
            padding: '16px 24px',
            backgroundColor: '#f8fafc',
            borderTop: '1px solid #e2e8f0',
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 12,
          }}
        >
          <button
            onClick={onClose}
            style={{ padding: '10px 18px', borderRadius: 8, border: '1px solid #cbd5e1', backgroundColor: '#ffffff', color: '#475569', fontWeight: 600, cursor: 'pointer' }}
          >
            Đóng
          </button>
          {result && result.scheduledSlots && result.scheduledSlots.length > 0 && (
            <button
              onClick={() => {
                onApplySchedule(result.scheduledSlots);
                onClose();
              }}
              style={{
                padding: '10px 24px',
                borderRadius: 8,
                backgroundColor: '#16a34a',
                color: '#ffffff',
                border: 'none',
                fontWeight: 700,
                cursor: 'pointer',
                boxShadow: '0 4px 6px -1px rgba(22, 163, 74, 0.3)',
              }}
            >
              ✅ Áp Dụng Lịch Học Này Vào Thời Gian Biểu
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
