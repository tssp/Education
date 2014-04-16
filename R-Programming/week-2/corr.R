corr <- function(directory, threshold = 0) {
  ## 'directory' is a character vector of length 1 indicating
  ## the location of the CSV files
  
  ## 'threshold' is a numeric vector of length 1 indicating the
  ## number of completely observed observations (on all
  ## variables) required to compute the correlation between
  ## nitrate and sulfate; the default is 0
  
  ## Return a numeric vector of correlations
  
  mv <- c()
  
  files <- list.files(path=directory, pattern="*.csv", full.names=TRUE, recursive=FALSE)
  
  for(file in files) {
    
    data = read.csv(file)
    mvl <- data[!is.na(data['sulfate']) & !is.na(data['nitrate']),c('ID', 'sulfate', 'nitrate')]
    
    mv <- rbind(mv, mvl)
  }
  
  cv <- c()
  
  for(id in unique(mv$ID)) {
    
    mvl <- mv[mv['ID'] == id ,]
   
    if(nrow(mvl)>threshold) {  
      
      cv <- c(cv, cor(mvl['sulfate'], mvl['nitrate']))
    } 
  }
  
  cv
}
