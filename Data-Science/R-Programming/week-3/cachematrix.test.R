library('testthat')

source('cachematrix.R')

test_that("Caching  the inverse matrix works as expected", {
  
  set.seed(1)
  om <- matrix(sample(1:9), nrow=3)
  im <- solve(om)
  
  sm <- makeCacheMatrix(om)
  
  expect_that(sm$get(),        equals(om))
  expect_that(sm$getinverse(), is_null())
  
  expect_that(cacheSolve(sm),  shows_message('No inverse matrix cached, calculating inverse'))
  
  expect_that(sm$get(),        equals(om))
  expect_that(sm$getinverse(), equals(im))
  
  expect_that(cacheSolve(sm),  shows_message('The inverse has already been calculated'))
})