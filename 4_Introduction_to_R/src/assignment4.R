# Assignment 04 - Introduction to R

# 1. Variables and functions
cat("##### 1. Variables and functions ######\n")
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
i = 0
exam_own_mean = 0
for (x in 1 : length(exam)) {
    exam_own_mean = exam[x] + exam_own_mean
    i = i + 1
}
exam_own_mean = exam_own_mean / i
cat("Mean of exam with self implemented mean function: ", exam_own_mean, "\n")

# 2. Histograms and Boxplots
cat("\n##### 2. Histograms and Boxplots ######\n")
# 2.1.a Histogram for examination results including XY labels and color
cat("Generating histogram (exam_histogram.png) and boxplot (exam_boxplot.png).\n")
png(file = "exam_histogram.png")
grades_labels <- c(1.0, 1.3, 1.7, 2.0, 2.3, 2.7, 3.0, 3.3, 3.7, 4.0, 5.0)
hist(exam, col = "lightblue", breaks = 10, xaxt = "n", xlim = c(1, 5), yaxt = "n", xlab = "Grades")
axis(1, at = c(grades_labels), las = 2)
axis(2, at = 0 : 2, las = 2)
dev.off()
# 2.1.b Boxplots for examination results including XY labels and color
png(file = "exam_boxplot.png")
boxplot(exam, col = "pink", yaxt = "n", xlab = "Exam", ylab = "Grades")
axis(2, at = c(grades_labels), las = 2)
dev.off

# 3. Dataframes, Correlations & Scatter plots
cat("\n##### 3. Dataframes, Correlations & Scatter plots ######\n")
# 3.1 Loading dataset faithful
d <- data.frame(e = c(faithful$eruptions), w = c(faithful$waiting))
# 3.2 Calculate Variance, Standard deviation, Average, Correlation
cat("Eruptions Variance: ", var(d$e), "\n")
cat("Eruptions Standard deviation: ", sd(d$e), "\n")
cat("Eruptions Average: ", mean(d$e), "\n")
cat("Eruptions Correlation (Kendall): ", cor(data.frame(d$e), use = "all.obs", method = "kendall") , "\n")

cat("Waiting Variance: ", var(d$w), "\n")
cat("Waiting Standard deviation: ", sd(d$w), "\n")
cat("Waiting Average: ", mean(d$w), "\n")
cat("Waiting Correlation (Kendall): ", cor(data.frame(d$w), use = "all.obs", method = "kendall") , "\n")
# 3.3 Plot Scatter-plot
cat("Generating Scatter-plot (faithful_scatter.png).\n")
png(file = "faithful_scatter.png")
plot(d$e, d$w, main = "Faithful Dataset", xlab = "eruptions", ylab = "waiting", col = "red")
dev.off()

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



# 6. Plots and Smoothing curves
# 7. Normal-distribution
# 8. T-test
# 9. tapply & apply
# 10. Functions
