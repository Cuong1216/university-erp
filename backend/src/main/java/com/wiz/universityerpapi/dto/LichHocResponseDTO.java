package com.wiz.universityerpapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LichHocResponseDTO {
    private String maLich;
    private String maLopHp;
    private String phongHoc;
    private Integer thuTrongTuan;
    private Integer tietBatDau;
    private Integer tietKetThuc;
    private List<Integer> danhSachTuan;
}
