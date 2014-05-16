# Question 1:
#
# Register an application with the Github API here 
# https://github.com/settings/applications. Access the API to get information on
# your instructors repositories (hint: this is the url you want 
# "https://api.github.com/users/jtleek/repos"). Use this data to find the time
# that the datasharing repo was created. What time was it created? This tutorial
# may be useful (https://github.com/hadley/httr/blob/master/demo/oauth2-github.r).
# You may also need to run the code in the base R package and not R studio.


library(httr)
library(jsonlite)
# 
# # 1. Find OANuth settings for github:
# #    http://developer.github.com/v3/oauth/
# oauth_endpoints("github")
# 
# # 2. Register an application at https://github.com/settings/applications
# #    Insert your values below - if secret is omitted, it will look it up in
# #    the GITHUB_CONSUMER_SECRET environmental variable.
# #
# #    Use http://localhost:1410 as the callback url
# myapp <- oauth_app("github", "763d717e0b455892bfed", "4a9ab53df90a8bdb7f05c321c93b377bdd2e0ff5")
# 
# # 3. Get OAuth credentials
# github_token <- oauth2.0_token(oauth_endpoints("github"), myapp)
# 
# # 4. Use API
# req <- GET("https://api.github.com/users/jtleek/repos", config(token = github_token))

df <- fromJSON("https://api.github.com/users/jtleek/repos")
df <- df[df$name == "datasharing", ]


cat('Question 1: Repo has been created at', df$created_at, '\n')

# Question 2:
#
# The sqldf package allows for execution of SQL commands on R data frames. We
# will use the sqldf package to practice the queries we might send with the 
# dbSendQuery command in RMySQL. Download the American Community Survey data and
# load it into an R object called acs.
# https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06pid.csv 
# Which of the following commands will select only the data for the probability
# weights pwgtp1 with ages less than 50?

library(sqldf)

url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fdata%2Fss06pid.csv"
file <- ".question-2.tmp.csv"
if(!file.exists(file)) {
  
  message("Downloading data set for question 2")
  download.file(url, destfile= file, mode= "wb")
}

# read local file into data.frame
acs <- read.table(file, sep = ",", header= TRUE)

cat('Question 2: sqldf("select pwgtp1 from acs where AGEP < 50")\n')

# Question 3:
#
# Using the same data frame you created in the previous problem, what is the
# equivalent function to unique(acs$AGEP).

cat('Question 3: sqldf("select distinct AGEP from acs")\n')


# Question 4:
#
# How many characters are in the 10th, 20th, 30th and 100th lines of HTML from
# this page: http://biostat.jhsph.edu/~jleek/contact.html 
#
# (Hint: the nchar() function in R may be helpful)
con <- url("http://biostat.jhsph.edu/~jleek/contact.html")
htmlCode <- readLines(con)
close(con)

lines <- c(10, 20, 30, 100)
result <- sapply(lines, function(line) nchar(htmlCode[[line]]))

cat('Question 4:', result, '\n') 


# Question 5:
#
# Read this data set into R and report the sum of the numbers in the fourth
# column: https://d396qusza40orc.cloudfront.net/getdata%2Fwksst8110.for 
# 
# Original source of the data:
# http://www.cpc.ncep.noaa.gov/data/indices/wksst8110.for 
#
# (Hint this is a fixed width file format)
url <- "https://d396qusza40orc.cloudfront.net/getdata%2Fwksst8110.for"
file <- ".question-5.tmp.csv"
if(!file.exists(file)) {
  
  message("Downloading data set for question 5")
  download.file(url, destfile= file, mode= "wb")
}

# read local file into data.frame
df <- read.fwf(file, widths=c(12, 7, 4, 9, 4, 9, 4, 9, 4), sep='\t', header= F, skip=4)
result <- sum(df$V4)

cat('Question 5:', result, '\n') 
