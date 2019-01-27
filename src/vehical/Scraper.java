/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehical;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Data;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import vehical.initializer.FirefoxInitializer;

/**
 *
 * @author dhanushka
 */
public class Scraper {

    String location;

    public boolean startScrape(String url, String location) throws InterruptedException, IOException {
        this.location = location;
        createXlsFile();
        FirefoxDriver driver = FirefoxInitializer.getDriver();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        driver.get(url);

        while (true) {
            Thread.sleep(5000);
            WebElement body = driver.findElementByTagName("body");
            WebElement mainSearchResult = body.findElement(By.xpath("//*[@id=\"mainSearchResultsContainer\"]"));
            WebElement pagination = mainSearchResult.findElements(By.xpath("./*")).get(1);
            WebElement searchResult = mainSearchResult.findElement(By.id("searchResultsContainer2"))
                    .findElement(By.tagName("div"))
                    .findElement(By.id("listingsDivParent"))
                    .findElement(By.id("listingsDiv"));

            List<WebElement> ads = searchResult.findElements(By.xpath("./*"));
            List<WebElement> list = new ArrayList<>();
            for (WebElement ad : ads) {
                if (ad.getAttribute("id").contains("banner")) {
                    System.err.println("SKIPPED BANNER");
                    continue;
                }

                list.add(ad.findElement(By.tagName("div")));
                System.out.println(ad.getAttribute("id") + " ADDED TO SCRAPE LIST");
            }
            innerPage(list, driver);
            WebElement next = pagination.findElements(By.xpath("./*")).get(0).findElement(By.className("nextPageElement"));
            executor.executeScript("arguments[0].click();", next);
            break;
        }
        return false;
    }

    private void innerPage(List<WebElement> list, FirefoxDriver driver) throws InterruptedException, IOException {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        Data data = null;
        List<Data> ds = new ArrayList<>();
        for (WebElement ad : list) {
            data = new Data();

            System.err.println(ad.getAttribute("class") + "========");
            String featuredImage = ad.findElement(By.tagName("div")).findElement(By.tagName("img")).getAttribute("src");
            String post_content = ad.findElement(By.tagName("div")).findElement(By.tagName("h4")).getAttribute("innerText");
            String post_excerpt = "";
            String post_status = "publish";
            String make = post_content.split(" ")[1];

            executor.executeScript("arguments[0].click();", ad);
            Thread.sleep(5000);
            String title = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/h1").getAttribute("innerText");
            String condition = driver.findElementByXPath("/html/body/div[1]/div[1]/div[1]/ul/li[2]")
                    .getAttribute("innerText").split(" ")[1];

            String price = ad.findElement(By.tagName("div")).findElement(By.className("cg-dealFinder-result-stats"))
                    .findElements(By.xpath("./*")).get(0).findElement(By.tagName("span")).getAttribute("innerText");
            String salePrice = "";
            price = price.replace(" ", "");
            price = price.split("\n")[1].replace("$", "").replace(",", ".");
            double sale = 1.14 * Double.parseDouble(price);
            salePrice = Math.round(sale * 100D) / 100D + "";

            String mileage = "";
            String belowMarket = "";
            String location = "";
            String transmission = "";
            String engine = "";
            String exterior_color = "";
            String interior_color = "";
            String vin = "";
            String drivetrain = "";
            String desc = "";
            String year = "";
            String modal = "";
            String img1 = "";
            String img2 = "";
            String img3 = "";
            String img4 = "";
            String img5 = "";

            List<WebElement> tbl = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[5]/div[1]/table/tbody")
                    .findElements(By.xpath("./*"));

            WebElement toptbl = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[3]/div/div[1]/div/div/div/table/tbody");
            belowMarket = toptbl.findElement(By.tagName("tr")).findElements(By.xpath("./*")).get(0)
                    .getAttribute("innerText").replace("BELOW MARKET", "");

            try {
                desc = driver.findElementByXPath("//*[@id=\"#description\"]").getAttribute("innerText");
            } catch (NoSuchElementException r) {
                System.err.println("ERROR : DESCRIPTION NOT FOUND");
            }

            WebElement info = driver.findElementByXPath("//*[@id=\"detailsContactDealerForm\"]").findElement(By.tagName("strong"));
            String[] split = info.getAttribute("innerText").split(" ");
            year = info.getAttribute("innerText").split(" ")[0];

            List<String> arr = new ArrayList<String>(Arrays.asList(split));
            arr.remove(0);
            arr.remove(1);
            for (String string : arr) {
                modal = modal + " " + string;
            }

            for (WebElement row : tbl) {
                System.out.println(row.getTagName());

                try {
                    row.findElements(By.xpath("./*")).get(0);
                } catch (Exception t) {
                    continue;
                }
                
                if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Mileage")) {
                    mileage = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Location")) {
                    location = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Transmission")) {
                    transmission = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Engine")) {
                    engine = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Exterior Color")) {
                    exterior_color = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Interior Color")) {
                    interior_color = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("VIN")) {
                    vin = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                } else if (row.findElements(By.xpath("./*")).get(0).getAttribute("innerText").contains("Drivetrain")) {
                    drivetrain = row.findElements(By.xpath("./*")).get(1).getAttribute("innerText");
                }
            }

            try {
                for (int i = 0; i < 5; i++) {
                    WebElement gallery = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[1]/div[2]");
                    if (i == 0) {
                        executor.executeScript("arguments[0].click();", gallery.findElements(By.xpath("./*")).get(1).findElement(By.tagName("div")));
                        Thread.sleep(5000);
                        img1 = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[1]/div[1]/div/a/div/img").getAttribute("src");
                    } else if (i == 1) {
                        executor.executeScript("arguments[0].click();", gallery.findElements(By.xpath("./*")).get(2).findElement(By.tagName("div")));
                        Thread.sleep(5000);
                        img2 = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[1]/div[1]/div/a/div/img").getAttribute("src");
                    } else if (i == 2) {
                        executor.executeScript("arguments[0].click();", gallery.findElements(By.xpath("./*")).get(3).findElement(By.tagName("div")));
                        Thread.sleep(5000);
                        img3 = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[1]/div[1]/div/a/div/img").getAttribute("src");
                    } else if (i == 3) {
                        executor.executeScript("arguments[0].click();", gallery.findElements(By.xpath("./*")).get(4).findElement(By.tagName("div")));
                        Thread.sleep(5000);
                        img4 = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[1]/div[1]/div/a/div/img").getAttribute("src");
                    } else if (i == 4) {
                        executor.executeScript("arguments[0].click();", gallery.findElements(By.xpath("./*")).get(5).findElement(By.tagName("div")));
                        Thread.sleep(5000);
                        img5 = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[1]/div[1]/div/a/div/img").getAttribute("src");
                    }
                    Thread.sleep(2000);
                }
            } catch (NoSuchElementException exception) {
                System.err.println("ERROR : LOW IMAGE COUNT");
            }
            System.out.println("FEATURED IMAGE - " + featuredImage);
            System.out.println("POST CONTENT - " + post_content);
            System.out.println("MAKE - " + make);
            System.out.println("PRICE - " + price);
            System.out.println("SALE PRICE - " + salePrice);
            System.out.println("CONDITION - " + condition);
            System.out.println("POST TITLE - " + title);
            System.out.println("MILAGE - " + mileage.replace("miles", ""));
            System.out.println("BELOW MARKETS - " + belowMarket);
            System.out.println("LOCATION - " + location);
            System.out.println("TRANSMISSION - " + transmission);
            System.out.println("ENGINE - " + engine);
            System.out.println("EXTERIOR COLOR - " + exterior_color);
            System.out.println("INTERIOR COLOR - " + interior_color);
            System.out.println("VIN - " + vin);
            System.out.println("DRIVETRAIN - " + drivetrain);
            System.out.println("DESC - " + desc);
            System.out.println("YEAR - " + year);
            System.out.println("MODEL - " + modal);
            System.out.println("IMG 1 - " + img1);
            System.out.println("IMG 2 - " + img2);
            System.out.println("IMG 3 - " + img3);
            System.out.println("IMG 4 - " + img4);
            System.out.println("IMG 5 - " + img5);

            data.setPost_title(title);
            data.setPost_content(post_content);
            data.setPost_excerpt(post_excerpt);
            data.setPost_status(post_status);
            data.setMake(make);
            data.setCondition(condition);
            data.setSale_price(salePrice);
            data.setPrice(price);
            data.setFeatured_Image(featuredImage);
            data.setMileage(mileage);
            data.setBelow_market(belowMarket);
            data.setLocation(location);
            data.setTransmission(transmission);
            data.setEngine(engine);
            data.setExterior_color(exterior_color);
            data.setInterior_color(interior_color);
            data.setVIN_number(vin);
            data.setDrive_train(drivetrain);
            data.setDescription(desc);
            data.setYear(year);
            data.setModel(modal);
            data.setPhotourl1(img1);
            data.setPhotourl2(img2);
            data.setPhotourl3(img3);
            data.setPhotourl4(img4);
            data.setPhotourl5(img5);
            ds.add(data);

        }
        try {
            appendRow(ds);
        } catch (InvalidFormatException ex) {
            Logger.getLogger(Scraper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void appendRow(List<Data> datalist) throws IOException, InvalidFormatException {
        try {

            Workbook wb3 = WorkbookFactory.create(new FileInputStream(location + "/inventory.xlsx"));
            Sheet sh = wb3.getSheet("inventory");
            int rows = sh.getLastRowNum();

            for (Data data : datalist) {
                Row row = sh.createRow(rows++);
                row.createCell(0).setCellValue(data.getPost_title());
                row.createCell(1).setCellValue(data.getPost_content());
                row.createCell(2).setCellValue(data.getPost_excerpt());
                row.createCell(3).setCellValue(data.getPost_status());
                row.createCell(4).setCellValue(data.getMake());
                row.createCell(5).setCellValue(data.getCondition());
                row.createCell(6).setCellValue(data.getSale_price());
                row.createCell(7).setCellValue(data.getPrice());
                row.createCell(8).setCellValue(data.getMileage());
                row.createCell(9).setCellValue(data.getFeatured_Image());
                row.createCell(10).setCellValue(data.getImages());
                row.createCell(11).setCellValue(data.getPhotourl1());
                row.createCell(12).setCellValue(data.getPhotourl2());
                row.createCell(13).setCellValue(data.getPhotourl3());
                row.createCell(14).setCellValue(data.getPhotourl4());
                row.createCell(15).setCellValue(data.getPhotourl5());
                row.createCell(16).setCellValue(data.getBelow_market());
                row.createCell(17).setCellValue(data.getLocation());
                row.createCell(18).setCellValue(data.getTransmission());
                row.createCell(19).setCellValue(data.getEngine());
                row.createCell(20).setCellValue(data.getExterior_color());
                row.createCell(21).setCellValue(data.getInterior_color());
                row.createCell(22).setCellValue(data.getVIN_number());
                row.createCell(23).setCellValue(data.getDrive_train());
                row.createCell(24).setCellValue(data.getDescription());
                row.createCell(25).setCellValue(data.getYear());
                row.createCell(26).setCellValue(data.getModel());
            }

            FileOutputStream fileOut = new FileOutputStream(location + "/inventory.xlsx");
            wb3.write(fileOut);
            fileOut.close();
            System.out.println("excel updated.");

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void createXlsFile() throws IOException {

        try {
            new FileInputStream(location + "/inventory.xlsx");
        } catch (FileNotFoundException e) {
            new File("/var/lib/tomcat8/inventory").mkdir();
            Workbook workbook = new XSSFWorkbook();

            CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("inventory");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(1);
//            
            ArrayList<String> list = new ArrayList();
            ArrayList<String> list2 = new ArrayList();

            list.add("Post_title");
            list.add("post_content");
            list.add("post_excerpt");
            list.add("post_status");
            list.add("make");
            list.add("condition");
            list.add("Sale price");
            list.add("Price");
            list.add("Mileage");
            list.add("Featured Image");
            list.add("Images");
            list.add("photourl1");
            list.add("photourl2");
            list.add("photourl3");
            list.add("photourl4");
            list.add("photourl5");
            list.add("below_market");
            list.add("location");
            list.add("transmission");
            list.add("engine");
            list.add("exterior_color");
            list.add("interior_color");
            list.add("VIN_number");
            list.add("drive_train");
            list.add("description");
            list.add("year");
            list.add("model");

            for (int i = 0; i < list.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(list.get(i));
                cell.setCellStyle(headerCellStyle);
            }

            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
            FileOutputStream fileOut = new FileOutputStream(location + "/inventory.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Excel Created");
        }
    }
}
