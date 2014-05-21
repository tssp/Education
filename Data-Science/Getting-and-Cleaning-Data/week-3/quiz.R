# Question 1:
#
# The American Community Survey distributes downloadable data about United States
# communities. Download the 2006 microdata survey about housing for the state of 
# Idaho using download.file() from here: 
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06hid.csv 
# and load the data into R. The code book, describing the variable names is here: 
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FPUMSDataDict06.pdf 
#
# Create a logical vector that identifies the households on greater than 10 acres 
# who sold more than $10,000 worth of agriculture products. Assign that logical 
# vector to the variable agricultureLogical. Apply the which() function like this 
# to identify the rows of the data frame where the logical vector is TRUE. 
# which(agricultureLogical) What are the first 3 values that result?
loadTemporaryFile <- function(file, url) {
 
  if(!file.exists(file)) {
    
    message("Downloading data set")
    download.file(url, destfile= file, mode= "wb")
  }
  
  file
}


url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06hid.csv"
file <- ".question-1.tmp.csv"
if(!file.exists(file)) {
  
  message("Downloading data set")
  download.file(url, destfile= file, mode= "wb")
}

# read local file into data.frame
df <- read.csv(file)

# filter NAs and households greater than 10 acres and more than $10K agricultures 
agricultureLogical <- (!is.na(df$ACR) & df$ACR==3 & !is.na(df$AGS) & df$AGS==6)
agricultureLogical <- (df$ACR==3 & df$AGS==6)

result <- rownames(head(df[which(agricultureLogical), ], 3))

cat('Question 1:', result, '\n') 



# Question 2:
# 
# Using the jpeg package read in the following picture of your instructor into R 
# https://d396qusza40orc.cloudfront.net/getdata%2Fjeff.jpg 
#
# Use the parameter native=TRUE. What are the 30th and 80th quantiles of the 
# resulting data?
library(jpeg)

url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fjeff.jpg"
file <- ".question-2.tmp.jpeg"
if(!file.exists(file)) {
  
  message("Downloading data set")
  download.file(url, destfile= file, mode= "wb")
}

image <- readJPEG(file, native= T)
result <- quantile(image, c(0.3, 0.8))

cat('Question 2:', result, '\n') 




# Question 3
#
# Load the Gross Domestic Product data for the 190 ranked countries in this data
# set:  https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FGDP.csv 
#
# Load the educational data from this data set: 
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FEDSTATS_Country.csv 
#
# Match the data based on the country shortcode. How many of the IDs match? Sort
# the data frame in descending order by GDP rank. What is the 13th country in the
# resulting data frame? 
#
# Original data sources: 
# http://data.worldbank.org/data-catalog/GDP-ranking-table 
# http://data.worldbank.org/data-catalog/ed-stats
#
readLines(".question-3a.tmp.csv", n=10)


dfEDU <- read.csv(loadTemporaryFile(".question-3b.tmp.csv", "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FEDSTATS_Country.csv"))

dfGDP <- read.csv(loadTemporaryFile(".question-3a.tmp.csv", "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FGDP.csv"),
                  skip=5, header=F, nrows=190)

dfGDP <- dfGDP[, c("V1", "V2", "V4", "V5")]
names(dfGDP) <- c("CountryCode", "Rank", "Long.Name", "Millions")

match <- merge(dfEDU, dfGDP, by.x="CountryCode", by.y="CountryCode")
match$Long.Name.x <- as.character(match$Long.Name.x)
match$Long.Name.y <- as.character(match$Long.Name.y)
result <- match[order(match$Rank, decreasing = T), ]

cat('Question 3:', nrow(result), ",", result$Long.Name.x[13], '\n') 



# Question 4:
#
# What is the average GDP ranking for the "High income: OECD" and "High income: 
# nonOECD" group?
result <- sapply(split(match$Rank, match$Income.Group), mean)
result <- c(result[["High income: OECD"]], result[["High income: nonOECD"]])

cat('Question 4:', result, '\n') 



# Question 5:
#
# Cut the GDP ranking into 5 separate quantile groups. Make a table versus 
# Income.Group. How many countries are Lower middle income but among the 38
# nations with highest GDP?
library(reshape2)

df <- match[order(match$Rank), ]
df <- head(df, 38)
df <- melt(df, id="Income.Group", measure.vars= "Rank")
df <- dcast(df, Income.Group ~ variable, sum)
result <- df[df$Income.Group == "Lower middle income", 2]

cat('Question 5:', result, '\n') 
