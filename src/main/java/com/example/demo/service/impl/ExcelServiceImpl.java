package com.example.demo.service.impl;


import static com.example.demo.utils.Constants.TILDE_SYMBOL;

import com.aspose.pdf.AbsorbedCell;
import com.aspose.pdf.AbsorbedRow;
import com.aspose.pdf.AbsorbedTable;
import com.aspose.pdf.Document;
import com.aspose.pdf.TableAbsorber;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextFragmentCollection;
import com.example.demo.controller.LoginController;
import com.example.demo.dto.FindCompareDto;
import com.example.demo.dto.config.KeyResult;
import com.example.demo.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
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
import org.apache.poi.ss.util.CellRangeAddress;
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
  private final LoginController loginController;

  @Override
  @SneakyThrows
  public List<String> compareExcelFiles(MultipartFile fileExcel, MultipartFile filePdf,
      HttpServletResponse response,
      int rowIgnore, int columnIgnore) {
   boolean isValidateExtension =  validateFileExtension(fileExcel, filePdf);
   if(isValidateExtension){
     return List.of("FILE_EXTENSION_NOT_FOUND");
   }
   boolean isMaxSize = validateSize(fileExcel, filePdf);
   if(isMaxSize){
     return List.of("FILE_MAX_SIZE");
   }
    var executor = Executors.newFixedThreadPool(5);
    Map<Integer, FindCompareDto> errorMaps = new HashMap<>();
    log.info("FileName1: [{}]", fileExcel.getOriginalFilename());
    log.info("FileName2: [{}]", filePdf.getOriginalFilename());

    try (Workbook workbookExcel = new XSSFWorkbook(
        fileExcel.getInputStream())) {
      int numOfSheet = workbookExcel.getNumberOfSheets();
      Sheet sheet1 = workbookExcel.getSheetAt(0);

      Map<Integer, String> resultDataExCel = executor.submit(
          () -> getAllDataSheet(sheet1, rowIgnore, columnIgnore)).get();
      List<String> resultDataPdf = executor.submit(() -> handlePdf(filePdf, numOfSheet)).get();
      resultDataPdf.removeLast();

      AtomicInteger countInx = new AtomicInteger(1);
      resultDataExCel.forEach((k, v) -> {
        if (resultDataPdf.size() > countInx.get()) {
          String elementPdf = resultDataPdf.get(countInx.get());
          if (!elementPdf.equals(v)) {
            errorMaps.put(k,
                FindCompareDto.builder().valCorrect(elementPdf).valIncorrect(v).build());
          }
          countInx.getAndIncrement();
        }
      });
      log.info("Total element sheet1: [{}]", resultDataExCel.size());

      if (!CollectionUtils.isEmpty(errorMaps)) {
        handleException(errorMaps, sheet1, workbookExcel, response, rowIgnore, fileExcel.getOriginalFilename());
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

  private boolean validateFileExtension(MultipartFile fileExcel, MultipartFile filePdf) {
    String extensionPdf = FileNameUtils.getExtension(filePdf.getOriginalFilename());
    String extensionXlsx = FileNameUtils.getExtension(fileExcel.getOriginalFilename());
      return !".pdf".equalsIgnoreCase(extensionPdf) || !".xlsx".equalsIgnoreCase(extensionXlsx);
  }

  private boolean validateSize(MultipartFile fileExcel, MultipartFile filePdf){
    long maxSize = 10 * 1024 * 1024; // 10MB
    return fileExcel.getSize() > maxSize || filePdf.getSize() > maxSize;
  }


  @SneakyThrows
  private List<String> handlePdf(MultipartFile filePdf, int numOfSheet) {
    List<String> list = new ArrayList<>();
    try (Document pdfDocument = new Document(filePdf.getInputStream())) {
      TableAbsorber absorber = new TableAbsorber();

      int sizePagePdf = pdfDocument.getPages().size();
      log.info("Size Page in Pdf file: {}, numOfSheet: {}", sizePagePdf, numOfSheet);
      int sizePageNew = numOfSheet > 1 ? sizePagePdf - 1 : sizePagePdf;

      log.info("ResultSizeExcel: {}, ResultSizePdf: {}", numOfSheet,
          sizePagePdf);
      if (sizePageNew != numOfSheet) {
        return List.of("Row_Not_Equals");
      }
      for (int i = 1; i <= sizePageNew; i++) {
        absorber.visit(pdfDocument.getPages().get_Item(i));
      }

      for (AbsorbedTable table : absorber.getTableList()) {
        for (AbsorbedRow row : table.getRowList()) {
          StringBuilder stringBuilder = new StringBuilder();
          for (AbsorbedCell cell : row.getCellList()) {
            TextFragmentCollection fragments = cell.getTextFragments();
            String textCell = "";

            for (TextFragment tf : fragments) {
              textCell = textCell.concat(tf.getText());
            }
            stringBuilder.append(textCell.replace(",", "").replaceAll("\\s+", ""))
                .append(TILDE_SYMBOL);
          }
          String e = new String(stringBuilder.toString().getBytes(StandardCharsets.UTF_8),
              StandardCharsets.UTF_8);
          list.add(e);
        }
      }
    } catch (Exception e) {
      log.info("Exception when handle Pdf: ", e);
    }

    return list;
  }

  private void handleException(Map<Integer, FindCompareDto> errorMaps, Sheet sheet1,
      Workbook workbookExcel, HttpServletResponse response, int rowIgnore, String fileName) {
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
    CellStyle cellStyleColor = workbookExcel.createCellStyle();
    Font font = workbookExcel.createFont();
    font.setColor(HSSFColorPredefined.RED.getIndex());
    cellStyleColor.setFont(font);

    // set color for background
    CellStyle cellStyleBackground = workbookExcel.createCellStyle();
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
      Map<Integer, String> valueError = findTextError(errorCompare.getValCorrect(),
          errorCompare.getValIncorrect());
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
        "attachment; filename=" + "Thong_Tin_Loi_" + fileName +"_" + LocalDate.now().format(dateTimeFormatter)
            + ".xlsx");
    try (OutputStream out = response.getOutputStream()) {
      workbookExcel.write(out);
    } catch (IOException e) {
      log.info("Error writing Excel file: ", e);
    }
  }

  private Map<Integer, String> findTextError(String valSource, String valCompare) {
    Map<Integer, String> result = new HashMap<>();
    String[] source = valSource.split(TILDE_SYMBOL);
    String[] compare = valCompare.split(TILDE_SYMBOL);
    for (int i = 0; i < source.length; i++) {
      if (!source[i].equals(compare[i])) {
        result.put(i, source[i]);
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
        valRow.append(getMergedCellValue(sheet, row.getRowNum(), cell.getColumnIndex(), cell))
            .append(TILDE_SYMBOL);
      }

      result.put(row.getRowNum(), valRow.toString().replace(",", "").replaceAll("\\s+", ""));
    }
    return result;
  }

  public static String getMergedCellValue(Sheet sheet, int row, int col, Cell cellResult) {
    for (CellRangeAddress range : sheet.getMergedRegions()) {
      if (range.isInRange(row, col)) {
        Row firstRow = sheet.getRow(range.getFirstRow());
        if (firstRow != null) {
          Cell cell = firstRow.getCell(range.getFirstColumn());
          return cell.getStringCellValue();
        }
      }
    }
    return cellResult.getStringCellValue();
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
