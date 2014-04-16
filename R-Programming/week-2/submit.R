source('corr.R')
source('pollutantmean.R')
source('complete.R')
source('submitscript1.R')

testCorr <- function() {
  
  p <- function(threshold) {
    
    cat('Calculating correlation, threshold:', threshold, '\n')
    
    cr <- corr('specdata', threshold)
    print(head(cr))
    print(summary(cr))
    print(length(cr))
  }
  
  p(0)
  p(150)
  p(400)
  p(5000)
}

testComplete <- function() {
  
  p <- function(ids) {
    
    cat('Aggregating number of observations, ids:', ids, '\n')
    
    print(complete('specdata', ids))
  }
  
  p(1)
  p(c(2, 4, 8, 10, 12))
  p(30:25)
  p(3)
}

testPullutantMean <- function() {
  
  p <- function(variable, ids) {
    
    cat('Calculating mean of', variable, ', ids:', ids, '\n')
    
    print(pollutantmean('specdata', variable, ids))
  }
  
  p("sulfate", 1:10)
  p("nitrate", 70:72)
  p("nitrate", 23)
}