import os
import shutil
import re

base_path = r"d:\projects\helloworld\university_erp\backend\src\main\java\com\wiz\universityerpapi"

# 1. Define target files and their new package
moved_files = {
    "service/SalaryCalculationDomainService.java": ("payroll/domain/service/SalaryCalculationDomainService.java", "com.wiz.universityerpapi.payroll.domain.service"),
    "controller/LuongController.java": ("payroll/presentation/controller/LuongController.java", "com.wiz.universityerpapi.payroll.presentation.controller"),
    "service/ILuongService.java": ("payroll/application/service/ILuongService.java", "com.wiz.universityerpapi.payroll.application.service"),
    "service/LuongService.java": ("payroll/application/service/LuongService.java", "com.wiz.universityerpapi.payroll.application.service"),
    "service/CauHinhLuongService.java": ("payroll/application/service/CauHinhLuongService.java", "com.wiz.universityerpapi.payroll.application.service"),
    "service/SalaryExportService.java": ("payroll/application/service/SalaryExportService.java", "com.wiz.universityerpapi.payroll.application.service"),
    "dto/MySalaryResponseDTO.java": ("payroll/application/dto/MySalaryResponseDTO.java", "com.wiz.universityerpapi.payroll.application.dto"),
    "dto/ChotLuongResponseDTO.java": ("payroll/application/dto/ChotLuongResponseDTO.java", "com.wiz.universityerpapi.payroll.application.dto"),
    "dto/ChotLuongRequestDTO.java": ("payroll/application/dto/ChotLuongRequestDTO.java", "com.wiz.universityerpapi.payroll.application.dto"),
    "dto/DepartmentSalaryDTO.java": ("payroll/application/dto/DepartmentSalaryDTO.java", "com.wiz.universityerpapi.payroll.application.dto"),
    "dto/MonthlySalaryTrendDTO.java": ("payroll/application/dto/MonthlySalaryTrendDTO.java", "com.wiz.universityerpapi.payroll.application.dto"),
    "dto/SalaryStatsResponseDTO.java": ("payroll/application/dto/SalaryStatsResponseDTO.java", "com.wiz.universityerpapi.payroll.application.dto"),
    "entity/CauHinhLuong.java": ("payroll/infrastructure/entity/CauHinhLuong.java", "com.wiz.universityerpapi.payroll.infrastructure.entity"),
    "entity/BangLuongThang.java": ("payroll/infrastructure/entity/BangLuongThang.java", "com.wiz.universityerpapi.payroll.infrastructure.entity"),
    "repository/CauHinhLuongRepository.java": ("payroll/infrastructure/repository/CauHinhLuongRepository.java", "com.wiz.universityerpapi.payroll.infrastructure.repository"),
    "repository/BangLuongThangRepository.java": ("payroll/infrastructure/repository/BangLuongThangRepository.java", "com.wiz.universityerpapi.payroll.infrastructure.repository"),
    "repository/projection/DepartmentSalaryView.java": ("payroll/infrastructure/repository/projection/DepartmentSalaryView.java", "com.wiz.universityerpapi.payroll.infrastructure.repository.projection"),
    "repository/projection/MonthlySalaryTrendView.java": ("payroll/infrastructure/repository/projection/MonthlySalaryTrendView.java", "com.wiz.universityerpapi.payroll.infrastructure.repository.projection")
}

# 2. Move files and update their package declarations
for old_rel_path, (new_rel_path, new_pkg) in moved_files.items():
    old_full_path = os.path.join(base_path, old_rel_path.replace("/", os.sep))
    new_full_path = os.path.join(base_path, new_rel_path.replace("/", os.sep))
    
    if not os.path.exists(old_full_path):
        print(f"Skipping {old_full_path} as it does not exist.")
        continue
    
    # Create directories
    os.makedirs(os.path.dirname(new_full_path), exist_ok=True)
    
    # Read content
    with open(old_full_path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Update package declaration
    content = re.sub(r'package\s+com\.wiz\.universityerpapi\.[a-zA-Z.]+;', f'package {new_pkg};', content)
    
    # Write to new path
    with open(new_full_path, "w", encoding="utf-8") as f:
        f.write(content)
    
    # Remove old file
    os.remove(old_full_path)
    print(f"Moved {old_rel_path} to {new_rel_path}")

# 3. Update imports across the whole project
class_moves = {}
for old_rel_path, (new_rel_path, new_pkg) in moved_files.items():
    class_name = old_rel_path.split("/")[-1].replace(".java", "")
    class_moves[class_name] = f"{new_pkg}.{class_name}"

roots = [r"d:\projects\helloworld\university_erp\backend\src\main\java", r"d:\projects\helloworld\university_erp\backend\src\test\java"]
for root_dir in roots:
    for dirpath, dirnames, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith(".java"):
                filepath = os.path.join(dirpath, filename)
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                
                new_content = content
                for class_name, fqn in class_moves.items():
                    # We might have `import com.wiz.universityerpapi.service.LuongService;`
                    new_content = re.sub(r'import\s+com\.wiz\.universityerpapi\.[a-zA-Z.]+\.' + class_name + r';', f'import {fqn};', new_content)
                
                if new_content != content:
                    with open(filepath, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Updated imports in {filepath}")

print("Refactoring complete.")
