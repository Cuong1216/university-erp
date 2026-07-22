package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.entity.BangLuongThang;
import com.wiz.universityerpapi.repository.BangLuongThangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryExportService {

    private final BangLuongThangRepository bangLuongThangRepository;

    public byte[] exportBangLuongThangExcel(int thang, int nam) throws IOException {
        List<BangLuongThang> bangLuongList = bangLuongThangRepository.findByThangAndNam(thang, nam);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Bang_Luong_T" + thang + "_" + nam);

            // Header style: in đậm, nền xám
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Data styles
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            DataFormat dataFormat = workbook.createDataFormat();
            currencyStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));

            // Tạo Header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Mã GV", "Tổng tiết", "Đơn giá (Snapshot)", "Thành tiền"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Tạo Data rows
            int rowNum = 1;
            for (BangLuongThang bl : bangLuongList) {
                Row row = sheet.createRow(rowNum++);

                // Mã GV
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(bl.getMaGv() != null ? bl.getMaGv() : "");
                cell0.setCellStyle(dataStyle);

                // Tổng tiết
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(bl.getTongSoTietThucTe() != null ? bl.getTongSoTietThucTe() : 0);
                cell1.setCellStyle(dataStyle);

                // Đơn giá (Snapshot)
                Cell cell2 = row.createCell(2);
                if (bl.getDonGiaTietSnapshot() != null) {
                    cell2.setCellValue(bl.getDonGiaTietSnapshot().doubleValue());
                } else {
                    cell2.setCellValue(0.0);
                }
                cell2.setCellStyle(currencyStyle);

                // Thành tiền
                Cell cell3 = row.createCell(3);
                if (bl.getTongTienLuong() != null) {
                    cell3.setCellValue(bl.getTongTienLuong().doubleValue());
                } else {
                    cell3.setCellValue(0.0);
                }
                cell3.setCellStyle(currencyStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
