rankhospital <- function(state, outcome, num="best") {
  
  ## Read outcome data
  ## Check that state and outcome are valid
  ## Return hospital name in that state with the given rank
  ## 30-day death rate
  
  df <- read.csv("outcome-of-care-measures.csv", colClasses = "character")
  
  if(!is.element(state, unique(df$State))) 
    stop('invalid state')
  
  if(!is.element(outcome, c('heart attack', 'heart failure', 'pneumonia'))) 
    stop('invalid outcome')
  
  mapper <- list( 'heart attack' = 'Hospital.30.Day.Death..Mortality..Rates.from.Heart.Attack',
                  'heart failure' = 'Hospital.30.Day.Death..Mortality..Rates.from.Heart.Failure',
                  'pneumonia' = 'Hospital.30.Day.Death..Mortality..Rates.from.Pneumonia')
  
  target <- as.character(mapper[outcome])
  
  # Filter state and project columns in question
  df <- df[df$State == state, c('Hospital.Name', target)]
  
  # Convert rate col to numerics
  suppressWarnings(df[target] <- as.numeric(df[, target]))
  
  # Filter on NAs 
  df <- df[!is.na(df[target]), ]
  
  # Sort by value and hospital name
  df <- df[order(df[2], df[1]), ]
  
  rank <- if(class(num) == 'numeric') {
    
    num
  
  } else {
    
    if (num == 'best') 1 else nrow(df)
  }
  
  if(nrow(df) >= rank) {
    
    df[rank,1]
    
  } else {
    
    NA
  }
}