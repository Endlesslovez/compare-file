package com.example.demo.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelService {

    /**
     * Compares two Excel files and returns the result.
     *
     * @param file1 the first Excel file
     * @param file2 the second Excel file
     * @return a string indicating the result of the comparison
     */
    List<String> compareExcelFiles(MultipartFile file1, MultipartFile file2, HttpServletResponse response, int rowIgnore, int columnIgnore);

    void downloadExcelFile(HttpServletResponse response);
}
