# Question 1:
#
# The American Community Survey distributes downloadable data about United
# States communities. Download the 2006 microdata survey about housing for the
# state of Idaho using download.file() from
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06hid.csv 
# and load the data into R. The code book, describing the variable names is here
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FPUMSDataDict06.pdf 
#
# How many housing units in this survey were worth more than $1,000,000?
url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06hid.csv"
file <- ".question-1.tmp.csv"
if(!file.exists(file)) {
  
  message("Downloading data set for question 1")
  download.file(url, destfile= file, mode= "wb")
}

# read local file into data.frame
df <- read.table(file, sep = ",", header= TRUE)

# according to the code book, the attribute must be VAL = 24
sub <- df[!is.na(df$VAL) & df$VAL == 24, ]
cat('Question 1:', nrow(sub), 'units\n')


# Question 2:
#
# Use the data you loaded from Question 1. Consider the variable FES in the code
# book. Which of the "tidy data" principles does this variable violate?

cat('Question 2: Tidy data has one variable per column\n')


# Question 3:
# 
# Download the Excel spreadsheet on Natural Gas Aquisition Program here: 
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FDATA.gov_NGAP.xlsx 
# Read rows 18-23 and columns 7-15 into R and assign the result to a 
# variable called: dat.
#
# What is the value of: sum(dat$Zip*dat$Ext,na.rm=T) 
library(xlsx)

url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2FDATA.gov_NGAP.xlsx"
file <- ".question-3.tmp.xlsx"
if(!file.exists(file)) {
  
  message("Downloading data set for question 3")
  download.file(url, destfile= file, mode="wb")
}

colindex <- 7:15 
rowindex <- 18:23
dat <- read.xlsx(file, sheetIndex= 1, colIndex= colindex, rowIndex= rowindex)
s <- sum(dat$Zip*dat$Ext,na.rm=T)
cat('Question 3:', s, '\n')


# Question 4:
#
# Read the XML data on Baltimore restaurants from here: 
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Frestaurants.xml
#
# How many restaurants have zipcode 21231?
library(XML)

url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Frestaurants.xml"
file <- ".question-4.tmp.xml"
if(!file.exists(file)) {
  
  message("Downloading data set for question 4")
  download.file(url, destfile= file, mode="wb")
}

doc <- xmlTreeParse(file, useInternal= TRUE)
root <- xmlRoot(doc)

s <- length(xpathApply(root, "//zipcode[text() = 21231]"))
cat('Question 4:', s, 'restaurants\n')


# Question 5: 
#
# The American Community Survey distributes downloadable data about United 
# States communities. Download the 2006 microdata survey about housing for the
# state of Idaho using download.file() from here: 
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06pid.csv 
# using the fread() command load the data into an R object DT. 
#
# Which of the following is the fastest way to calculate the average value of
# the variable pwgtp15?
url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06pid.csv"
file <- ".question-5.tmp.csv"
if(!file.exists(file)) {
  
  message("Downloading data set for question 5")
  download.file(url, destfile= file, mode= "wb")
}

DT <- read.table(file, sep = ",", header= TRUE)
 
n1 <- "rowMeans(DT)[DT$SEX==1]; rowMeans(DT)[DT$SEX==2]"
n2 <- "sapply(split(DT$pwgtp15,DT$SEX),mean)"
n3 <- "DT[,mean(pwgtp15),by=SEX]"
n4 <- "mean(DT$pwgtp15,by=DT$SEX)"
n5 <- "tapply(DT$pwgtp15,DT$SEX,mean)"
n6 <- "mean(DT[DT$SEX==1,]$pwgtp15); mean(DT[DT$SEX==2,]$pwgtp15)"

f1 <- function() {rowMeans(DT)[DT$SEX==1]; rowMeans(DT)[DT$SEX==2]}
f2 <- function() {sapply(split(DT$pwgtp15,DT$SEX),mean)}
f3 <- function() {DT[,mean(pwgtp15),by=SEX]}
f4 <- function() {mean(DT$pwgtp15,by=DT$SEX)}
f5 <- function() {tapply(DT$pwgtp15,DT$SEX,mean)}
f6 <- function() {mean(DT[DT$SEX==1,]$pwgtp15); mean(DT[DT$SEX==2,]$pwgtp15)}

functions <- c(f2, f5, f6)
names     <- c(n2, n5, n6)
elapsed   <- sapply(functions, function(f) system.time(replicate(30, f()))[["elapsed"]])

result <- data.frame(elapsed, names)
result <- result[order(result$elapsed), ]

print(result)

cat('Question 5:', 'sapply(split(DT$pwgtp15,DT$SEX),mean) is the fastest algorithm\n')