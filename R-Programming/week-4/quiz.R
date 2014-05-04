
set.seed(1)
rp <- rpois(5, 2)

cat('Question 1: ', rp, '\n')
cat('Question 2: rnorm\n')
cat('Question 3: It ensures that the sequence of random numbers is reproducible.\n')
cat('Question 4: qpois.\n')

set.seed(10)
x <- rbinom(10, 10, 0.5)
e <- rnorm(10, 0, 20)
y <- 0.5 + 2 * x + e
cat('Question 5: Generate data from a Normal linear model ', y, '\n')
cat('Question 6: rbinom.\n')
cat('Question 7: the function call stack.\n')
cat('Question 8: 100%.\n')
cat('Question 9: It is the time spent by the CPU evaluating an expression\n')
cat('Question 10: elapsed time may be smaller than user time\n')