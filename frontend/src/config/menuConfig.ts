import type { RoleType } from '../types/auth.types';

export interface MenuItem {
  key: string;
  label: string;
  path: string;
  icon?: string;              // Tên icon hoặc định danh icon
  allowedRoles?: RoleType[];  // Không truyền -> Ai đăng nhập cũng thấy
  children?: MenuItem[];      // Menu con
}

export const SIDEBAR_MENU_CONFIG: MenuItem[] = [
  {
    key: 'dashboard',
    label: 'Tổng quan',
    path: '/dashboard',
    icon: 'LayoutDashboard',
    // Không có allowedRoles -> tất cả mọi role đều truy cập được
  },
  {
    key: 'student_grades',
    label: 'Xem Điểm & Kết Quả Học Tập',
    path: '/student/grades',
    icon: 'Award',
    allowedRoles: ['ROLE_SINH_VIEN'],
  },
  {
    key: 'grading',
    label: 'Nhập điểm học phần',
    path: '/grading',
    icon: 'PenTool',
    allowedRoles: ['ROLE_GIANG_VIEN', 'ROLE_GIAO_VU'],
  },
  {
    key: 'teacher_salary',
    label: 'Thù lao & Nhật ký giảng dạy',
    path: '/teacher/salary',
    icon: 'FileText',
    allowedRoles: ['ROLE_GIANG_VIEN'],
  },
  {
    key: 'schedule',
    label: 'Xem lịch học / Lịch thi',
    path: '/schedule',
    icon: 'Calendar',
    allowedRoles: ['ROLE_SINH_VIEN', 'ROLE_GIANG_VIEN', 'ROLE_GIAO_VU'],
  },
  {
    key: 'tuition',
    label: 'Tra cứu & Nộp học phí VNPay',
    path: '/tuition',
    icon: 'CreditCard',
    allowedRoles: ['ROLE_SINH_VIEN', 'ROLE_ADMIN', 'ROLE_GIAO_VU'],
  },

  {
    key: 'academic_salary_management',
    label: 'Quản lý thù lao & Xuất Excel',
    path: '/academic/salary-management',
    icon: 'Briefcase',
    allowedRoles: ['ROLE_GIAO_VU', 'ROLE_ADMIN'],
  },
  {
    key: 'salary_config',
    label: 'Cấu hình lương & Định mức',
    path: '/admin/salary-config',
    icon: 'DollarSign',
    allowedRoles: ['ROLE_ADMIN'],
  },
  {
    key: 'user_management',
    label: 'Quản lý Người dùng & Quyền',
    path: '/admin/users',
    icon: 'Users',
    allowedRoles: ['ROLE_ADMIN'],
    children: [
      {
        key: 'users_list',
        label: 'Danh sách tài khoản',
        path: '/admin/users/list',
        allowedRoles: ['ROLE_ADMIN'],
      },
      {
        key: 'roles_list',
        label: 'Phân quyền hệ thống',
        path: '/admin/users/roles',
        allowedRoles: ['ROLE_ADMIN'],
      },
    ],
  },
];

/**
 * Hàm lọc danh sách Menu theo Role hiện tại của User (hỗ trợ menu lồng nhau)
 */
export const filterMenuItemsByRoles = (menuItems: MenuItem[], userRoles: RoleType[]): MenuItem[] => {
  return menuItems
    .filter((item) => {
      // Nếu không yêu cầu role cụ thể nào -> hiển thị
      if (!item.allowedRoles || item.allowedRoles.length === 0) return true;
      // Nếu có yêu cầu -> kiểm tra user có ít nhất 1 role khớp không
      return item.allowedRoles.some((role) => userRoles.includes(role));
    })
    .map((item) => {
      // Nếu có menu con -> tiếp tục lọc đệ quy
      if (item.children) {
        return {
          ...item,
          children: filterMenuItemsByRoles(item.children, userRoles),
        };
      }
      return item;
    })
    .filter((item) => {
      // Nếu sau khi lọc menu con mà menu cha không có link và cũng hết menu con -> ẩn menu cha
      if (item.children && item.children.length === 0 && !item.path) {
        return false;
      }
      return true;
    });
};
