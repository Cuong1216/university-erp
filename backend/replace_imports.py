import os

target_dir = r"d:\projects\helloworld\university_erp\backend\src\main\java"

replacements = {
    "com.wiz.universityerpapi.exception": "com.wiz.universityerpapi.core.exception",
    "com.wiz.universityerpapi.config": "com.wiz.universityerpapi.core.config",
    "com.wiz.universityerpapi.security": "com.wiz.universityerpapi.core.security",
    "com.wiz.universityerpapi.util": "com.wiz.universityerpapi.core.util"
}

for root, _, files in os.walk(target_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            for old, new in replacements.items():
                content = content.replace(old, new)
                
            if content != original_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated {filepath}")
