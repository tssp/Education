# General

This CodeBook describes the raw and tidy data sets about wearable computing. The tidy data set consists of 81 features and 180 observations.

# Study Design


One of the most exciting areas in all of data science right now is wearable computing - see for example this article . Companies like Fitbit, Nike, and Jawbone Up are racing to develop the most advanced algorithms to attract new users. The data linked to from the course website represent data collected from the accelerometers from the Samsung Galaxy S smartphone. A full description is available at the site where the data was obtained: 

http://archive.ics.uci.edu/ml/datasets/Human+Activity+Recognition+Using+Smartphones 


# Requirements


Please download the data from https://d396qusza40orc.cloudfront.net/getdata%2Fprojectfiles%2FUCI%20HAR%20Dataset.zip and extract it to this repository directy. The repository must now have a structure identical to:

* CodeBook.md 
* README.md
* run_analysis.R
* UCI HAR Dataset/train and sub files
* UCI HAR Dataset/test and sub files
* UCI HAR Dataset and sub files

Detailled information about the data structure can be found in UCI HAR Dataset/README.


# Transformation

There are several steps that are executed on the raw data to transform it to a tidy data set. This section contains some prosa instructions, the technical instructions can be found in the run_analysis.R .

Each feature name in the original data set is modified to make it more readable: brackets, dots and dashes are removed.

## Step 1
From the train and test directory the X, y and subject files are read into data frame and combined as a new one. The result train and test data sets gets merged into a new one. The resulting data frame consists of the subject, the activity number and the 561 features that are described in UCI HAR Dataset/features.info

## Step 2
In the second step a column projection is performed to reduce the number of columns to the subject, activity number and those columns that contain *std* and *mean* information.

## Step 3
In the third step the activity numbers get replaced by the corresponding labels (see also UCI HAR Dataset/activity_labels.txt)

## Step 4
The fourth step sets the friendly column name *activity* in the activty column.

## Step 5
The fifth steps groups the observations by *activity* and *subject* and calculates the average on each feature. The feature names get a *_ave* suffix.

## Results
The resulting tidy data set gets saved as tidy_data.csv with a tab as delimiter.

