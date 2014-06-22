# Load data
NEI <- readRDS("summarySCC_PM25.rds")
SCC <- readRDS("Source_Classification_Code.rds")


# Make some preparation
library(data.table)

data <- as.data.table(NEI)

# Find relevant SCC identifiers
x <- SCC[grep("Highway Vehicles", SCC$SCC.Level.Two), "SCC"]

data <- data[ data$fips %in% c("24510", "06037"), ]
data <- data[data$SCC %in% x, ] 
data$City <- as.factor(sapply(data$fips, function(f) if(f == "24510") return("Baltimore City") else return("Los Angeles County")))
data[, c("fips", "SCC", "Pollutant", "type"):=NULL]

data <- data[, lapply(.SD, sum), by = c("year", "City")]
# Plot the graph and save it on disk
png(file="plot6.png", width= 1024, height= 1024)
qplot(year, Emissions, data= data, geom= "line", color= City) + 
  labs(title="Total Emissions from motor-vehicle sources in Baltimore City and Los Angeles County") + 
  labs(y = expression(PM[2.5] * " in tons"), x = "Year")

dev.off()

