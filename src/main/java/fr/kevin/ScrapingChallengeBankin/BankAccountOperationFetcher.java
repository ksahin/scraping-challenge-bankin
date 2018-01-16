package fr.kevin.ScrapingChallengeBankin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BankAccountOperationFetcher implements Callable<List<BankAccountOperation>>{
	
	private static final Logger LOGGER = Logger.getLogger(BankAccountOperationFetcher.class.getName());
	private String url ;
	public BankAccountOperationFetcher(String url){
		this.url = url ;
	}
	
	
	public List<BankAccountOperation> call() throws Exception {
		// TODO: make this shit configurable
		System.setProperty("webdriver.chrome.driver", "/Users/kevin/Downloads/chromedriver");
    	ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        List<BankAccountOperation> operations = new ArrayList<BankAccountOperation>();
        
        LOGGER.info("Processing " + url );
        int attemps = 0 ;
        
        // TODO: try to override "SetTimeOut" js function to make the alert insta pop 
        while(operations.isEmpty() && attemps < 5){
            driver.get(url);
        	try{
        		   //Wait 5 seconds till alert is present
        		   WebDriverWait wait = new WebDriverWait(driver, 8);
        		   Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        		   //Accepting alert.
        		   alert.accept();
        		}catch(Throwable e){
        		   LOGGER.warning("Error came while waiting for the alert popup. "+e.getMessage());
        		}
        	operations.addAll(findTable(driver));
        	attemps++ ;
        }
        // This should not happen
    	if(operations.isEmpty()){
    		LOGGER.severe("Operation list empty for " + url );
    	}
        driver.quit();
        LOGGER.info(url + " done");
		return operations ;
	}
	
    private static List<BankAccountOperation> parseTable(WebDriver driver) throws InterruptedException, StaleElementReferenceException{
    	Thread.sleep(150);
    	List<BankAccountOperation> operations = new ArrayList<BankAccountOperation>();
    	// we want all tr but not the first line of the table
    	List<WebElement> operationList = (List<WebElement>) driver.findElements(By.xpath("//tbody/tr[not(.//th)]"));
    	String regex = "(.+)\\s(Transaction\\s\\d+)\\s(\\d+)(.)";
    	Pattern pattern = Pattern.compile(regex);
    	
    	for(WebElement op : operationList){
    		BankAccountOperation operation = new BankAccountOperation();
    		Matcher m = pattern.matcher(op.getText());
    		if(m.find()){
    			operation.setAccount(m.group(1));
    			operation.setTransactionId(Integer.parseInt(m.group(2).split(" ")[1]));
    			operation.setAmount(Integer.parseInt(m.group(3)));
    			operation.setCurrency(m.group(4));
    		}
    		operations.add(operation);
    	}
    	return operations;
    }
    
    private static List<BankAccountOperation> findTable(WebDriver driver) throws JsonProcessingException, InterruptedException{
 	// check if iframe is present
    	List<BankAccountOperation> operations = new ArrayList<BankAccountOperation>();
 	try{
 		driver.findElement(By.id("fm"));
 		driver.switchTo().frame("fm");
 		operations = parseTable(driver);
 		
 		
 	}catch(NoSuchElementException e){
 		WebElement table = driver.findElement(By.id("dvTable"));
 		if(table.findElements(By.xpath(".//tr")).size() == 0 ){
 			JavascriptExecutor js = (JavascriptExecutor) driver;  
 			js.executeScript("slowmode = false; generate();"); 			
 			WebDriverWait wait = new WebDriverWait(driver, 14);
 			WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table/tbody | //iframe[@id='fm']")));
 			findTable(driver);
 		}else{
 			// retry to avoid StaleElementReference exception
 			int attemps = 0 ; 
 			while(attemps < 3){
 				try{
 					operations = parseTable(driver);
 					break;
 				}catch(StaleElementReferenceException e1){
 				}
 				attemps++ ;
 			}
 			
 		}
 		
 	}
 	return operations ;
    }

}
