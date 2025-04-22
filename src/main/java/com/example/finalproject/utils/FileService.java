package com.example.finalproject.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipOutputStream;

public class FileService {


    public static Path uploadFile(String uploadPath, MultipartFile file) throws IOException {

        FileService.openFolder(uploadPath);

        Path filePath = Paths.get(uploadPath + "/" + file.getOriginalFilename());
        Files.write(filePath, file.getBytes());

        return filePath;
    }

    public static void unzipFile(String zipFilePath, String destDir) throws IOException {
        openFolder(destDir);

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());

                openFolder(newFile.getParent());

                if (!entry.isDirectory()) {
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }


    public static List<String> listFilesInDirectory(String directoryPath) {
        List<String> fileList = new ArrayList<>();
        File folder = new File(directoryPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }

    public static File openFolder(String dir){
        File folder = new File(dir);
        if (!folder.exists()) {
            if(!folder.mkdirs()){
                throw new RuntimeException("Could not open the folder");
            }
        }
        return folder;
    }
    public static boolean isZipFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] signature = new byte[4];
            inputStream.read(signature);

            // ZIP file signature: 50 4B 03 04
            return signature[0] == 0x50 && signature[1] == 0x4B && signature[2] == 0x03 && signature[3] == 0x04;
        } catch (IOException e) {
            return false;
        }
    }

    public static int numberOfShpFiles(String directoryPath){
        int number=0;
        for (String filePaths : listFilesInDirectory(directoryPath)) {
            if (filePaths.endsWith(".shp")) {
                number++;
            }
        }
        return number;
    }

    public static String getShapefilePath(String directoryPath) {
        for (String filePaths : listFilesInDirectory(directoryPath)) {
            if (filePaths.endsWith(".shp")) {
                return filePaths;
            }
        }
        return null;
    }


    public static void deleteFolder(String folderPath) {
        Path folder = Paths.get(folderPath);
        try {
            if (Files.exists(folder)) {
                Files.walk(folder)
                        .sorted(Comparator.reverseOrder()) // Delete files before the folder itself
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                            }
                        });
                System.out.println("Folder deleted: " + folderPath);
            } else {
                System.out.println("Folder does not exist: " + folderPath);
            }
        } catch (IOException e) {
            System.err.println("Error deleting folder: " + e.getMessage());
        }
    }

    public static byte[] exportToJson(List<String> columns, List<Object[]> rows) {
        JSONArray jsonArray = new JSONArray();
        for (Object[] row : rows) {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < columns.size(); i++) {
                obj.put(columns.get(i), row[i]);
            }
            jsonArray.put(obj);
        }
        return jsonArray.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] exportToCsv(List<String> columns, List<Object[]> rows) {
        StringBuilder csv = new StringBuilder(String.join(",", columns) + "\n");
        for (Object[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                csv.append(row[i] == null ? "" : row[i].toString());
                if (i < row.length - 1) csv.append(",");
            }
            csv.append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] exportToXlsx(List<String> columns, List<Object[]> rows) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            headerRow.createCell(i).setCellValue(columns.get(i));
        }
        int rowIdx = 1;
        for (Object[] row : rows) {
            Row sheetRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < row.length; i++) {
                sheetRow.createCell(i).setCellValue(row[i] == null ? "" : row[i].toString());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    public static byte[] exportToShapefile(String layer) throws IOException {
        String outputDir = "/tmp/";
        String basePath = outputDir + layer;

        List<String> command = Arrays.asList(
                "pgsql2shp",
                "-f", basePath,
                "-h", "chorvoq-postgis",
                "-u", "erkinovmuhammadayubps@gmail.com",
                "-P", "Qazxswe123$",
                "gis_data",
                layer
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("pgsql2shp failed with exit code " + exitCode);
            }

            // Create a zip of the generated files
            String zipPath = outputDir + layer + ".zip";
            try (FileOutputStream fos = new FileOutputStream(zipPath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (String ext : List.of(".shp", ".shx", ".dbf", ".prj")) {
                    File file = new File(basePath + ext);
                    if (!file.exists()) continue;

                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                    }
                }
            }

            System.out.println("shapefile zipped");
            return Files.readAllBytes(Paths.get(zipPath));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Error exporting shapefile", e);
        }
    }
}
