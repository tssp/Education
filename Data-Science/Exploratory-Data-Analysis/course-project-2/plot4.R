# Load data
NEI <- readRDS("summarySCC_PM25.rds")
SCC <- readRDS("Source_Classification_Code.rds")


# Make some preparation
library(data.table)

data <- as.data.table(NEI)

# Find relevant SCC identifiers
x <- SCC[grep("(C|c)ombustion", SCC$SCC.Level.One), "SCC"]
y <- SCC[grep("(C|c)oal", SCC$SCC.Level.One), "SCC"]

data <- data[(data$SCC %in% x | data$SCC %in% y), ] 
data[, c("fips", "SCC", "Pollutant", "type"):=NULL]

data <- data[, lapply(.SD, sum), by = c("year")]
data$Emissions = data$Emissions / 1000

# Plot the graph and save it on disk
png(file="plot4.png", width= 1024, height= 1024)
qplot(year, Emissions, data= data, geom= "line") + 
  labs(title="Total Emissions for coal combustion-related sources") + 
  labs(y = expression(PM[2.5] * " in kilo tons"), x = "Year")

dev.off()

