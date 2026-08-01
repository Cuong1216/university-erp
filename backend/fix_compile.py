import os

base_dir = r"d:\projects\helloworld\university_erp\backend\src\main\java\com\wiz\universityerpapi"

# 1. ChungChi.java
chung_chi_path = os.path.join(base_dir, "entity", "ChungChi.java")
with open(chung_chi_path, 'r', encoding='utf-8') as f:
    content = f.read()
if "import com.wiz.universityerpapi.schedule.infrastructure.entity.GiangVien;" not in content:
    content = content.replace("package com.wiz.universityerpapi.entity;", "package com.wiz.universityerpapi.entity;\n\nimport com.wiz.universityerpapi.schedule.infrastructure.entity.GiangVien;")
    with open(chung_chi_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 2. KetQuaHocTap.java
kht_path = os.path.join(base_dir, "entity", "KetQuaHocTap.java")
with open(kht_path, 'r', encoding='utf-8') as f:
    content = f.read()
if "import com.wiz.universityerpapi.schedule.infrastructure.entity.LopHocPhan;" not in content:
    content = content.replace("package com.wiz.universityerpapi.entity;", "package com.wiz.universityerpapi.entity;\n\nimport com.wiz.universityerpapi.schedule.infrastructure.entity.LopHocPhan;")
    with open(kht_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 3. MonHoc.java
mon_hoc_path = os.path.join(base_dir, "schedule", "infrastructure", "entity", "MonHoc.java")
with open(mon_hoc_path, 'r', encoding='utf-8') as f:
    content = f.read()
if "import com.wiz.universityerpapi.entity.BoMon;" not in content:
    content = content.replace("package com.wiz.universityerpapi.schedule.infrastructure.entity;", "package com.wiz.universityerpapi.schedule.infrastructure.entity;\n\nimport com.wiz.universityerpapi.entity.BoMon;")
    with open(mon_hoc_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 4. GiangVien.java
giang_vien_path = os.path.join(base_dir, "schedule", "infrastructure", "entity", "GiangVien.java")
with open(giang_vien_path, 'r', encoding='utf-8') as f:
    content = f.read()
if "import com.wiz.universityerpapi.entity.User;" not in content:
    imports = """
import com.wiz.universityerpapi.entity.User;
import com.wiz.universityerpapi.entity.BoMon;
import com.wiz.universityerpapi.entity.ChucDanh;
import com.wiz.universityerpapi.entity.HocVi;
"""
    content = content.replace("package com.wiz.universityerpapi.schedule.infrastructure.entity;", "package com.wiz.universityerpapi.schedule.infrastructure.entity;\n" + imports)
    with open(giang_vien_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 5. Global replacement for DTOs
for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                
            original = content
            content = content.replace("com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs", "com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs")
            
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated DTO import in {filepath}")

print("Fix applied.")
