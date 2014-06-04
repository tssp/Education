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
png(file="plot3.png", width= 480, height= 480)

plot(df$Time, df$Sub_metering_1, type="l", 
     main='', 
     xlab='',
     ylab='Energy sub metering')
lines(df$Time, df$Sub_metering_2, col= 'red')
lines(df$Time, df$Sub_metering_3, col= 'blue')
legend("topright",  lty=c(1,1),  col= c("black", "red", "blue"), legend=c("Sub_metering_1", "Sub_metering_2","Sub_metering_3"))

dev.off()

