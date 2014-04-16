complete <- function(directory, id = 1:332) {
  ## 'directory' is a character vector of length 1 indicating
  ## the location of the CSV files
  
  ## 'id' is an integer vector indicating the monitor ID numbers
  ## to be used
  
  ## Return a data frame of the form:
  ## id nobs
  ## 1  117
  ## 2  1041
  ## ...
  ## where 'id' is the monitor ID number and 'nobs' is the
  ## number of complete cases
  
  mv <- c()
  
  for(i in id) {
    
    file <- sprintf("%s/%03d.csv", directory, i)
    data = read.csv(file)
    
    mvl <- data[!is.na(data['sulfate']) & !is.na(data['nitrate']),]
    mv <- rbind(mv, c(i, nrow(mvl)))
  }
  
  
  df <- data.frame(mv)
  colnames(df) <- c('id', 'nobs')
  
  df
}
