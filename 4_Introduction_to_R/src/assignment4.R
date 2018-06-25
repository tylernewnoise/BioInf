#!/usr/bin/env Rscript
# Assignment 04 - Introduction to R

# 1. Variables and functions
cat("\n##### 1. Variables and functions ######\n")
# 1.1 Create variable 'exam' that contains the makrs
exam <- c(3.3, 1.7, 2.0, 4.0, 1.3, 2.0, 3.0, 2.7, 3.7, 2.3, 1.7, 2.3)
cat("Vector exam: ", exam, "\n")
# 1.2.a calculate mean
exam_mean <- mean(exam)
cat("Mean of exam: ", exam_mean, "\n")
# 1.2.b calculate variance and standard deviation
exam_sd <- sd(exam)
cat("Variance of exam: ", exam_sd, "\n")
exam_variance <- var(exam)
cat("Standard deviation of exam: ", exam_variance, "\n")
# 1.2.c calculate median
exam_median <- median(exam)
cat("Median of exam: ", exam_median, "\n")
# 1.3 self implemented mean function
own_mean <- function(x) {
    i = 0
    own_mean = 0
    for (j in 1 : length(x)) {
        own_mean = exam[j] + own_mean
        i = i + 1
    }
    own_mean = own_mean / i
}
cat("Mean of exam with self implemented mean function: ", own_mean(exam), "\n")

# 2. Histograms and Boxplots
cat("\n##### 2. Histograms and Boxplots ######\n")
# 2.1.a Histogram for examination results including XY labels and color
cat("Generating histogram (exam_histogram.png) and boxplot (exam_boxplot.png).\n")
png(file = "exam_histogram.png")
grades_labels <- c(1.0, 1.3, 1.7, 2.0, 2.3, 2.7, 3.0, 3.3, 3.7, 4.0, 5.0)
hist(exam, col = "lightblue", breaks = 10, xaxt = "n", xlim = c(1, 5), yaxt = "n", xlab = "Grades",
     main = "Histogram of exam")
axis(1, at = c(grades_labels), las = 2)
axis(2, at = 0 : 2, las = 2)
invisible(dev.off())
# 2.1.b Boxplots for examination results including XY labels and color
png(file = "exam_boxplot.png")
boxplot(exam, col = "pink", yaxt = "n", xlab = "Exam", ylab = "Grades", main = "Boxplot of exam")
axis(2, at = c(grades_labels), las = 2)
invisible(dev.off())

# 3. Dataframes, Correlations & Scatter plots
cat("\n##### 3. Dataframes, Correlations & Scatter plots ######\n")
# 3.1 Loading dataset faithful
library(MASS)
data(faithful)
cor(faithful$eruptions, faithful$waiting)
# 3.2 Calculate Variance, Standard deviation, Average, Correlation
cat("Eruptions Variance: ", var(faithful$eruptions), "\n")
cat("Eruptions Standard deviation: ", sd(faithful$eruptions), "\n")
cat("Eruptions Average: ", mean(faithful$eruptions), "\n")
cat("Waiting Variance: ", var(faithful$waiting), "\n")
cat("Waiting Standard deviation: ", sd(faithful$waiting), "\n")
cat("Waiting Average: ", mean(faithful$waiting), "\n")
cat("Correlation: ", cor(faithful$eruptions, faithful$waiting), "\n")
# 3.3 Plot Scatter-plot
cat("Generating Scatter-plot (faithful_scatter.png).\n")
png(file = "faithful_scatter.png")
plot(faithful$eruptions, faithful$waiting, main = "Faithful Dataset", xlab = "eruptions",
     ylab = "waiting", col = "red")
invisible(dev.off())

# 4. Vectors
cat("\n##### 4. Vectors ######\n")
# 4.1 Create Variables
X <- c(3, 7, 1, 10, 15, 8, 11, 2, 12)
Y <- c(8, 6, 2, 0, 4, 11, 9, 17, 3)
# 4.2 Remove last element of each vector
X <- X[- length(X)]
Y <- Y[- length(Y)]
# 4.3 Concatenate both vectors into new vector Z
Z <- c(X, Y)
cat ("X and Y concatenate to Z: ", Z, "\n")
# 4.4 Assign 9 to every number contained in Z which is greater than 9
for (x in 1 : length(Z)) {
    if (Z[x] > 9) {
        Z[x] <- 9
    }
}
cat ("Removed every number in Z greater 9: ", Z, "\n")

# 5. Dataframes & Plots
cat("\n##### 5. Dataframes & Plots ######\n")
# 5.1 Load data set anorexia
library(MASS)
data(anorexia)
# 5.2 Calculate average of $Postwt only for successful therapy
successful_therapy = c()
for (i in 1 : length(anorexia$Postwt)) {
    if (anorexia$Prewt[i] < anorexia$Postwt[i]) {
        successful_therapy <- c(anorexia$Postwt[i], successful_therapy)
    }
}
cat("Average of $Postwt only for successful_therapy: ", mean(successful_therapy), "\n")
# 5.3 Plot weight of each patient before and after therapy
# 5.3a All cohorts together
cat("Generating plot (anorexia_all_cohorts.png).\n")
png(file = "anorexia_all_cohorts.png")
plot(anorexia$Prewt, col = "red" , ylab = "Weight",
     main = "Weight of each patient before and after therapy")
points(anorexia$Postwt, col = "green", xlab = "Patient")
legend("bottomright", c("before", "after"), lty = c(1,2), col=c("red", "green"))
invisible(dev.off())
# 5.3b Split into three cohorts
# TODO

# 6. Plots and Smoothing curves
cat("\n#### 6. Plots and Smoothing curves #####\n")
# 6.1 Load dataset airquality
data(airquality)
# 6.2 Diagram that plots Temperature
cat("Generating plot for tempearature (airquality.png).\n")
png(file = "airquality.png")
plot(airquality[, "Temp"], type = "l", col = "blue", ylab = "Temperature in °F",
xlab = "Date", main = "Diagram of tempearature", xaxt="n")
legend("bottomright", c("f = 0.15"), lty = c(1), col = c("red"))
v1 <- c (15, 45, 75, 105, 135)
v2 <- c("May","Jun","Jul","Aug","Sep")
axis(1, at = v1, labels = v2)
# 6.3 Add smoothing curve
lines(lowess(airquality[, "Temp"], f = .15), col = "red")
invisible(dev.off())

# 7. Normal-distribution
cat("\n#### 7. Normal-distribution #####\n")
# 7.1 Create vectors of normal-distributed random samples
v_size_10 <- matrix(data = rep(rnorm(n = 10, mean = 50, sd = 15), 80), ncol = 10)
v_size_100 <- matrix(data = rep(rnorm(n = 100, mean = 50, sd = 15),80), ncol = 100)
v_size_1000 <- matrix(data = rep(rnorm(n = 1000, mean = 50, sd = 15),80), ncol = 1000)
# 7.2 Calculate average and standard deviation
cat("Mean for sample-size 10:", mean(apply(v_size_10, 1, mean)), "\n")
cat("Mean for sample-size 100:", mean(apply(v_size_100, 1, mean)), "\n")
cat("Mean for sample-size 1000:", mean(apply(v_size_1000, 1, mean)), "\n")
cat("Standard deviation for sample-size 10:", sd(apply(v_size_10, 1, sd)), "\n")
cat("Standard deviation for sample-size 100:", sd(apply(v_size_100, 1, sd)), "\n")
cat("Standard deviation for sample-size 1000:", sd(apply(v_size_1000, 1, sd)), "\n")
# 7.3 Create boxplot of the random-samples
cat("Generating boxplot for random samples(random.png).\n")
png(file = "random.png")
boxplot(apply(v_size_10, 1, mean), apply(v_size_100,1,mean),apply(v_size_1000,1,mean),
    col = c("red", "green", "blue"), main = "Normal-distribution")
invisible(dev.off())

# 8. T-test
cat("\n#### 8. T-test #####\n")
patient_data <- c(34 ,56 ,45 ,47 ,69 ,93 ,51 ,63 ,54 ,62 ,31 ,55 ,47 ,44 ,73 ,89 ,44 ,60 ,50, 61)
new_drug <- matrix(patient_data , byrow = T, nrow = 2, dimnames = list(c("Before", "After"), 1:10))
t.test(new_drug[1,], new_drug[2,], alpha = 0.01)

# 9. tapply & apply
cat("\n#### 9. tapply & apply #####\n")
# 9.1 Average temp per month
res <-  tapply(airquality$Temp, airquality$Month, mean)
cat("Average temp per month with tapply: ", res, "\n")
# 9.2 Average ozone per month
cat("Average ozone per month with tapply: ", tapply(airquality$Ozone, airquality$Month, mean,
    na.rm = T), "\n")
data(Orange)
# 9.3 Diameter per year
cat("Diameter per year with tapply: ", tapply(Orange$circumference, as.integer(Orange$age / 365),
    mean , na.rm = T), "\n")
# 9.4 Growth per year
cat("Growth per year with tapply: ", diff(tapply(Orange$circumference, as.integer(Orange$age / 365),
    mean , na.rm = T)), "\n")

# 10. Functions
cat("\n#### 10. Functions #####\n")
# 10.1 Iterative and...
own_sum_iter <- function(x) {
    sum = 0
    for (i in seq(from = 1, to = x, by = 1)) {
        sum = sum + i
    }
    return(sum)
}
# ... Recursive function
own_sum_rec <- function(x) {
    if (x == 1) {
        return(1)
    }
    return(x + own_sum_rec(x - 1))
}
# 10.2 Printing evil values
cat("Sum of 666 iterativly: ", own_sum_iter(666), "\n")
cat("Sum of 666 recursivly: ", own_sum_rec(666), "\n")
# 10.3 Printing reason
cat("The iterative function should be faster because there is only one function call on the stack.")
cat("\n")
