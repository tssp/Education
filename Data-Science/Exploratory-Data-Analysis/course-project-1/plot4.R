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
temp <- par(mfrow= c(1,2))

png(file="plot4.png", width= 480, height= 480)

plot(df$Time, df$Global_active_power, type="l", main='', xlab='', ylab='Global Active Power')
#plot(df$Time, df$Voltage, type="l", main='', xlab='datetim')

dev.off()

par(temp)

