package com.jagrosh.jmusicbot.utils;

import com.sedmelluq.lava.extensions.youtuberotator.planner.*;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import dev.lavalink.youtube.clients.Web;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriverLogLevel;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.os.ExecutableFinder;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.bonigarcia.wdm.WebDriverManager;

public class YouTubeUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(YouTubeUtil.class);
    
    public enum RoutingPlanner {
        NONE,
        ROTATE_ON_BAN,
        LOAD_BALANCE,
        NANO_SWITCH,
        ROTATING_NANO_SWITCH
    }
    
    public static IpBlock parseIpBlock(String cidr) {
        if (Ipv6Block.isIpv6CidrBlock(cidr))
            return new Ipv6Block(cidr);

        if (Ipv4Block.isIpv4CidrBlock(cidr))
            return new Ipv4Block(cidr);
        
        throw new IllegalArgumentException("Could not parse CIDR " + cidr);
    }
    
    public static AbstractRoutePlanner createRouterPlanner(RoutingPlanner routingPlanner, List<IpBlock> ipBlocks) {
        
        switch (routingPlanner) {
            case NONE:
                return null;
            case ROTATE_ON_BAN:
                return new RotatingIpRoutePlanner(ipBlocks);
            case LOAD_BALANCE:
                return new BalancingIpRoutePlanner(ipBlocks);
            case NANO_SWITCH:
                return new NanoIpRoutePlanner(ipBlocks, true);
            case ROTATING_NANO_SWITCH:
                return new RotatingNanoIpRoutePlanner(ipBlocks);
            default:
                throw new IllegalArgumentException("Unknown RoutingPlanner value provided");
        }
    }

    public static void GetPoTokenAndVisitorData(String userChromePath, String userChromeDriverPath, boolean headless)
    {
        //
        // 1. Find a Chrome install & launch the WebDriver
        //
        WebDriverManager wdm = WebDriverManager.chromedriver();
        if (userChromePath != null) {
            wdm.browserVersionDetectionCommand(userChromePath + " --version");
        }
        else if (wdm.getBrowserPath().isEmpty())
        {
            // try finding a Chromium browser instead (Linux users usually install Chromium instead of Google Chrome)
            wdm = WebDriverManager.chromiumdriver();
            if (wdm.getBrowserPath().isEmpty()) {
                LOGGER.error("Could not obtain a PO token for YouTube playback, because no Chrome browser could be found. Please install Google Chrome or Chromium!");
                return;
            }
        }
        if (userChromeDriverPath == null)
            // Automatically download & set up ChromeDriver for the Chrome/Chromium version that the user has installed
            wdm.setup();
        else {
            // Find & use the ChromeDriver executable the user provided
            String path = new ExecutableFinder().find(userChromeDriverPath);
            if (path == null) {
                LOGGER.error("Could not obtain a PO token for YouTube playback, because the specified ChromeDriver could not be found. " +
                        "Please check your config's value of the chromedriverpath, or set it to \"AUTO\" to have JMusicBot automatically download ChromeDriver");
                return;
            }
            LOGGER.info("Using ChromeDriver at {}", path);
            System.setProperty("webdriver.chrome.driver", path);
        }

        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.setBinary(userChromePath != null ? userChromePath : wdm.getBrowserPath().get().toString());

        if (!headless)
            chromeOptions.addArguments("--auto-open-devtools-for-tabs");
        else
            chromeOptions.addArguments("--headless=new");

        ChromeDriverService.Builder chromeDriverBuilder = new ChromeDriverService.Builder();
        if (LOGGER.isDebugEnabled())
            chromeDriverBuilder.withLogLevel(ChromiumDriverLogLevel.DEBUG);

        WebDriver driver = new ChromeDriver(chromeDriverBuilder.build(), chromeOptions);

        //
        // 2. Setup a network interceptor to intercept the player API request & obtain po token and visitor data
        //

        // as the network interceptor below runs on a different thread, we'll have it notify us on this/current thread
        // whether retrieving the data failed (completes with the Exception) or succeeded (completes with null)
        CompletableFuture<Exception> interceptorFuture = new CompletableFuture<>();

        NetworkInterceptor interceptor = new NetworkInterceptor(driver,
                (Filter) next -> req -> {
            
            if (req.getMethod() != HttpMethod.POST || !req.getUri().contains("/youtubei/v1/player"))
                return next.execute(req);
    
            try {
                String rawBody = Contents.string(req);
                JSONObject body = new JSONObject(rawBody);

                String visitorData = body
                        .getJSONObject("context")
                        .getJSONObject("client")
                        .getString("visitorData");

                String poToken = body
                        .getJSONObject("serviceIntegrityDimensions")
                        .getString("poToken");


                LOGGER.info("Successfully retrieved PO token & visitor data for YouTube playback.");
                LOGGER.debug("PO token: {}", poToken);
                LOGGER.debug("Visitor Data: {}", visitorData);

                if (poToken.length() < 160)
                    LOGGER.warn("There is a high chance that the retrieved PO token will not work. Try on a different network.");

                Web.setPoTokenAndVisitorData(poToken, visitorData);

                interceptorFuture.complete(null);
            } catch (Exception e) {
                interceptorFuture.complete(e);
            }

            return next.execute(req);
        });

        //
        // 3. Navigate to YouTube & start the player
        //
        try {
            driver.get("https://www.youtube.com/embed/aqz-KE-bpKQ");
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            WebElement moviePlayer = driver.findElement(By.cssSelector("#movie_player"));
            moviePlayer.click();

            try {
                // Wait for the network interceptor to tell us if it got the data
                Exception result = interceptorFuture.get(20, TimeUnit.SECONDS);
                if (result != null)
                    throw new Exception("Failed to read YouTube player request body", result);
            } catch (TimeoutException e) {
                throw new Exception("Timed out waiting for YouTube player request");
            }


        } catch (Exception e) {
            LOGGER.error("Failed to obtain PO token for YouTube playback: {}", e.getMessage());
            LOGGER.debug("Exception:", e);
        } finally {
            interceptor.close();
            // Only quit the browser when not in headless mode. Helps with troubleshooting.
            if (headless)
                driver.quit();
        }
    }
}
