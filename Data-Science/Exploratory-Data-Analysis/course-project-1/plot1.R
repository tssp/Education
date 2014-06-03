library(sqldf)

# Read only the records on the given date
df <- read.csv.sql(
        file= "household_power_consumption.txt", 
        sql = "select * from file where Date in ('1/2/2007', '2/2/2007')",
        header = TRUE,
        sep = ";"
      )

# Convert the Date/Time fields from character to date/time classes
df$Time = strptime(paste(df$Date,df$Time,sep=" "), format="%d/%m/%Y %H:%M:%S")
df$Date = as.Date(df$Date, format="%d/%m/%Y")

# Plot the graph and save it on disk
png(file="plot1.png", width= 480, height= 480)
hist(df$Global_active_power,
     col='red',
     main='Global Active Power', 
     xlab='Global Active Power (kilowatts)')
dev.off()

