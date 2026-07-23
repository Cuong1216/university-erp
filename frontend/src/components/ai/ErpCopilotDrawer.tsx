import React, { useState, useRef, useEffect } from 'react';
import { useAiChatStore } from '../../store/useAiChatStore';
import { useAuthStore } from '../../store/useAuthStore';

const QUICK_PROMPTS = [
  'Ai dạy nhiều tiết nhất tháng này?',
  'Tổng chi phí lương theo Khoa tháng 6/2026?',
  'So sánh lương bình quân Khoa CNTT và Kinh Tế',
  'Danh sách giảng viên có học vị Tiến sĩ',
];

export const ErpCopilotDrawer: React.FC = () => {
  const { roles } = useAuthStore();
  const { isOpen, loading, messages, toggleDrawer, closeDrawer, sendMessage, clearMessages } =
    useAiChatStore();
  const [inputText, setInputText] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Chỉ hiển thị Copilot cho Admin và Giao Vụ
  const canAccessCopilot = roles.some((r) => r === 'ROLE_ADMIN' || r === 'ROLE_GIAO_VU');

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  if (!canAccessCopilot) {
    return null;
  }

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputText.trim() || loading) return;
    const textToSend = inputText;
    setInputText('');
    await sendMessage(textToSend);
  };

  const handleQuickPrompt = async (promptText: string) => {
    if (loading) return;
    await sendMessage(promptText);
  };

  return (
    <>
      {/* Nút Floating Button góc phải dưới màn hình */}
      {!isOpen && (
        <button
          onClick={toggleDrawer}
          style={{
            position: 'fixed',
            bottom: 24,
            right: 24,
            zIndex: 1000,
            padding: '12px 20px',
            borderRadius: '50px',
            backgroundColor: '#1e3a8a',
            color: '#ffffff',
            border: 'none',
            boxShadow: '0 10px 25px -5px rgba(30, 58, 138, 0.4), 0 8px 10px -6px rgba(30, 58, 138, 0.3)',
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: 15,
            transition: 'all 0.2s ease',
          }}
          onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.05)')}
          onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1)')}
        >
          <span style={{ fontSize: 20 }}>🤖</span>
          <span>ERP Copilot AI</span>
        </button>
      )}

      {/* Backdrop overlay khi mở drawer */}
      {isOpen && (
        <div
          onClick={closeDrawer}
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(15, 23, 42, 0.3)',
            backdropFilter: 'blur(2px)',
            zIndex: 1001,
          }}
        />
      )}

      {/* Drawer Container */}
      <div
        style={{
          position: 'fixed',
          top: 0,
          right: isOpen ? 0 : '-480px',
          width: '450px',
          maxWidth: '90vw',
          height: '100vh',
          backgroundColor: '#ffffff',
          boxShadow: '-10px 0 30px rgba(0, 0, 0, 0.15)',
          zIndex: 1002,
          display: 'flex',
          flexDirection: 'column',
          transition: 'right 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
        }}
      >
        {/* Header */}
        <div
          style={{
            padding: '16px 20px',
            backgroundColor: '#1e3a8a',
            color: '#ffffff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span style={{ fontSize: 24 }}>🤖</span>
            <div>
              <h3 style={{ margin: 0, fontSize: 17, fontWeight: 600 }}>ERP Copilot Assistant</h3>
              <span style={{ fontSize: 12, color: '#93c5fd', display: 'flex', alignItems: 'center', gap: 6 }}>
                <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#22c55e', display: 'inline-block' }} />
                Text-to-SQL (Read-Only DB Isolated)
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            <button
              onClick={clearMessages}
              title="Xóa lịch sử trò chuyện"
              style={{
                background: 'rgba(255, 255, 255, 0.15)',
                border: 'none',
                color: '#ffffff',
                padding: '6px 10px',
                borderRadius: 6,
                cursor: 'pointer',
                fontSize: 12,
              }}
            >
              Làm mới
            </button>
            <button
              onClick={closeDrawer}
              style={{
                background: 'transparent',
                border: 'none',
                color: '#ffffff',
                fontSize: 20,
                cursor: 'pointer',
                padding: '4px 8px',
              }}
            >
              ✕
            </button>
          </div>
        </div>

        {/* Messages Body */}
        <div
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: '16px',
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
            backgroundColor: '#f8fafc',
          }}
        >
          {messages.map((msg) => (
            <div
              key={msg.id}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: msg.sender === 'user' ? 'flex-end' : 'flex-start',
              }}
            >
              <div
                style={{
                  maxWidth: '85%',
                  padding: '12px 16px',
                  borderRadius: msg.sender === 'user' ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                  backgroundColor: msg.sender === 'user' ? '#3b82f6' : '#ffffff',
                  color: msg.sender === 'user' ? '#ffffff' : '#1e293b',
                  boxShadow: '0 2px 5px rgba(0,0,0,0.05)',
                  border: msg.sender === 'bot' ? '1px solid #e2e8f0' : 'none',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word',
                  fontSize: 14,
                  lineHeight: 1.5,
                }}
              >
                {msg.text}
              </div>
              <span style={{ fontSize: 11, color: '#94a3b8', marginTop: 4, padding: '0 4px' }}>
                {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          ))}

          {loading && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 14px', backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #e2e8f0', width: 'fit-content' }}>
              <span style={{ fontSize: 16 }}>⚡</span>
              <span style={{ fontSize: 13, color: '#64748b' }}>Copilot đang suy luận và truy vấn cơ sở dữ liệu...</span>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Quick Prompts Chips */}
        <div
          style={{
            padding: '10px 16px',
            backgroundColor: '#f1f5f9',
            borderTop: '1px solid #e2e8f0',
            display: 'flex',
            gap: 6,
            overflowX: 'auto',
            whiteSpace: 'nowrap',
          }}
        >
          <span style={{ fontSize: 12, color: '#64748b', alignSelf: 'center', fontWeight: 600 }}>💡 Gợi ý:</span>
          {QUICK_PROMPTS.map((prompt, idx) => (
            <button
              key={idx}
              disabled={loading}
              onClick={() => handleQuickPrompt(prompt)}
              style={{
                padding: '6px 12px',
                borderRadius: '20px',
                border: '1px solid #cbd5e1',
                backgroundColor: '#ffffff',
                color: '#334155',
                fontSize: 12,
                cursor: loading ? 'not-allowed' : 'pointer',
                transition: 'all 0.15s',
              }}
              onMouseOver={(e) => (!loading ? (e.currentTarget.style.borderColor = '#3b82f6') : null)}
              onMouseOut={(e) => (!loading ? (e.currentTarget.style.borderColor = '#cbd5e1') : null)}
            >
              {prompt}
            </button>
          ))}
        </div>

        {/* Input Area */}
        <form
          onSubmit={handleSend}
          style={{
            padding: '16px',
            backgroundColor: '#ffffff',
            borderTop: '1px solid #e2e8f0',
            display: 'flex',
            gap: 10,
          }}
        >
          <input
            type="text"
            placeholder="Hỏi dữ liệu DB bằng Tiếng Việt (VD: Ai dạy nhiều tiết nhất?)..."
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            disabled={loading}
            style={{
              flex: 1,
              padding: '10px 14px',
              borderRadius: '8px',
              border: '1px solid #cbd5e1',
              fontSize: 14,
              outline: 'none',
            }}
          />
          <button
            type="submit"
            disabled={!inputText.trim() || loading}
            style={{
              padding: '10px 18px',
              borderRadius: '8px',
              backgroundColor: !inputText.trim() || loading ? '#94a3b8' : '#1e3a8a',
              color: '#ffffff',
              border: 'none',
              fontWeight: 600,
              cursor: !inputText.trim() || loading ? 'not-allowed' : 'pointer',
              transition: 'background-color 0.2s',
            }}
          >
            Gửi
          </button>
        </form>
      </div>
    </>
  );
};
