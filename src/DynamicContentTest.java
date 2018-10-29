import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DynamicContentTest {

    //Path to chrome driver/gecko driver
    public String driverPath= "/DynamicWebpageTest/chromedriver.exe";
    //Page with Dynamic content to test
    public String testPage = "https://the-internet.herokuapp.com/dynamic_content";
    public WebDriver driver;
    //Path to json file for avatars (Test2)
    public String jsonFilePath = "/src/Avatars.json";

    //XPath
    public String xpRow = "/div[@class='large-10 columns']";
    public String xpathImg = "//div[@class='large-2 columns']/img";
    public String imgURL = "https://the-internet.herokuapp.com/img/avatars/Original-";

    //Utility method to initialize a web driver
    public WebDriver getDriver() {
        System. setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.get(testPage);
        return driver;
    }

    //Utility method to close a driver.
    public void closeDriver(WebDriver driver){
        driver.quit();
    }

//    Test 1:
//    Assert that the dynamic text (the lorem ipsum text block) on the page contains a word at least 10 characters in length.
//    Stretch goal:
//    Print the longest word on the page.
    @Test
    public void testMinimumLength(){

        //setup the driver with test page
        driver = getDriver();

        int maxLen =0;
        int reqLen = 10;
        boolean reqLenFound =false;
        String reqLenWord ="";
        int reqLenRow = 0;
        //This is for the stretch goal to store the words with longest length assuming there can be any number of words with same length
        List<String> longestWords = new ArrayList<>();

        try{
            //FInd the rows of text in the webpage using xpath
            List<WebElement> objs = driver.findElements(By.xpath("/" + xpRow));
            int numOfElements = objs.size();

            // for every row, get the text string
            for(int i=0;i<numOfElements;i++){
               String reqXpath = "//div[@class='row']" + "[" + i + "]" + xpRow;
                List<WebElement> row = new ArrayList<>();
               try{
                    row = driver.findElements(By.xpath(reqXpath));
                    System.out.println("The text in row " + i + "is shown as expected");

                    //for a row of text, split the text string to an array of words and find the longest word(s)
                    String text = row.get(0).getText();
                    String[] words = text.split(" ");

                    for(String w : words){
                        int len = w.length();
                        //If the required length is met, update the values for the test to display later
                        if(!reqLenFound  && len >= reqLen) {
                            reqLenFound = true;
                            reqLenWord = w;
                            reqLenRow = i;
                        }
                        if(len > maxLen)
                        {
                            maxLen=len;
                            longestWords.add(w);
                        }
                        else if(len==maxLen)
                            longestWords.add(w);
                    }
               }
               catch (NoSuchElementException e){
                   System.out.println(" No element found using the xpath\n%s" + e );
                   driver.quit();
                }


                System.out.println("TEST 1 OUTPUT");
                System.out.println();
                System.out.println();
                if(reqLenFound){
                   System.out.println("A word with minimum length requirement of 10 was found");
                   System.out.println("First encountered Min word: " + reqLenWord + " Row: " + reqLenRow);
                }

                System.out.println("Longest character words in the page :");
                for(String word: longestWords)
                    System.out.println(word);

            }

        }
        catch(AssertionError e){
            System.out.println(e);
        }
        finally{
            closeDriver(driver);
        }
    }

//    Test 2:
//
//    Assert that the "Punisher" image (silhouette with a skull on his chest) does not appear on the page.  This test may pass or fail on any given execution depending on whether the punisher happens to be on the page.
//
//    Stretch goal:
//
//    Give names to each avatar that can appear on the page and print out each avatars name


    @Test
    public void testAssertImage() throws IOException{

        //setup the driver with test page
        driver = getDriver();

        //BUild a hashmap with the avatars in json file. More avatars discovered will be added to the json file as we keep running this test again
        //Only 5 avatars were found in my testing. Hence this method to keep discovering more avatars based on the url
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String,String> avatars =
                new ObjectMapper().readValue(jsonFilePath, HashMap.class);

        //This is for stretch goal to store the avatar names we come across in the page
        List<String> avatarInPage = new ArrayList<>();
        boolean punisherFound = false;

        try {
            //Get all the images in the page
            List<WebElement> pageAvatar = driver.findElements(By.xpath(xpathImg));
            int numImgs = pageAvatar.size();

            //For every image, get the url value
            for (int i = 0; i < numImgs; i++) {
                String url = pageAvatar.get(0).getAttribute("src");
                // check if the url matches Punisher url. update the boolean value
                if (url.equals(imgURL + avatars.get("Punisher"))) {
                    punisherFound = true;
                    avatarInPage.add("Punisher");
                } else {
                    //CHeck every value in the map we built from the json. If it is present, get the name and add to our list of names
                    for (Map.Entry entry : avatars.entrySet()) {
                        if ((imgURL + entry.getValue()).equals(url)) {
                            avatarInPage.add((String) entry.getKey());
                        } else {
                            //if it is not present in our map, add it to the map and assign it a name. Assuming we can give any name,
                            // it will be easier to give a name corresponding to URL image number
                            //Hence names are given as Avatar <number found in url>
                            //not storing the entire url in the json. just the dynamic part of it.
                            String subUrl = url.split("Original-")[1];
                            String name = "Avatar " + subUrl.split("Avatar-")[1];
                            avatars.put(name, subUrl);
                            avatarInPage.add(name);
                        }
                    }
                }

            }

            System.out.println("TEST 2 OUTPUT");
            System.out.println();
            System.out.println();
            if (punisherFound)
                System.out.println("Punisher was found on the page.");
            else
                System.out.println("Punisher was not found on the page.");

            System.out.println();
            System.out.println();
            System.out.println("Avatars found on the page: ");
            for (String s : avatarInPage)
                System.out.println(s);

            //Write the map of values to json file as there might have been something new added
            mapper.writeValue(new File(jsonFilePath), avatars);
        }
        catch( NoSuchElementException e){
            System.out.println("Element not found " + e);

        }
        finally{
            closeDriver(driver);
        }
    }

}
