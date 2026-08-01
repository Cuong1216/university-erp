import os
import shutil

base_dir = r"d:\projects\helloworld\university_erp\backend\src\main\java\com\wiz\universityerpapi"

# Format: (Original Relative Path, New Relative Path, Old Package, New Package)
files_to_move = [
    # Controllers
    ("controller/LichHocController.java", "schedule/presentation/controller/LichHocController.java", "com.wiz.universityerpapi.controller", "com.wiz.universityerpapi.schedule.presentation.controller"),
    ("controller/ScheduleOptimizationController.java", "schedule/presentation/controller/ScheduleOptimizationController.java", "com.wiz.universityerpapi.controller", "com.wiz.universityerpapi.schedule.presentation.controller"),

    # Services
    ("service/LichHocService.java", "schedule/application/service/LichHocService.java", "com.wiz.universityerpapi.service", "com.wiz.universityerpapi.schedule.application.service"),
    ("service/ScheduleOptimizationService.java", "schedule/application/service/ScheduleOptimizationService.java", "com.wiz.universityerpapi.service", "com.wiz.universityerpapi.schedule.application.service"),
    ("service/ScheduleJobConsumer.java", "schedule/application/service/ScheduleJobConsumer.java", "com.wiz.universityerpapi.service", "com.wiz.universityerpapi.schedule.application.service"),
    ("service/schedule/CpSatSchedulerEngine.java", "schedule/application/service/CpSatSchedulerEngine.java", "com.wiz.universityerpapi.service.schedule", "com.wiz.universityerpapi.schedule.application.service"),
    ("service/schedule/GreedySchedulerEngine.java", "schedule/application/service/GreedySchedulerEngine.java", "com.wiz.universityerpapi.service.schedule", "com.wiz.universityerpapi.schedule.application.service"),
    ("service/schedule/ISchedulerEngine.java", "schedule/application/service/ISchedulerEngine.java", "com.wiz.universityerpapi.service.schedule", "com.wiz.universityerpapi.schedule.application.service"),

    # DTOs
    ("dto/LichHocResponseDTO.java", "schedule/application/dto/LichHocResponseDTO.java", "com.wiz.universityerpapi.dto", "com.wiz.universityerpapi.schedule.application.dto"),
    ("dto/TaoLichRequestDTO.java", "schedule/application/dto/TaoLichRequestDTO.java", "com.wiz.universityerpapi.dto", "com.wiz.universityerpapi.schedule.application.dto"),
    ("dto/schedule/ScheduleOptimizationDTOs.java", "schedule/application/dto/ScheduleOptimizationDTOs.java", "com.wiz.universityerpapi.dto.schedule", "com.wiz.universityerpapi.schedule.application.dto"),

    # Repositories
    ("repository/LichHocChiTietRepository.java", "schedule/infrastructure/repository/LichHocChiTietRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    ("repository/TuanHocChiTietRepository.java", "schedule/infrastructure/repository/TuanHocChiTietRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    ("repository/GiangVienRepository.java", "schedule/infrastructure/repository/GiangVienRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    ("repository/PhanCongDayRepository.java", "schedule/infrastructure/repository/PhanCongDayRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    ("repository/NhatKyGiangDayRepository.java", "schedule/infrastructure/repository/NhatKyGiangDayRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    ("repository/LopHocPhanRepository.java", "schedule/infrastructure/repository/LopHocPhanRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    ("repository/MonHocRepository.java", "schedule/infrastructure/repository/MonHocRepository.java", "com.wiz.universityerpapi.repository", "com.wiz.universityerpapi.schedule.infrastructure.repository"),
    
    # Projections
    ("repository/projection/LecturerConflictView.java", "schedule/infrastructure/repository/projection/LecturerConflictView.java", "com.wiz.universityerpapi.repository.projection", "com.wiz.universityerpapi.schedule.infrastructure.repository.projection"),
    ("repository/projection/GiangVienHeSoView.java", "schedule/infrastructure/repository/projection/GiangVienHeSoView.java", "com.wiz.universityerpapi.repository.projection", "com.wiz.universityerpapi.schedule.infrastructure.repository.projection"),
    ("repository/projection/GiangVienProjection.java", "schedule/infrastructure/repository/projection/GiangVienProjection.java", "com.wiz.universityerpapi.repository.projection", "com.wiz.universityerpapi.schedule.infrastructure.repository.projection"),

    # Entities
    ("entity/LichHocChiTiet.java", "schedule/infrastructure/entity/LichHocChiTiet.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
    ("entity/TuanHocChiTiet.java", "schedule/infrastructure/entity/TuanHocChiTiet.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
    ("entity/GiangVien.java", "schedule/infrastructure/entity/GiangVien.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
    ("entity/PhanCongDay.java", "schedule/infrastructure/entity/PhanCongDay.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
    ("entity/NhatKyGiangDay.java", "schedule/infrastructure/entity/NhatKyGiangDay.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
    ("entity/LopHocPhan.java", "schedule/infrastructure/entity/LopHocPhan.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
    ("entity/MonHoc.java", "schedule/infrastructure/entity/MonHoc.java", "com.wiz.universityerpapi.entity", "com.wiz.universityerpapi.schedule.infrastructure.entity"),
]

import_replacements = {}
for item in files_to_move:
    filename = os.path.basename(item[0])
    class_name = filename.replace(".java", "")
    old_full_class = f"{item[2]}.{class_name}"
    new_full_class = f"{item[3]}.{class_name}"
    import_replacements[old_full_class] = new_full_class
    # Also add wildcard replacement if applicable, but explicit is safer
    
print("Moving files and updating packages...")
for old_rel, new_rel, old_pkg, new_pkg in files_to_move:
    old_path = os.path.join(base_dir, old_rel.replace('/', os.sep))
    new_path = os.path.join(base_dir, new_rel.replace('/', os.sep))
    
    if os.path.exists(old_path):
        os.makedirs(os.path.dirname(new_path), exist_ok=True)
        
        # Read content, update package
        with open(old_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        content = content.replace(f"package {old_pkg};", f"package {new_pkg};")
        
        # Write to new path
        with open(new_path, 'w', encoding='utf-8') as f:
            f.write(content)
            
        # Remove old file
        os.remove(old_path)
        print(f"Moved {old_rel} -> {new_rel}")
    else:
        print(f"WARNING: File not found: {old_path}")

print("\nUpdating imports globally...")
for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                
            original_content = content
            for old_class, new_class in import_replacements.items():
                content = content.replace(f"import {old_class};", f"import {new_class};")
                # Handle potential wildcard imports? 
                # Our previous explicit list should cover all classes
            
            # Special case for DTO static imports if any (e.g. ScheduleOptimizationDTOs.*)
            # Actually, import_replacements handles specific classes. Let's add wildcards just in case.
            # But the explicit class names should be enough for most cases.
            
            # Hande ScheduleOptimizationDTOs inner classes
            content = content.replace("import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs.*;", "import com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs.*;")
            
            if content != original_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated imports in: {filepath}")

# Remove empty directories
print("\nCleaning up empty directories...")
for root, dirs, files in os.walk(base_dir, topdown=False):
    for dir in dirs:
        dir_path = os.path.join(root, dir)
        try:
            if not os.listdir(dir_path):
                os.rmdir(dir_path)
                print(f"Removed empty directory: {dir_path}")
        except Exception as e:
            pass

print("Done!")
