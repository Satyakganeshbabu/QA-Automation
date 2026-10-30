package com.satyak.qa.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By items = By.cssSelector(".cart_item");
    private final By checkout = By.cssSelector("[data-test='checkout']");

    public CartPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public List<String> itemNames() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_list")));
        return driver.findElements(items).stream()
            .map(item -> item.findElement(By.cssSelector(".inventory_item_name")).getText()).toList();
    }

    public void checkout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkout)).click();
    }
}
