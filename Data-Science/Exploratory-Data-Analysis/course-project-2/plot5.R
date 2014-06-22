# Load data
NEI <- readRDS("summarySCC_PM25.rds")
SCC <- readRDS("Source_Classification_Code.rds")


# Make some preparation
library(data.table)

data <- as.data.table(NEI)

# Find relevant SCC identifiers
x <- SCC[grep("Highway Vehicles", SCC$SCC.Level.Two), "SCC"]

data <- data[ data$fips == "24510", ]
data <- data[data$SCC %in% x, ] 
data[, c("fips", "SCC", "Pollutant", "type"):=NULL]

data <- data[, lapply(.SD, sum), by = c("year")]
# Plot the graph and save it on disk
png(file="plot5.png", width= 1024, height= 1024)
qplot(year, Emissions, data= data, geom= "line") + 
  labs(title="Total Emissions from motor-vehicle sources in Baltimore City") + 
  labs(y = expression(PM[2.5] * " in tons"), x = "Year")

dev.off()

