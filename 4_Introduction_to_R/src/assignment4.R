# Assignment 04 - Introduction to R

# 1. Variables and functions
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
# 2.1.a Histogram for examination results including XY labels and color
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
# 4. Vectors
# 5. Dataframes & Plots
# 6. Plots and Smoothing curves
# 7. Normal-distribution
# 8. T-test
# 9. tapply & apply
# 10. Functions
