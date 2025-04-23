package com.example.demo.service.impl;


import static com.example.demo.utils.Constants.TILDE_SYMBOL;

import com.example.demo.dto.FindCompareDto;
import com.example.demo.dto.config.KeyResult;
import com.example.demo.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellBase;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of the ExcelService interface for compiling Excel files.
 */
@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

  private final KeyResult keyResult;

  @Override
  @SneakyThrows
  public List<String> compareExcelFiles(MultipartFile file1, MultipartFile file2,
      HttpServletResponse response,
      int rowIgnore, int columnIgnore) {
    var executor = Executors.newFixedThreadPool(5);
    Map<Integer, FindCompareDto> errorMaps = new HashMap<>();
    log.info("FileName1: [{}]", file1.getOriginalFilename());
    log.info("FileName2: [{}]", file2.getOriginalFilename());

    try (Workbook workbook1 = new XSSFWorkbook(
        file1.getInputStream()); Workbook workbook2 = new XSSFWorkbook(file2.getInputStream())) {
      Sheet sheet1 = workbook1.getSheetAt(0);
      Sheet sheet2 = workbook2.getSheetAt(0);

      int rowCountSheet1 = sheet1.getPhysicalNumberOfRows();
      int rowCountSheet2 = sheet2.getPhysicalNumberOfRows();

      if (rowCountSheet1 != rowCountSheet2) {
        log.error("Row count of two sheets are not equal");
        return List.of("Row_Not_Equals");
      }
//      Set<Callable<Map<Integer, String>>> callables = new HashSet<>();
//      callables.add(() -> getAllDataSheet(sheet1, rowIgnore, columnIgnore));
//      callables.add(() -> getAllDataSheet(sheet2, rowIgnore, columnIgnore));
//      List<Future<Map<Integer, String>>> futureList = executor.invokeAll(callables);

      Map<Integer, String> valSheet1 = executor.submit(() -> getAllDataSheet(sheet1, rowIgnore, columnIgnore)).get();
      Map<Integer, String> valSheet2 = executor.submit(() -> getAllDataSheet(sheet2, rowIgnore, columnIgnore)).get();
//      Map<Integer, String> valSheet1 = futureList.get(0).get();
//      Map<Integer, String> valSheet2 = futureList.get(1).get();
      log.info("Total element sheet1: [{}]", valSheet1.size());
      log.info("Total element sheet2: [{}]", valSheet2.size());

      valSheet2.forEach((k, v) -> {
        if (valSheet1.containsKey(k)) {
          String val1 = valSheet1.get(k);
          if (!val1.equals(v)) {
            errorMaps.put(k, FindCompareDto.builder().valSource(val1).valCompare(v).build());
          }
        }
      });
      if (!CollectionUtils.isEmpty(errorMaps)) {
        Iterator<Row> rowIterator = sheet1.rowIterator();
        for (int i = 0; i < rowIgnore; i++) {
          rowIterator.next();
        }

        Row rowTitle = sheet1.getRow(rowIgnore - 1);
        Iterator<Cell> cellIteratorTitle = rowTitle.cellIterator();
        int lastCellNumTitle = rowTitle.getLastCellNum();

        List<String> titleList = new ArrayList<>();
        while (cellIteratorTitle.hasNext()) {
          titleList.add(cellIteratorTitle.next().getStringCellValue());
        }
        for (String s : titleList) {
          rowTitle.createCell(lastCellNumTitle++).setCellValue(s);
        }
        // set Cell font color error
        CellStyle cellStyleColor = workbook1.createCellStyle();
        Font font = workbook1.createFont();
        font.setColor(HSSFColorPredefined.RED.getIndex());
        cellStyleColor.setFont(font);

        // set color for background
        CellStyle cellStyleBackground = workbook1.createCellStyle();
        cellStyleBackground.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
        cellStyleBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyleBackground.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

        while (rowIterator.hasNext()) {
          Row row = rowIterator.next();
          int cellCount = row.getLastCellNum();
          log.info("Cell count [{}]", cellCount);
          log.info("Content Error: [{}]", errorMaps.get(row.getRowNum()));
          FindCompareDto errorCompare = errorMaps.get(row.getRowNum());
          if (errorCompare == null) {
            continue;
          }
          Iterator<Cell> cellIterator = row.cellIterator();
          while (cellIterator.hasNext()) {
            cellIterator.next().setCellStyle(cellStyleBackground);
          }
          Map<Integer, String> valueError = findTextError(errorCompare.getValSource(),
              errorCompare.getValCompare());
          if (CollectionUtils.isEmpty(valueError)) {
            continue;
          }

          valueError.forEach((k, v) -> {
            Cell cell = row.createCell(cellCount + k);
            if (cell != null) {
              cell.setCellValue(v);
              cell.setCellStyle(cellStyleColor);
            }
          });
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        response.setContentType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
            "attachment; filename=" + "Thong_Tin_Loi_"+ LocalDate.now().format(dateTimeFormatter) + ".xlsx");
        try (OutputStream out = response.getOutputStream()) {
          workbook1.write(out);
        } catch (IOException e) {
          log.info("Error writing Excel file: {}", e.getMessage());
          throw e;
        }
      } else {
        return List.of("Success");
      }
    } catch (Exception e) {
      log.info("[compareExcelFiles] Exception when read compare file: {}", e.getMessage());
      throw e;
    } finally {
      executor.shutdown();
      while (!executor.isTerminated()) {
        log.info("Closed Executor...");
      }

    }

    return null;
  }

  private Map<Integer, String> findTextError(String valSource, String valCompare) {
    Map<Integer, String> result = new HashMap<>();
    String[] source = valSource.split(TILDE_SYMBOL);
    String[] compare = valCompare.split(TILDE_SYMBOL);
    for (int i = 0; i < source.length; i++) {
      if (!source[i].equals(compare[i])) {
        result.put(i, compare[i]);
      }
    }
    return result;
  }

  private Map<Integer, String> getAllDataSheet(Sheet sheet, int rowIgnore, int columnIgnore) {
    log.info("Thread name: [{}]", Thread.currentThread().getName());
    Map<Integer, String> result = new HashMap<>();
    Iterator<Row> rowIterator = sheet.rowIterator();

    for (int i = 0; i < rowIgnore; i++) {
      rowIterator.next();
    }
    while (rowIterator.hasNext()) {
      Row row = rowIterator.next();
      Iterator<Cell> cellIterator = row.cellIterator();
      StringBuilder valRow = new StringBuilder();
      for (int i = 0; i < columnIgnore; i++) {
        cellIterator.next();
      }
      while (cellIterator.hasNext()) {
        Cell cell = cellIterator.next();
        log.info("Row: [{}] cell [{}]", row.getRowNum(), cell.getColumnIndex());
        ((CellBase) cell).setCellType(CellType.STRING);
        valRow.append(cell.getStringCellValue()).append(TILDE_SYMBOL);
      }
      result.put(row.getRowNum(), valRow.toString());
    }
    return result;
  }


  @Override
  @SneakyThrows
  public void downloadExcelFile(HttpServletResponse response) {
    Workbook workbook = new XSSFWorkbook();

    try (workbook; OutputStream out = response.getOutputStream()) {
      workbook.createSheet("Sheet1");
      response.setContentType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader("Content-Disposition", "attachment; filename=error.xlsx");
      workbook.write(out);
    } catch (IOException e) {
      log.error("[downloadExcelFile] Error writing Excel file: {}", e.getMessage());
      throw e;
    }
  }
}
