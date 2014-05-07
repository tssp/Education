rankall <- function(outcome, num="best") {
  
  ## Read outcome data
  ## Check that state and outcome are valid
  ## For each state, find the hospital of the given rank
  ## Return a data frame with the hospital names and the
  ## (abbreviated) state name
  
  df <- read.csv("outcome-of-care-measures.csv", colClasses = "character")
  
  if(!is.element(outcome, c('heart attack', 'heart failure', 'pneumonia'))) 
    stop('invalid outcome')
  
  mapper <- list( 'heart attack' = 'Hospital.30.Day.Death..Mortality..Rates.from.Heart.Attack',
                  'heart failure' = 'Hospital.30.Day.Death..Mortality..Rates.from.Heart.Failure',
                  'pneumonia' = 'Hospital.30.Day.Death..Mortality..Rates.from.Pneumonia')

  target <- as.character(mapper[outcome])
  
  states <- unique(df$State)
  
  rankPerState <- function(state) {
    
    # Filter state and project columns in question
    rdf <- df[df$State == state, c('Hospital.Name', target)]
    
    # Convert rate col to numerics
    suppressWarnings(rdf[target] <- as.numeric(rdf[, target]))
    
    # Filter on NAs 
    rdf <- rdf[!is.na(rdf[target]), ]
    
    # Sort by value and hospital name
    rdf <- rdf[order(rdf[2], rdf[1]), ]
    
    # Determine rank position
    rank <- if(class(num) == 'numeric') {
      
      num
      
    } else {
      
      if (num == 'best') 1 else nrow(rdf)
    }
    
    
    hospital <- if(rank <= nrow(rdf)) {
      
      rdf[rank,1]
      
    } else {
      
      NA
    }
  }
  
  hospitals <- sapply(states, rankPerState)
  
  results <- data.frame(cbind(hospitals, states))
  results <- results[order(results[2]),]
  colnames(results) <- c('hospital', 'state')
  
  results
}
