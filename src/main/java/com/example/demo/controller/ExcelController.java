package com.example.demo.controller;

import com.example.demo.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

  private final ExcelService excelService;

  @GetMapping("/get-compare")
  public String showCompareForm() {
    try {
      String username = SecurityContextHolder.getContext().getAuthentication().getName();
      if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
        return "redirect:/user/login";
      }
      return "compare";
    } catch (Exception e) {
      log.info("Error getting username: {}", e.getMessage());
    }
    return "redirect:/user/login";
  }

  @PostMapping("/compare")
  public String compareExcelFiles(@RequestPart("fileResource") MultipartFile fileResource,
      @RequestPart("fileCompare") MultipartFile fileCompare,
      @RequestParam("ignore-row") String ignoreRow,
      @RequestParam("ignore-column") String ignoreColumn,
      Model model, HttpServletResponse response) {
    log.info("Ignore row: {}, Ignore column: {}", ignoreRow, ignoreColumn);
    int rowIgnoreInt = 0;
    int columnIgnoreInt = 0;
    if(ignoreRow != null && !ignoreRow.isEmpty()) {
      rowIgnoreInt = Integer.parseInt(ignoreRow);
    }
    if(ignoreColumn != null && !ignoreColumn.isEmpty()) {
      columnIgnoreInt = Integer.parseInt(ignoreColumn);
    }
    List<String> result = excelService.compareExcelFiles(fileResource, fileCompare, response,
        rowIgnoreInt, columnIgnoreInt);
    model.addAttribute("result", result);
    return "compare";

  }
}