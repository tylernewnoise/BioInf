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
for (x in 1:length(exam)) {
    exam_own_mean = exam[x] + exam_own_mean
    i = i + 1
}
exam_own_mean = exam_own_mean / i
cat("Mean of exam with self implemented mean function: ", exam_own_mean, "\n")

# 2. Histograms and Boxplots
# 2.1.a Histogram for examination results

# 2.1.b Boxplots for examination results

# 2.2 Add X and Y labels

# 2.3 Fill histogram and boxplot with color


# 3. Dataframes, Correlations & Scatter plots
# 4. Vectors
# 5. Dataframes & Plots
# 6. Plots and Smoothing curves
# 7. Normal-distribution
# 8. T-test
# 9. tapply & apply
# 10. Functions
