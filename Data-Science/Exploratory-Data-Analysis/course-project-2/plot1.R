# Load data
NEI <- readRDS("summarySCC_PM25.rds")

# Make some preparation
library(data.table)

data <- as.data.table(NEI)
data[, c("fips", "SCC", "Pollutant", "type"):=NULL]

data <- data[, lapply(.SD, sum), by = c("year")]
data$Emissions= data$Emissions / 1000

# Plot the graph and save it on disk
library(ggplot2)

png(file="plot1.png", width= 1024, height= 1024)
qplot(year, Emissions, data= data, geom= "line") + 
  labs(title="Total Emissions USA") + 
  labs(y = expression(PM[2.5] * " in kilo tons"), x = "Year")
dev.off()

