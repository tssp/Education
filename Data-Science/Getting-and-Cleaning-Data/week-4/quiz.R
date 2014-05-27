loadTemporaryFile <- function(file, url) {
  
  if(!file.exists(file)) {
    
    message("Downloading data set")
    download.file(url, destfile= file, mode= "wb")
  }
  
  file
}




# Question 1
#
# The American Community Survey distributes downloadable data about United States
# communities. Download the 2006 microdata survey about housing for the state of 
# Idaho using download.file() from here: 
#  
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06hid.csv 
#
# and load the data into R. The code book, describing the variable names is here: 
#  
#  https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FPUMSDataDict06.pdf 
#
# Apply strsplit() to split all the names of the data frame on the characters 
# "wgtp". What is the value of the 123 element of the resulting list?

df <- read.csv(loadTemporaryFile('.question-1.tmp.csv', 'https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06hid.csv'))
result <- strsplit(names(df), "wgtp")

cat("Question 1:", result[[123]], "\n")


# Question 2
#
# Load the Gross Domestic Product data for the 190 ranked countries in this data
# set:  https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FGDP.csv 
#
# Remove the commas from the GDP numbers in millions of dollars and average them.
#
# What is the average? 
#
# Original data sources: http://data.worldbank.org/data-catalog/GDP-ranking-table

df <- read.csv(loadTemporaryFile('.question-2.tmp.csv', 'https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FGDP.csv '),
               skip=5, header=F, nrows=190)
df <- df[, c("V1", "V2", "V4", "V5")]
names(df) <- c("CountryCode", "Rank", "Long.Name", "Millions")

gdp <- as.numeric(gsub(",", "", df$Millions))
result <- mean(gdp)

cat("Question 2:", result, "\n")


# Question 3:
#
# In the data set from Question 2 what is a regular expression that would allow 
# you to count the number of countries whose name begins with "United"? 
# 
# Assume that the variable with the country names in it is named countryNames. 
# 
# How many countries begin with United?

countryNames <- df$Long.Name

result <- length(grep("^United",countryNames))

cat("Question 3:", result, "\n")

# Question 4:
# 
# Load the Gross Domestic Product data for the 190 ranked countries in this data
# set: https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FGDP.csv 
#
# Load the educational data from this data set: 
#
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FEDSTATS_Country.csv 
#
# Match the data based on the country shortcode. Of the countries for which the 
# end of the fiscal year is available, how many end in June? 
#
# Original data sources: 
# http://data.worldbank.org/data-catalog/GDP-ranking-table 
# http://data.worldbank.org/data-catalog/ed-stats
df1 <- read.csv(loadTemporaryFile('.questiona-4a.tmp.csv', 'https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FGDP.csv'),
               skip=5, header=F, nrows=190)
df1 <- df1[, c("V1", "V2", "V4", "V5")]
names(df1) <- c("CountryCode", "Rank", "Long.Name", "Millions")

df2<- read.csv(loadTemporaryFile('.questiona-4b.tmp.csv', 'https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FEDSTATS_Country.csv'))

df <- merge(df1, df2, by= "CountryCode")
result <- length(grep("^Fiscal year end: June", df$Special.Notes))

cat("Question 4:", result, "\n")


# Question 5:
# 
# You can use the quantmod (http://www.quantmod.com/) package to get historical
# stock prices for publicly traded companies on the NASDAQ and NYSE. Use the
# following code to download data on Amazon's stock price and get the times the
# data was sampled.
library(quantmod)

amzn = getSymbols("AMZN",auto.assign=FALSE)
sampleTimes = index(amzn) 

# How many values were collected in 2012? How many values were collected on 
# Mondays in 2012?

# Long-form: 
# amzn[(index(amzn) >= as.Date("2012-01-01") & index(amzn) <= as.Date("2012-12-24")), ]

# Short-form:
amzn["2012"]
