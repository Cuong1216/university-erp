import os
import re
import subprocess

backend_dir = r'd:\projects\helloworld\university_erp\backend'
src_dir = os.path.join(backend_dir, 'src', 'main', 'java')
test_dir = os.path.join(backend_dir, 'src', 'test', 'java')

# 1. Map all classes to their full package names
class_to_pkg = {}
for root_dir in [src_dir, test_dir]:
    for root, dirs, files in os.walk(root_dir):
        for f in files:
            if f.endswith('.java'):
                filepath = os.path.join(root, f)
                with open(filepath, 'r', encoding='utf-8') as file:
                    content = file.read()
                    pkg_match = re.search(r'package\s+(.*?);', content)
                    if pkg_match:
                        class_name = f.replace('.java', '')
                        class_to_pkg[class_name] = pkg_match.group(1) + '.' + class_name
                        
                        # Handle inner classes if any (e.g., ScheduleOptimizationDTOs.ClassRequirementDTO)
                        if 'ScheduleOptimizationDTOs' in f:
                            class_to_pkg['ClassRequirementDTO'] = pkg_match.group(1) + '.ScheduleOptimizationDTOs.ClassRequirementDTO'
                            class_to_pkg['ScheduleOptimizationResponseDTO'] = pkg_match.group(1) + '.ScheduleOptimizationDTOs.ScheduleOptimizationResponseDTO'
                            class_to_pkg['ScheduleJobDTO'] = pkg_match.group(1) + '.ScheduleOptimizationDTOs.ScheduleJobDTO'

def add_import(filepath, class_fqn):
    with open(filepath, 'r', encoding='utf-8') as file:
        content = file.read()
    
    if f'import {class_fqn};' in content:
        return False
        
    # Find the last import
    lines = content.split('\n')
    last_import_idx = -1
    for i, line in enumerate(lines):
        if line.startswith('import '):
            last_import_idx = i
            
    if last_import_idx != -1:
        lines.insert(last_import_idx + 1, f'import {class_fqn};')
    else:
        # insert after package
        for i, line in enumerate(lines):
            if line.startswith('package '):
                lines.insert(i + 1, '')
                lines.insert(i + 2, f'import {class_fqn};')
                break
                
    with open(filepath, 'w', encoding='utf-8') as file:
        file.write('\n'.join(lines))
    return True

def remove_broken_imports(filepath):
    with open(filepath, 'r', encoding='utf-8') as file:
        lines = file.readlines()
        
    new_lines = []
    changed = False
    for line in lines:
        if 'import ' in line and ('does not exist' in line or 'package com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs' in line):
             # just a heuristic, better is to replace old bad imports
             pass
        
        # specifically fix ScheduleOptimizationDTOs
        if 'import com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs' in line:
            new_lines.append(line.replace('com.wiz.universityerpapi.dto.schedule.ScheduleOptimizationDTOs', 'com.wiz.universityerpapi.schedule.application.dto.ScheduleOptimizationDTOs'))
            changed = True
        else:
            new_lines.append(line)
            
    if changed:
        with open(filepath, 'w', encoding='utf-8') as file:
            file.writelines(new_lines)
            
def fix_compile():
    print("Running mvn compile...")
    result = subprocess.run(['mvnw.cmd', 'clean', 'compile'], cwd=backend_dir, capture_output=True, text=True)
    if result.returncode == 0:
        print("Compile success!")
        return True
        
    errors = result.stdout.split('\n')
    missing_symbols = []
    
    import_regex = re.compile(r'\[ERROR\]\s+(.*\.java):\[\d+,\d+\]\s+cannot\s+find\s+symbol')
    symbol_regex = re.compile(r'\[ERROR\]\s+symbol:\s+class\s+(\w+)')
    
    current_file = None
    for i, line in enumerate(errors):
        file_match = import_regex.search(line)
        if file_match:
            current_file = file_match.group(1)
        elif 'symbol:   class ' in line and current_file:
            sym_match = symbol_regex.search(line)
            if sym_match:
                symbol = sym_match.group(1)
                missing_symbols.append((current_file, symbol))
            current_file = None
            
    fixed = 0
    for filepath, symbol in set(missing_symbols):
        if symbol in class_to_pkg:
            if add_import(filepath, class_to_pkg[symbol]):
                print(f"Fixed missing {symbol} in {filepath}")
                fixed += 1
        else:
            print(f"Could not find package for {symbol}")
            
    # Also fix explicit package missing
    for root, dirs, files in os.walk(src_dir):
        for f in files:
            if f.endswith('.java'):
                remove_broken_imports(os.path.join(root, f))
                
    return fixed == 0

for _ in range(5):
    if fix_compile():
        break
