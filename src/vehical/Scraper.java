/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import vehical.initializer.FirefoxInitializer;

/**
 *
 * @author dhanushka
 */
public class Scraper {

    public boolean startScrape(String url) throws InterruptedException {

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

    private void innerPage(List<WebElement> list, FirefoxDriver driver) throws InterruptedException {
        JavascriptExecutor executor = (JavascriptExecutor) driver;

        for (WebElement ad : list) {
            executor.executeScript("arguments[0].click();", ad);
            Thread.sleep(5000);
            String title = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/h1").getAttribute("innerText");
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

            List<WebElement> tbl = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[2]/div[5]/div[1]/table/tbody")
                    .findElements(By.xpath("./*"));

            WebElement toptbl = driver.findElementByXPath("/html/body/div[1]/div[1]/main/div[1]/div[3]/div[5]/div[3]/div[3]/div/div[1]/div/div/div/table/tbody");
            belowMarket = toptbl.findElement(By.tagName("tr")).findElements(By.xpath("./*")).get(0)
                    .getAttribute("innerText").replace("BELOW MARKET", "");

            desc = driver.findElementByXPath("//*[@id=\"#description\"]").getAttribute("innerText");
            WebElement info = driver.findElementByXPath("//*[@id=\"detailsContactDealerForm\"]").findElement(By.tagName("strong"));
            String[] split = info.getAttribute("innerText").split(" ");
            year = info.getAttribute("innerText").split(" ")[0];
            
            List<String> arr = new ArrayList<String>(Arrays.asList(split));
            arr.remove(0);
            arr.remove(1);
            for (String string : arr) {
                modal = modal+" "+string;
            }
            

            for (WebElement row : tbl) {
                System.out.println(row.getTagName());
                try {

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

                } catch (Exception e) {

                }
            }

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
        }
    }
}
