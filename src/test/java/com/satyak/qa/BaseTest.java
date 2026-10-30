package com.satyak.qa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static String config(String property, String env, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(env);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    protected String baseUrl() {
        String url = config("baseUrl", "BASE_URL", "https://www.saucedemo.com/");
        return url.endsWith("/") ? url : url + "/";
    }
    protected String standardUser() {
        return config("standardUser", "STANDARD_USER", "standard_user");
    }
    protected String lockedOutUser() {
        return config("lockedOutUser", "LOCKED_OUT_USER", "locked_out_user");
    }
    protected String password() {
        return config("saucePassword", "SAUCE_PASSWORD", "secret_sauce");
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getenv().getOrDefault("HEADLESS", "false"))) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    protected void saveFailureScreenshot(String testName) {
        if (!(driver instanceof TakesScreenshot)) return;
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(source.toPath(), dir.resolve(testName.replaceAll("[^a-zA-Z0-9._-]", "_") + ".png"),
                       java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {
            // Preserve the original test failure; screenshot failure should not mask it.
        }
    }

    @RegisterExtension
    final TestExecutionExceptionHandler screenshotOnFailure = (context, throwable) -> {
        saveFailureScreenshot(context.getDisplayName());
        throw throwable;
    };
}
