package com.satyak.qa.pages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CheckoutPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By firstName = By.cssSelector("[data-test='firstName']");
    private final By lastName = By.cssSelector("[data-test='lastName']");
    private final By postalCode = By.cssSelector("[data-test='postalCode']");
    private final By continueButton = By.cssSelector("[data-test='continue']");
    private final By error = By.cssSelector("[data-test='error']");
    private final By subtotal = By.cssSelector(".summary_subtotal_label");
    private final By tax = By.cssSelector(".summary_tax_label");
    private final By total = By.cssSelector(".summary_total_label");
    private final By finish = By.cssSelector("[data-test='finish']");

    public CheckoutPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void fillCustomer(String first, String last, String postal) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName)).sendKeys(first);
        driver.findElement(lastName).sendKeys(last);
        driver.findElement(postalCode).sendKeys(postal);
    }

    public void continueToOverview() {
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    public String errorText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(error)).getText();
    }

    public double money(By locator) {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
        Matcher matcher = Pattern.compile("\\$([0-9]+\\.[0-9]{2})").matcher(text);
        if (!matcher.find()) throw new AssertionError("Could not parse money value from: " + text);
        return Double.parseDouble(matcher.group(1));
    }

    public double itemTotal() { return money(subtotal); }
    public double taxTotal() { return money(tax); }
    public double grandTotal() { return money(total); }

    public void finish() {
        wait.until(ExpectedConditions.elementToBeClickable(finish)).click();
    }
}
