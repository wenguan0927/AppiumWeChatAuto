package com.example;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.appium.java_client.android.AndroidDriver;

@SuppressWarnings("Since15")
public class AppiumAutoScan {

    private static AndroidDriver mDriver;

    /**
     * 公众号列表数据
     */
    private static List<String> mGongzhongData = new ArrayList<>();
    /**
     * 已点击浏览的公众号数据
     */
    private static List<String> mGongzhongClickData = new ArrayList<>();

    private int mHistoryIndex;

    private final int TYPE_ALL = 0;
    private final int TYPE_IN_WEEK = 1;
    private final int TYPE_IN_MONTH = 2;
    private final int TYPE_IN_YEAR = 3;

    @Test
    public void launchBrowser() {
        try {
            mDriver = getDriver();
            gotoList(mDriver);
            do {
                mDriver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
                /**
                 * 这边是列表Item的Id，获取屏幕上显示的所有列表Item项
                 */
                List<WebElement> elementList = mDriver.findElementsById("com.tencent.mm:id/a0y");
                List<String> nameList = getListData(elementList);
                if(nameList != null){
                    mGongzhongData.addAll(nameList);
                }
                mDriver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);

                for (String name : nameList) {
                    if (!mGongzhongClickData.contains(name)) {
                        mGongzhongClickData.add(name);
                        mHistoryIndex = 0;
                        if (gotoMessageHistory(mDriver, name)) {
                            /**
                             * 遍历列表给出的文章
                             */
                            viewArticleDetail(mDriver);
                        }
                        /**
                         * 返回公众号列表
                         */
                        goBackToList(mDriver);
                    }
                }

                /**
                 * 滑动公众号列表逐个点击
                 */
            } while (!isScrollToBottom(mDriver));

            mDriver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取列表项节点文本
     *
     * @param dataList
     * @return
     */
    private static List<String> getListData(List<WebElement> dataList) {
        if (dataList == null) {
            return null;
        }
        int size = dataList.size();
        List<String> nameArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String name = dataList.get(i).getText();
            if(!isExist(name)){
                nameArray.add(name);
            }
        }
        return nameArray;
    }

    private static boolean isExist(String name){
        return mGongzhongData.contains(name);
    }

    /**
     * 滑动列表加载下一页数据
     *
     * @param driver
     * @return
     * @throws InterruptedException
     */
    private static boolean isScrollToBottom(AndroidDriver driver) throws InterruptedException {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        String beforeswipe = driver.getPageSource();
        driver.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 1000);
        /**
         * 设置8s超时，网络较差情况下时间过短加载不出内容
         */
        mDriver.manage().timeouts().implicitlyWait(8000, TimeUnit.MILLISECONDS);
        String afterswipe = driver.getPageSource();
        /**
         * 已经到底部
         */
        if (beforeswipe.equals(afterswipe)) {
            return true;
        }
        return false;
    }

    /**
     * 进入公众号列表
     *
     * @param driver
     * @throws Exception
     */
    private static void gotoList(AndroidDriver driver) throws Exception {
        driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
        driver.findElement(By.xpath("//android.widget.TextView[@text='通讯录']")).click();
        driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
        driver.findElement(By.xpath("//android.widget.TextView[@text='公众号']")).click();
    }

    private static AndroidDriver getDriver() throws MalformedURLException {
        DesiredCapabilities capability = new DesiredCapabilities();

        capability.setCapability("platformName", "emulator-5554");
        capability.setCapability("platformVersion", "4.4.4");
        capability.setCapability("deviceName", "MuMu");

        /**
         * 真机上platformName使用"Android"
         */
        /*
        capability.setCapability("platformName", "Android");
        capability.setCapability("platformVersion", "6.0");
        capability.setCapability("deviceName", "FRD-AL00");
        */

        capability.setCapability("unicodeKeyboard","True");
        capability.setCapability("resetKeyboard","True");
        capability.setCapability("app", "");
        capability.setCapability("appPackage", "com.tencent.mm");
        capability.setCapability("appActivity", ".ui.LauncherUI");
        capability.setCapability("fastReset", false);
        capability.setCapability("fullReset", false);
        capability.setCapability("noReset", true);
        capability.setCapability("newCommandTimeout", 2000);

        /**
         * 必须加这句，否则webView和native来回切换会有问题
         */
        capability.setCapability("recreateChromeDriverSessions", true);

        /**
         * 关键是加上这段
         */
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("androidProcess", "com.tencent.mm:tools");
        capability.setCapability(ChromeOptions.CAPABILITY, options);

        String url = "http://127.0.0.1:4723/wd/hub";
        mDriver = new AndroidDriver<>(new URL(url), capability);
        return mDriver;
    }


    /**
     *
     * @param driver
     * @throws Exception
     */
    private void viewArticleDetail(AndroidDriver driver) throws Exception {
        boolean isNeedBreak = false;

        do {
            /**
             * 切换到WebView上下文环境
             */
            driver.context("WEBVIEW_com.tencent.mm:tools");
            driver.manage().timeouts().implicitlyWait(1500, TimeUnit.MILLISECONDS);
            System.out.println("================= driver context ：" + driver.getContext());
            /**
             * 根据class类型模糊匹配找到公众号文章列表项
             * class类型有四种：
             * weui_media_box appmsg js_appmsg : 文章
             * weui_media_box text js_appmsg : 文本
             * weui_media_box img js_appmsg : 图片
             * weui_media_box appmsg audio_msg_primary js_appmsg playing : 语音
             *
             * 这边获取到的数据是所有已加载的，包括屏幕上没显示的，如果是Android原生的ListView只会获取到屏幕上已显示的项
             */
            List<WebElement> msgHistory = driver.findElements(By.xpath("//div[starts-with(@class,'weui_media_box')]"));

            int size = msgHistory.size();
            System.out.println("==================文章个数：" + size);
            for (int i = mHistoryIndex; i < size; i++) {
                checkException(driver, msgHistory, size);
                /**
                 * 切换到WebView上下文环境逐个点击列表项
                 */
                driver.context("WEBVIEW_com.tencent.mm:tools");
                WebElement item = msgHistory.get(mHistoryIndex);
                mHistoryIndex ++;

                String articleTitle = item.getText();
                System.out.println("==================articleTitle ：" + articleTitle);
                String articleTime = getDateStr(articleTitle);
                isNeedBreak = checkTimeLimit(TYPE_IN_WEEK, articleTime);
                if(isNeedBreak){
                    break;
                }
                String classAttr = item.getAttribute("class");
                /**
                 * 只点击文章类型列表项，其他不处理
                 */
                if("weui_media_box appmsg js_appmsg".equals(classAttr)){
                    /**
                     * 进入公众号文章详情页面
                     */
                    item.click();
                    mDriver.manage().timeouts().implicitlyWait(8000, TimeUnit.MILLISECONDS);

                    /**
                     * 切换回native的context，点击返回历史文章列表
                     */
                    driver.context("NATIVE_APP");
                    Thread.sleep(2500);
                    /**
                     * 此处替换为ID的方式减少查找的时间
                     *
                     * driver.findElement(By.xpath("//android.widget.ImageView[@content-desc='返回']")).click();
                     */
                    driver.findElementById("com.tencent.mm:id/ht").click();
                }
            }
            /**
             * 切换到NATIVE_APP上下文环境做滑动操作，加载下一页数据
             */
            driver.context("NATIVE_APP");
        }while (!isNeedBreak && !isScrollToBottom(driver));

        /**
         * 切回native方式，点击返回按钮回到公众号列表页面
         */
        driver.context("NATIVE_APP");
    }

    /**
     * 检查文章日期是否在限制范围内，超过日期限制则跳出循环
     *
     * @param type
     * @param articleTime
     * @return
     */
    private boolean checkTimeLimit(int type, String articleTime){
        System.out.println("==================checkTimeLimit ：" + articleTime);
        Calendar calendarLimit = Calendar.getInstance();
        switch (type){
            case TYPE_ALL:
                return false;
            case TYPE_IN_WEEK:
                calendarLimit.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case TYPE_IN_MONTH:
                calendarLimit.add(Calendar.MONTH, -1);
                break;
            case TYPE_IN_YEAR:
                calendarLimit.add(Calendar.YEAR, -1);
                break;
            default:break;
        }
        Date dateLimit = calendarLimit.getTime();
        System.out.println("==================dateLimit ：" + dateLimit.toString());
        int year = Integer.parseInt(articleTime.substring(0, articleTime.indexOf("年")));
        int month = Integer.parseInt(articleTime.substring(articleTime.indexOf("年") + 1, articleTime.indexOf("月")));
        int day = Integer.parseInt(articleTime.substring(articleTime.indexOf("月") + 1, articleTime.indexOf("日")));

        Calendar calendarArticle=Calendar.getInstance();
        calendarArticle.set(Calendar.YEAR, year);
        /**
         * 月份需要减去一个月，否则出来的日期会多加一个月的时间
         */
        calendarArticle.set(Calendar.MONTH, month - 1);
        calendarArticle.set(Calendar.DAY_OF_MONTH, day);
        Date dateArticle = calendarArticle.getTime();

        System.out.println("==================dateArticle ：" + dateArticle.toString());
        if(dateArticle.after(dateLimit)){
            return false;
        }
        return true;
    }

    /**
     * 在点击文章详情页面返回按钮的时候，低概率会连续返回，回到详细资料页面
     * 这边做一个检查，如果回到这个页面再次进入历史文章页面，重新爬取
     */
    private void checkException(AndroidDriver driver,List<WebElement> msgHistory, int size){
        try {
            /**
             * 此处替换为ID的方式减少查找的时间
             *
             * WebElement element = driver.findElement(By.xpath("//android.widget.TextView[@text='全部消息']"));
             */
            WebElement element = driver.findElementById("com.tencent.mm:id/aom");
            element.click();
            mDriver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
            mHistoryIndex = 0;
            driver.context("WEBVIEW_com.tencent.mm:tools");
            driver.manage().timeouts().implicitlyWait(1500, TimeUnit.MILLISECONDS);
            msgHistory = driver.findElements(By.xpath("//div[starts-with(@class,'weui_media_box')]"));
            size = msgHistory.size();
            System.out.println("==================is detail page, reEnter!");
        } catch (NoSuchElementException e){
            System.out.println("==================not detail page!");
        }
    }

    /**
     * 进入历史文章列表
     *
     * @param driver
     * @param name
     * @return
     * @throws Exception
     */
    private boolean gotoMessageHistory(AndroidDriver driver, String name) throws Exception {
        String xPath = "//android.widget.TextView[@text='" + name + "']";
        mDriver.findElement(By.xpath(xPath)).click();
        mDriver.manage().timeouts().implicitlyWait(3000, TimeUnit.MILLISECONDS);

        /**
         * driver.findElement(By.xpath("//android.widget.ImageButton[@content-desc='聊天信息']")).click();
         *
         * 此处替换为ID的方式减少查找的时间
         */
        WebElement elementChatInfo = driver.findElementById("com.tencent.mm:id/hh");
        elementChatInfo.click();
        mDriver.manage().timeouts().implicitlyWait(3000, TimeUnit.MILLISECONDS);

        /**
          * 滑动到列表底部
          */
        while (!isScrollToBottom(driver)) {

        }

        /**
         * driver.findElement(By.xpath("//android.widget.TextView[@text='全部消息']")).click();
         *
         * 此处替换为ID的方式减少查找的时间
         */
        WebElement elementAllInfo = driver.findElementById("com.tencent.mm:id/aom");
        elementAllInfo.click();
        mDriver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);

        return true;
    }

    /**
     * 从历史文章页面回到公众号列表
     *
     * @param driver
     * @throws Exception
     */
    private void goBackToList(AndroidDriver driver) throws Exception {
        By backBtn = By.xpath("//android.widget.ImageView[@content-desc='返回']");
        /**
         * 连续返回三次回到公众号列表
         */
        int i = 0;
        while (isElementExist(driver, backBtn) && i < 3) {
            i++;
            driver.findElement(backBtn).click();
            mDriver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
        }
        System.out.println("已经回到公众号列表");
    }

    private boolean isElementExist(AndroidDriver driver, By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    /**
     * 从字符串中正则匹配获取文章的日期
     *
     * @param content
     * @return
     */
    public static String getDateStr(String content){
        return getMatcherStrs(content,"\\d{4}\\"+"年"+"\\d{1,2}\\"+"月"+"\\d{1,2}\\" + "日");
    }

    /**
     * 获取匹配的字符串
     *
     * @param content
     * @param sPattern
     * @return
     */
    public static String getMatcherStrs(String content, String sPattern){
        Pattern p = Pattern.compile(sPattern);
        Matcher m = p.matcher(content);
        if(m.find()){
            return m.group();
        }
        return null;
    }

    /* =========================》》》》》 未使用 《《《《《《 ========================*/
    /**
     * 滑动获取公众号名称列表数据
     *
     * @param driver
     * @return
     */
    private static void getSearchList(AndroidDriver driver) throws InterruptedException {
        do {
            Thread.sleep(2000);
            /**
             * 这边是列表Item的Id，获取屏幕上显示的所有列表Item项
             */
            List<WebElement> elementList = driver.findElementsById("com.tencent.mm:id/a0y");
            List<String> nameList = getListData(elementList);
            if(nameList != null){
                mGongzhongData.addAll(nameList);
            }
            Thread.sleep(2000);
        } while (!isScrollToBottom(driver));
    }

    /**
     * 向下滑动
     *
     * @param driver
     * @return
     * @throws InterruptedException
     */
    private static boolean scrollDown(AndroidDriver driver) throws InterruptedException {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        boolean swiped;
        String beforeswipe = driver.getPageSource();
        driver.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 1000);
        Thread.sleep(1000);
        String afterswipe = driver.getPageSource();
        swiped = beforeswipe.equals(afterswipe) ? true : false;
        return swiped;
    }
}
