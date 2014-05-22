library(data.table)


# This function loads the features, activities and subjects from either the
# train or test directory into a data.frame object
course.loaddata <- function(identifier)  {
  
  dir <- paste(root, identifier, sep="/")

  targets  <- paste(dir, paste("y_", identifier, ".txt", sep=""), sep="/")
  subjects <- paste(dir, paste("subject_", identifier, ".txt", sep=""), sep="/")
  features <- paste(dir, paste("X_", identifier, ".txt", sep=""), sep="/")
  
  dfActivities <- read.table(targets)
  dfSubjects   <- read.table(subjects)
  dfFeatures   <- read.table(features, col.names = dfFeatureNames$name)
  
  df <- dfFeatures
  df["activity_number"] = dfActivities
  df["subject"]  = dfSubjects
  
  df
}

# This function loads the train and test data and returns a merged data.table
# object
course.mergedata <- function() {
  
  dfTest  <- course.loaddata("test")
  dfTrain <- course.loaddata("train")
  
  rbind(dfTrain, dfTest)
}

message("Step 0: Preparing Global Variables")

root <- "UCI HAR Dataset"
dfFeatureNames <- read.table(paste(root, "features.txt", sep="/"), col.names= c("id", "name"), stringsAsFactors=FALSE)
dfFeatureNames$name <- gsub("\\(|\\)|-", "", dfFeatureNames$name)

dfActivityLabels <- read.table(paste(root, "activity_labels.txt", sep="/"), col.names= c("id", "name"), stringsAsFactors=FALSE)


message("Step 1: Preparing train and test data")
dfStep1 <- course.mergedata() 


message("Step 2: Selecting only mean and std columns")
dfFeatureNamesStep2 <- dfFeatureNames[grep("(std)|(mean)", dfFeatureNames$name), ]
columnNamesStep2 <- c("activity_number", "subject", dfFeatureNamesStep2$name)
dfStep2 <- dfStep1[columnNamesStep2]


message("Step 3: Replacing activity numbers by labels")
dfStep3 <- dfStep2
dfStep3["activity_number"] <- sapply(dfStep3$activity_number, function(x) dfActivityLabels$name[x])

message("Step 4: Setting friendly name to activity variable")
dfStep4 <- dfStep3
names(dfStep4)[names(dfStep4) == 'activity_number'] <- 'activity'

message("Step 5: Calculating averages by groups")
dfStep5 <- data.table(dfStep4)
dfStep5 <- dfStep5[, lapply(.SD, mean), by = c("activity", "subject")]

columnNamesStep5 <- c("activity", "subject", sapply(dfFeatureNamesStep2$name, function(colname) paste("avg", colname, sep="_")))
setnames(dfStep5, colnames(dfStep5), columnNamesStep5)

write.table(dfStep5, file="result.csv", sep="\t", quote= F, row.names= F)
