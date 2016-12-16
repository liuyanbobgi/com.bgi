library(ggplot2)
i <- 1
while (i < 11){
  filetmp <- paste("Cluster", i, sep = "")
  ori <- read.delim(filetmp, header = F)
  ori1 <- ori[2:nrow(ori),]
  colnames(ori1) <- c("rt", "Intensity")
  titletmp <- paste(ori[1,1],"_",ori[1,2]," XIC", sep = "")


  p <- ggplot(data = ori1, mapping = aes(x = rt, y = Intensity))

  q <- p + geom_line(colour = "blue", size = 1) + geom_point() +
       ggtitle(titletmp) + xlab("Retention Time (Min)") + 
        theme(plot.title = element_text(hjust = 0.5, face = "bold"))
  pdf(paste(filetmp,".pdf"))
  plot(q)
  dev.off()
  i <- i+1 
}

