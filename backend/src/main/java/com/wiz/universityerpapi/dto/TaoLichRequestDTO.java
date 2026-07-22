package com.wiz.universityerpapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaoLichRequestDTO {
    private String maLich;

    @NotBlank(message = "Mã lớp học phần không được để trống")
    private String maLopHp;

    @NotBlank(message = "Phòng học không được để trống")
    private String phongHoc;

    @NotNull(message = "Thứ trong tuần không được để trống")
    @Min(value = 2, message = "Thứ trong tuần phải từ 2 đến 8")
    @Max(value = 8, message = "Thứ trong tuần phải từ 2 đến 8")
    private Integer thuTrongTuan;

    @NotNull(message = "Tiết bắt đầu không được để trống")
    @Min(value = 1, message = "Tiết bắt đầu phải lớn hơn hoặc bằng 1")
    private Integer tietBatDau;

    @NotNull(message = "Tiết kết thúc không được để trống")
    private Integer tietKetThuc;

    private Integer tuanThu;
    private List<Integer> danhSachTuan;

    private String maGv;
}
