package fr.kevin.ScrapingChallengeBankin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class App{

    private static final String baseUrl = "https://web.bankin.com/challenge/index.html" ;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    
    
    /*	Set chromeDriverPath
     *  chromeDriverPath should be in a config.properties file
     *  Exit if any problem found
     */
    static {
    	InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			LOGGER.severe("Could not find config.properties, quitting");
			System.exit(1);
		}
    	Properties properties = new Properties();
    	try {
			properties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.severe("Error loading property file, quitting");
			System.exit(1);
		}
    	String  chromeDriverPath = properties.getProperty("chromeDriverPath");
    	File f = new File(chromeDriverPath);
    	if(!f.exists()){
    		LOGGER.severe("The provided chromeDriverPath does not exist, quitting");
			System.exit(1);
    	}
    	System.setProperty("webdriver.chrome.driver", chromeDriverPath);
    }
    
    
    public static void main( String[] args ) throws JsonProcessingException, InterruptedException{
    	
    	
        LOGGER.info("Starting");
        
        
    	// TODO: make this shit configurable
		
		
        int cpt = 0 ;
        List<BankAccountOperation> operations = new ArrayList<BankAccountOperation>();
        
        // Thread number hardcoded for the moment
        // TODO: benchmark / fine tune this by n° of core available on the host machine
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Set<Callable<List<BankAccountOperation>>> callables = new HashSet<Callable<List<BankAccountOperation>>>();
        
        // After https://web.bankin.com/challenge/index.html?start=4950 it seems like there isn't any more bank account operations
        while(cpt < 5000){
        	String url = baseUrl + String.format("?start=%s", cpt) ;
        	
        	BankAccountOperationFetcher fetcher = new BankAccountOperationFetcher(url);
        	callables.add(fetcher);
        	cpt += 50 ;
        }
        
        try{
        	List<Future<List<BankAccountOperation>>> futures = executor.invokeAll(callables);
        	executor.shutdown();
        	
        	executor.awaitTermination(10, TimeUnit.MINUTES);
        	
        	
        	// get the result of all threads in the operations list
        	for(Future<List<BankAccountOperation>> future : futures){
        		operations.addAll(future.get());
        	}
        }catch(InterruptedException e){
        	e.printStackTrace();
        }catch(ExecutionException e2){
        	e2.printStackTrace();
        }
        
        
        // Sort the operation list by transactionId
        Collections.sort(operations, new Comparator<BankAccountOperation>(){
            public int compare(BankAccountOperation o1, BankAccountOperation o2){
                if(o1.getTransactionId() == o2.getTransactionId())
                    return 0;
                return o1.getTransactionId() < o2.getTransactionId() ? -1 : 1;
            }
       });
        
        // pretty printing the operations to a json file
        ObjectMapper objectMapper = new ObjectMapper();
     	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
     	 File file = new File("bankAccountOperations.json");
         try {
        	 objectMapper.writeValue(file, operations);
         } catch (IOException e) {
             e.printStackTrace();
         }
         LOGGER.info("Done");
    }
    
}
