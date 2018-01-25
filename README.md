# scraping-challenge-bankin

Little attempt to solve the [Bankin Web Scraping Challenge](https://blog.bankin.com/challenge-engineering-web-scrapping-dc5839543117)
The goal here is to scrape this (horrible) website https://web.bankin.com/challenge/index.html  using these tools : NodeJS, CasperJS, PhantomJS, Chrome Headless, Firefox Headless, Selenium. 
I chose Selenium and Chrome Headless. 


## Getting started

You will need Java 8, Maven, Selenium,  Chrome last version and [ChromeDriver](https://chromedriver.storage.googleapis.com/index.html?path=2.35/)

### Prerequisites

After downloading ChromeDriver, you will need to edit the config.properties file, and set the path to your chromedriver executable

***config.properties***
```
chromeDriverPath=/Path/to/chromedriver
```

## Actual results 

For the moment, this script is able to retrieve the 5000 bank account operations in 3/4 minutes(depending on the thread number), and write the result to a JSON file. 
This is ***bad*** . 
Basically there are only 100 requests to make ( 50 items / request), it shouldn't take that long, but because I didn't identify every blocking scenarios for the parsing, I had to make lots of retries everywhere, slowing down the whole process. 

All transactions are generated client side, using the start parameter as an offset, and some others like slowmode/failmode to generate different cases, so we could only load the page once, with the script, and execute the generate() / doGenerate() function to extract all transactions. But would that still be considered web scraping ? On a real banking website, that would not be possible to do that !

## TODO

* Override setTimeOut to speed up everything
* Benchmark / fine tune the thread number / make it dynamic (hardcoded atm)
* Maybe use Chrome tabs instead of 1 instance of chrome per thread
* Identify the last cases to eliminate all the nasty retries everywhere.
