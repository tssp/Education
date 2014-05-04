library('testthat')

source('rankhospital.R')


expect_that(rankhospital("TX", "heart failure", 4),       equals("DETAR HOSPITAL NAVARRO"))
expect_that(rankhospital("MD", "heart attack", "worst"),  equals("HARFORD MEMORIAL HOSPITAL"))
expect_that(rankhospital("MN", "heart attack", 5000),     equals(NA))
