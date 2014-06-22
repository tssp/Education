# Load data
NEI <- readRDS("summarySCC_PM25.rds")

# Make some preparation
library(data.table)

data <- as.data.table(NEI)
data <- data[ data$fips == "24510", ] 
data[, c("fips", "SCC", "Pollutant"):=NULL]

data <- data[, lapply(.SD, sum), by = c("year", "type")]
data$type = as.factor(data$type)

# Plot the graph and save it on disk
library(ggplot2)

png(file="plot3.png", width= 1024, height= 1024)
qplot(year, Emissions, data= data, geom= "line", color= type) + 
  labs(title="Emissions by type in Baltimore City, USA") + 
  labs(y = expression(PM[2.5] * " in tons"), x = "Year")

dev.off()

